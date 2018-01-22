package com.beepiz.cameracoroutines.sample.recording

import android.media.MediaRecorder
import android.util.Size
import splitties.concurrency.isUiThread
import splitties.init.appCtx
import timber.log.Timber

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
        setVideoEncodingBitRate(kushGaugeInBitsPerSecond(w, h, desiredFrameRate, motionFactor = 2))
        setOnErrorListener { mr, what, extra ->
            Timber.e("$what $extra")
        }
        prepare()
    }

    fun chooseVideoSize(choices: Array<Size>): Size {
        return choices.firstOrNull { (w, h) ->
            minOf(w, h) <= 480 && maxOf(w, h) <= 800 // && (it.width == (it.height * (16 / 9)))
        } ?: choices.last()
    }

    @Suppress("NOTHING_TO_INLINE") private inline operator fun Size.component1() = width
    @Suppress("NOTHING_TO_INLINE") private inline operator fun Size.component2() = height
}
