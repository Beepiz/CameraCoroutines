package com.beepiz.cameracoroutines.sample.autorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Handler
import android.support.annotation.RequiresPermission
import android.util.Size
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.extensions.cameraManager
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import com.beepiz.cameracoroutines.sample.recording.VideoRecorder
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
suspend fun recordVideo(
        lensFacing: CamCharacteristics.LensFacing,
        outputPath: String,
        bgHandler: Handler,
        durationMillis: Int = 10_000
) = recordVideo(lensFacing.intValue, durationMillis, outputPath, bgHandler)

@RequiresPermission(Manifest.permission.CAMERA)
@SuppressLint("MissingPermission")
@Throws
private suspend fun recordVideo(lensFacing: Int,
                                durationMillis: Int,
                                outputPath: String,
                                bgHandler: Handler) {
    require(bgHandler.looper != mainLooper) {
        "bgHandler is NOT on a background Looper!"
    }
    val currentJob = coroutineContext[Job]!!
    async(coroutineContext + bgHandler.asCoroutineDispatcher()) {
        val camId: String = camManager.cameraIdList.firstOrNull {
            val characteristics = camManager.getCameraCharacteristics(it)
            characteristics[CameraCharacteristics.LENS_FACING] == lensFacing
        } ?: throw NoSuchElementException("No back camera found")
        val camCharacteristics = camManager.getCameraCharacteristics(camId)
        val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val videoSize = chooseVideoSize(configMap.outputSizes<MediaCodec>())
        val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]
        val cam = CamDevice(camId, bgHandler)
        val recorder = MediaRecorder()
        try {
            cam.open()
            with(VideoRecorder) {
                recorder.setupAndPrepare(currentJob, videoSize, sensorOrientation, outputPath)
            }
            val surfaces = listOf(recorder.surface)
            cam.createCaptureSession(surfaces).use { session ->
                session.awaitConfiguredState()
                val captureRequest = session.createCaptureRequest(CamDevice.Template.RECORD) {
                    surfaces.forEach(it::addTarget)
                    it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
                }
                session.setRepeatingRequest(captureRequest)
                recorder.start()
                delay(durationMillis)
                session.stopRepeating()
            }
            recorder.stop()
        } finally {
            cam.close()
            recorder.release()
        }
    }.await()
}

private fun chooseVideoSize(choices: Array<Size>): Size {
    return choices.firstOrNull { (w, h) ->
        minOf(w, h) <= 480 && maxOf(w, h) <= 800 // && (it.width == (it.height * (16 / 9)))
    } ?: choices.last()
}

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Size.component1() = width

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Size.component2() = height
