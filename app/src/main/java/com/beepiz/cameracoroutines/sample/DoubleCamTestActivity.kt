package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresPermission
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.beepiz.cameracoroutines.CamCaptureSession
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.extensions.cameraManager
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import com.beepiz.cameracoroutines.sample.viewdsl.lazy
import com.beepiz.cameracoroutines.sample.viewdsl.setContentView
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.android.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class DoubleCamTestActivity : AppCompatActivity() {

    private val ui by lazy(::DoubleCamTestUi)

    private lateinit var frontJob: Job
    private lateinit var backJob: Job

    private val camThread by kotlin.lazy { HandlerThread("camera") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui)
        camThread.start()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        frontJob = testFrontCam()
        backJob = testBackCam()
    }

    override fun onStop() {
        super.onStop()
        frontJob.cancel()
        backJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        camThread.quitSafely()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun testFrontCam() = launch(UI) {
        val camHandler = Handler(camThread.looper)
        val recorder = MediaRecorder()
        try {
            val camManager = cameraManager
            val backCamId = camManager.cameraIdList.firstOrNull {
                val characteristics = camManager.getCameraCharacteristics(it)
                characteristics[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_FRONT
            } ?: throw NoSuchElementException("No back camera found")
            val camCharacteristics = camManager.getCameraCharacteristics(backCamId)
            val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
            val outputSizes = configMap.outputSizes<MediaRecorder>()
            val previewSizes = configMap.outputSizes<SurfaceHolder>()
            val videoSize = Recorder.chooseVideoSize(outputSizes)
            val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]
            val cam = CamDevice(backCamId, camHandler)
            cam.open()
            ui.surfaceHolderState1.createdChannel.consume {
                for (created in this) if (created) break
            }
            val camDispatcher = camHandler.asCoroutineDispatcher()
            async(camDispatcher) {
                with(Recorder) {
                    recorder.setupAndPrepare(videoSize, fileName = "CamCoroutinesFront", withAudio = false)
                }
            }.await()
            val surfaces = listOf(recorder.surface)
            val session = cam.createCaptureSession(surfaces)
            session.stateChannel.consume {
                loop@ for (state in this) {
                    when (state) {
                        is CamCaptureSession.State.Configured -> break@loop
                        is CamCaptureSession.State.Closed -> throw CancellationException("Session closed!")
                    }
                }
            }
            val captureRequest = session.createCaptureRequest(CamDevice.Template.PREVIEW) {
                surfaces.forEach(it::addTarget)
                it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
            }
            session.setRepeatingRequest(captureRequest)
            recorder.start()
            delay(5000)
            recorder.stop()
            delay(1000)
            finish()
        } catch (e: Exception) {
            Timber.e(e)
            finish()
        } finally {
            recorder.release()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun testBackCam() = launch(UI) {
        val recorder = MediaRecorder()
        val camHandler = Handler(camThread.looper)
        try {
            val camManager = cameraManager
            val backCamId = camManager.cameraIdList.firstOrNull {
                val characteristics = camManager.getCameraCharacteristics(it)
                characteristics[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_BACK
            } ?: throw NoSuchElementException("No back camera found")
            val camCharacteristics = camManager.getCameraCharacteristics(backCamId)
            val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
            val outputSizes = configMap.outputSizes<MediaRecorder>()
            val previewSizes = configMap.outputSizes<SurfaceHolder>()
            val videoSize = Recorder.chooseVideoSize(outputSizes)
            val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]
            val cam = CamDevice(backCamId, camHandler)
            cam.open()
            ui.surfaceHolderState2.createdChannel.consume {
                for (created in this) if (created) break
            }
            val camDispatcher = camHandler.asCoroutineDispatcher()
            async(camDispatcher) {
                with(Recorder) {
                    recorder.setupAndPrepare(videoSize, fileName = "CamCoroutinesBack")
                }
            }.await()
            val surfaces = listOf(recorder.surface)
            val session = cam.createCaptureSession(surfaces)
            session.stateChannel.consume {
                loop@ for (state in this) {
                    when (state) {
                        is CamCaptureSession.State.Configured -> break@loop
                        is CamCaptureSession.State.Closed -> throw CancellationException("Session closed!")
                    }
                }
            }
            val captureRequest = session.createCaptureRequest(CamDevice.Template.PREVIEW) {
                surfaces.forEach(it::addTarget)
                it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
            }
            session.setRepeatingRequest(captureRequest)
            recorder.start()
            delay(5000)
            recorder.stop()
        } catch (e: Exception) {
            Timber.e(e)
            finish()
        } finally {
            recorder.release()
        }
    }
}
