package com.beepiz.cameracoroutines.sample

import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import splitties.concurrency.isUiThread
import splitties.init.appCtx

object Recorder {

    fun kushGaugeInBitsPerSecond(
            recordingWidth: Int,
            recordingHeight: Int,
            recordingFrameRate: Int,
            motionFactor: Int = 1) = (recordingWidth * recordingHeight).let { pixelCount ->
        pixelCount * recordingFrameRate * motionFactor * 0.07f
    }.toInt()

    const val desiredFrameRate = 30

    fun MediaRecorder.setupAndPrepare(size: Size, fileName: String = "CamCoroutinesTest", withAudio: Boolean = true) {
        val w = size.width
        val h = size.height
        check(!isUiThread)
        setOrientationHint(90)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        if (withAudio) setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile("${appCtx.getExternalFilesDir(null).absolutePath}/$fileName.mp4")
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        if (withAudio) setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setVideoSize(w, h)
        setVideoFrameRate(desiredFrameRate)
        setVideoEncodingBitRate(kushGaugeInBitsPerSecond(w, h, desiredFrameRate, motionFactor = 4))
        setOnErrorListener { mr, what, extra ->
            Log.e("MEDIA_RECORDER", "$what $extra")
        }
        prepare()
    }

    fun chooseVideoSize(choices: Array<Size>): Size {
        return choices.firstOrNull {
            it.height <= 720 && (it.width == (it.height * (16 / 9)))
        } ?: choices.last()
    }
}
