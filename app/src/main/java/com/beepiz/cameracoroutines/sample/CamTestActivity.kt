package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresPermission
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.extensions.cameraManager
import com.beepiz.cameracoroutines.sample.extensions.hasFlag
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import com.beepiz.cameracoroutines.sample.viewdsl.lazy
import com.beepiz.cameracoroutines.sample.viewdsl.setContentView
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.android.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import timber.log.Timber
import java.util.concurrent.TimeUnit

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
        val recorder = MediaRecorder()
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
            CamDevice(backCamId, camHandler).use { cam ->
                cam.open()
                ui.surfaceHolderState.awaitCreated()
                val camDispatcher = camHandler.asCoroutineDispatcher()
                async(camDispatcher) {
                    with(Recorder) {
                        recorder.setupAndPrepare(videoSize)
                    }
                }.await()
                val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
                val videoFormat = MediaFormat.createVideoFormat("video/avc", videoSize.width, videoSize.height)
                val codecName = codecList.findEncoderForFormat(videoFormat)
                val videoEncoder = MediaCodec.createByCodecName(codecName)
                try {
                    videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//                    val surfaces = listOf(recorder.surface)
                    val surfaces = listOf(ui.previewSurface, videoEncoder.createInputSurface())
                    cam.createCaptureSession(surfaces).use { session ->
                        session.awaitConfiguredState()
                        val captureRequest = session.createCaptureRequest(CamDevice.Template.RECORD) {
                            surfaces.forEach(it::addTarget)
                            it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
                        }
                        session.setRepeatingRequest(captureRequest)
                        val outputPath = "TODO" //TODO: Put a proper path
                        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                        try {
                            muxer.setOrientationHint(90)
                            videoEncoder.start()
                            val encoding = async(camDispatcher) {
                                val bufferInfo = MediaCodec.BufferInfo()
                                val timeoutUs = TimeUnit.MILLISECONDS.toMicros(100)
                                var videoTrackIndex = 0
                                var muxerStarted = false
                                encodingLoop@ while (true) {
                                    yield()
                                    val indexOrInfo = videoEncoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                                    if (indexOrInfo < 0) {
                                        @Suppress("UnnecessaryVariable")
                                        val encoderInfo = indexOrInfo
                                        when (encoderInfo) {
                                            MediaCodec.INFO_TRY_AGAIN_LATER -> continue@encodingLoop
                                            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                                check(!muxerStarted) { "Format changed twice!" }
                                                videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                                                muxer.start()
                                                muxerStarted = true
                                            }
                                            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> throw IllegalStateException("Shouldn't happen on API 21+")
                                            else -> {
                                                Timber.wtf("Unexpected encoderInfo: $encoderInfo")
                                                continue@encodingLoop
                                            }
                                        }
                                    } else {
                                        @Suppress("UnnecessaryVariable") val index = indexOrInfo
                                        val encodedData = videoEncoder.getOutputBuffer(index)
                                        if (bufferInfo.flags.hasFlag(MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
                                            bufferInfo.size = 0
                                        }
                                        if (bufferInfo.size != 0) {
                                            check(muxerStarted) { "Muxed hasn't started!" }
                                            // According to Android Tests (CameraRecordingStream),
                                            // It is sometimes necessary to adjust the
                                            // ByteBuffer values to match BufferInfo.
                                            encodedData.position(bufferInfo.offset)
                                            encodedData.limit(bufferInfo.offset + bufferInfo.size)

                                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                                        }
                                        videoEncoder.releaseOutputBuffer(index, false)
                                        if (bufferInfo.flags.hasFlag(MediaCodec.BUFFER_FLAG_END_OF_STREAM)) {
                                            break@encodingLoop
                                        }
                                    }
                                }
                            }
                            delay(5000)
                            videoEncoder.stop()
                            muxer.stop()
//                            recorder.start()
//                            delay(5000)
//                            recorder.stop()
                            session.stopRepeating()
                            encoding.await()
                            finish()
                        } finally {
                            muxer.release()
                        }
                    }
                } finally {
                    videoEncoder.release()
                }
            }
        } catch (e: CameraAccessException) {
            Timber.e(e)
        } catch (e: CamException) {
            Timber.e(e)
        } catch (e: Exception) {
            Timber.e(e)
            finish()
        } finally {
            recorder.release()
        }
    }
}
