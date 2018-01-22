package com.beepiz.cameracoroutines.sample.autorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.support.annotation.RequiresPermission
import android.util.Size
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.CamDevice.Template
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.extensions.cameraManager
import com.beepiz.cameracoroutines.sample.Recorder
import com.beepiz.cameracoroutines.sample.VideoEncoder
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import splitties.concurrency.mainLooper
import kotlin.coroutines.experimental.intrinsics.coroutineContext

private val camManager = cameraManager

@RequiresPermission(Manifest.permission.CAMERA)
@SuppressLint("MissingPermission")
@Throws(CameraAccessException::class, CamException::class, Exception::class)
suspend fun recordVideo(frontCamera: Boolean,
                        durationMillis: Int = 10000,
                        outputPath: String,
                        bgHandler: Handler) {
    require(bgHandler.looper != mainLooper) {
        "bgHandler is NOT on a background Looper!"
    }
    val lensFacing = if (frontCamera) CameraCharacteristics.LENS_FACING_FRONT
    else CameraCharacteristics.LENS_FACING_BACK
    recordVideo(lensFacing, durationMillis, outputPath, bgHandler)
}

@RequiresPermission(Manifest.permission.CAMERA)
@SuppressLint("MissingPermission")
private suspend fun recordVideo(lensFacing: Int,
                                durationMillis: Int,
                                outputPath: String,
                                bgHandler: Handler) {
    async(coroutineContext + bgHandler.asCoroutineDispatcher()) {
        val camId: String = camManager.cameraIdList.firstOrNull {
            val characteristics = camManager.getCameraCharacteristics(it)
            characteristics[CameraCharacteristics.LENS_FACING] == lensFacing
        } ?: throw NoSuchElementException("No back camera found")
        val camCharacteristics = camManager.getCameraCharacteristics(camId)
        val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val videoSize = chooseVideoSize(configMap.outputSizes<MediaCodec>())
        val (width, height) = videoSize
        val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]
        CamDevice(camId, bgHandler).use { cam ->
            cam.open()
            val videoFormat = createVideoFormat(width, height)
            val currentJob = coroutineContext[Job]!!
            VideoEncoder(currentJob, videoFormat, outputPath, sensorOrientation).use { encoder ->
                val encoderInputSurface = encoder.createInputSurface()
                val surfaces = listOf(encoderInputSurface)
                try {
                    cam.createCaptureSession(surfaces).use { captureSession ->
                        captureSession.awaitConfiguredState()
                        val captureRequest = captureSession.createCaptureRequest(Template.RECORD) {
                            surfaces.forEach(it::addTarget)
                            it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
                        }
                        captureSession.setRepeatingRequest(captureRequest)
                        encoder.start()
                        delay(durationMillis)
                        encoder.stop().await()
                        captureSession.stopRepeating()
                    }
                } finally {
                    encoderInputSurface.release()
                }
            }
        }
    }.await()
}

private fun chooseVideoSize(choices: Array<Size>): Size {
    return choices.firstOrNull { (w, h) ->
        minOf(w, h) <= 480 && maxOf(w, h) <= 800 // && (it.width == (it.height * (16 / 9)))
    } ?: choices.last()
}

private fun createVideoFormat(width: Int, height: Int): MediaFormat {
    return MediaFormat.createVideoFormat(videoMimeType, width, height).apply {
        setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        val fps = 30
        val bitrate = Recorder.kushGaugeInBitsPerSecond(width, height, fps, 4)
        setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        setInteger(MediaFormat.KEY_CAPTURE_RATE, fps)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
    }
}

@Suppress("NOTHING_TO_INLINE") private inline operator fun Size.component1() = width
@Suppress("NOTHING_TO_INLINE") private inline operator fun Size.component2() = height

private const val videoMimeType = MediaFormat.MIMETYPE_VIDEO_MPEG4
