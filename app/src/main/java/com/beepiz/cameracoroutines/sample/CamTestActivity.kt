package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresPermission
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.extensions.cameraManager
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import com.beepiz.cameracoroutines.sample.viewdsl.lazy
import com.beepiz.cameracoroutines.sample.viewdsl.setContentView
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.android.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import splitties.init.appCtx
import timber.log.Timber

class CamTestActivity : AppCompatActivity() {

    private val ui by lazy(::CamTestUi)

    private lateinit var testJob: Job

    private val camThread by kotlin.lazy { HandlerThread("camera") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui)
        camThread.start()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        testJob = testCamera()
    }

    override fun onStop() {
        super.onStop()
        testJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        camThread.quitSafely()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun testCamera() = launch(UI) {
        val camHandler = Handler(camThread.looper)
        val previewSurface = ui.previewSurface
        val surfaceState = ui.surfaceHolderState
        try {
            val camManager = cameraManager
            val backCamId = camManager.cameraIdList.firstOrNull {
                val characteristics = camManager.getCameraCharacteristics(it)
                characteristics[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_BACK
            } ?: throw NoSuchElementException("No back camera found")
            val camCharacteristics = camManager.getCameraCharacteristics(backCamId)
            val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
            val outputSizes = configMap.outputSizes<MediaCodec>()
            val previewSizes = configMap.outputSizes<SurfaceHolder>()
            val videoSize = Recorder.chooseVideoSize(outputSizes)
            val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]
            CamDevice(backCamId, camHandler).use { cam ->
                cam.open()
                surfaceState.awaitCreated()
                val camDispatcher = camHandler.asCoroutineDispatcher()
                async(camDispatcher) {
                    val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_MPEG4, videoSize.width, videoSize.height).also {
                        it.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                        val fps = 30
                        val bitrate = Recorder.kushGaugeInBitsPerSecond(videoSize.width, videoSize.height, fps, 4)
                        it.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                        it.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                        it.setInteger(MediaFormat.KEY_CAPTURE_RATE, fps)
                        it.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
                    }
                    val outputPath = "${appCtx.getExternalFilesDir(null).absolutePath}/VideoEncoderTest.mp4"
                    VideoEncoder(videoFormat, outputPath, orientationInDegrees = 90).use { encoder ->
                        val encoderInputSurface = encoder.createInputSurface()
                        val surfaces = listOf(previewSurface, encoderInputSurface)
                        cam.createCaptureSession(surfaces).use { session ->
                            session.awaitConfiguredState()
                            val captureRequest = session.createCaptureRequest(CamDevice.Template.RECORD) {
                                surfaces.forEach(it::addTarget)
                                it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
                            }
                            session.setRepeatingRequest(captureRequest)
                            encoder.start()
                            delay(5000)
                            encoder.stop().await()
                            session.stopRepeating()
                        }
                    }
                }.await()
                finish()
            }
        } catch (e: CameraAccessException) {
            Timber.e(e)
        } catch (e: CamException) {
            Timber.e(e)
        } catch (e: Exception) {
            Timber.e(e)
            finish()
        }
    }
}
