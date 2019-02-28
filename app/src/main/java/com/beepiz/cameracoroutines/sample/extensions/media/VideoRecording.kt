package com.beepiz.cameracoroutines.sample.extensions.media

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaRecorder
import android.util.Size
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics.LensFacing
import com.beepiz.cameracoroutines.sample.extensions.outputSizes
import com.beepiz.cameracoroutines.sample.recording.VideoRecorder
import com.beepiz.cameracoroutines.withOpenCamera
import com.beepiz.cameracoroutines.withSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import splitties.mainthread.isMainThread
import splitties.systemservices.cameraManager

@ExperimentalCoroutinesApi
suspend fun recordVideo(
    lensFacing: LensFacing,
    outputPath: String,
    awaitStop: suspend () -> Unit
) {
    recordVideo(lensFacing.intValue, outputPath, awaitStop)
}

@ExperimentalCoroutinesApi
private suspend fun recordVideo(
    lensFacing: Int,
    outputPath: String,
    awaitStop: suspend () -> Unit
) = coroutineScope {
    val camManager = cameraManager
    val camId: String = camManager.cameraIdList.firstOrNull {
        val characteristics = camManager.getCameraCharacteristics(it)
        characteristics[CameraCharacteristics.LENS_FACING] == lensFacing
    } ?: throw NoSuchElementException("No camera with requested facing ($lensFacing) found")
    val recorderAsync = async(Dispatchers.Default) {
        val camCharacteristics = camManager.getCameraCharacteristics(camId)
        val configMap = camCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]!!
        val videoSize = VideoRecorder.chooseVideoSize(configMap.outputSizes<MediaCodec>())
        val sensorOrientation = camCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]!!
        MediaRecorder().apply {
            setupForVideoRecording(videoSize, sensorOrientation, outputPath)
        }
    }
    camManager.withOpenCamera(camId) { camera ->
        recorderAsync.await().use { recorder ->
            val surfaces = listOf(recorder.surface)
            camera.withSession(surfaces) { session ->
                session.awaitConfiguredState()
                val captureRequest = session.createCaptureRequest(CamDevice.Template.Record) {
                    surfaces.forEach(it::addTarget)
                    it[CaptureRequest.CONTROL_MODE] = CameraMetadata.CONTROL_MODE_AUTO
                }
                session.setRepeatingRequest(captureRequest)
                recorder.start()
                awaitStop()
                session.stopRepeating()
                session.awaitConfiguredState()
                recorder.stop()
            }
        }
    }
}

fun MediaRecorder.setupForVideoRecording(
    size: Size,
    orientationInDegrees: Int,
    outputPath: String,
    withAudio: Boolean = true
) {
    check(!isMainThread)
    val w = size.width
    val h = size.height
    setOrientationHint(orientationInDegrees)
    setVideoSource(MediaRecorder.VideoSource.SURFACE)
    if (withAudio) setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    setOutputFile(outputPath)
    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
    if (withAudio) setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    setVideoSize(w, h)
    val desiredFrameRate = 30
    setVideoFrameRate(desiredFrameRate)
    setVideoEncodingBitRate(
        kushGaugeInBitsPerSecond(
            recordingWidth = w,
            recordingHeight = h,
            recordingFrameRate = desiredFrameRate,
            motionFactor = 2
        )
    )
    prepare()
}

private fun kushGaugeInBitsPerSecond(
    recordingWidth: Int,
    recordingHeight: Int,
    recordingFrameRate: Int,
    motionFactor: Int = 1
): Int = (recordingWidth * recordingHeight).let { pixelCount ->
    pixelCount * recordingFrameRate * motionFactor * 0.07f
}.toInt()
