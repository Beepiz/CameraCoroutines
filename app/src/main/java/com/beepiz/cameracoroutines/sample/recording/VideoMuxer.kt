package com.beepiz.cameracoroutines.sample.recording

import android.media.MediaMuxer
import android.os.Looper
import kotlinx.coroutines.experimental.Job
import splitties.concurrency.isUiThread

class VideoMuxer(private val parentJob: Job,
                 private val videoEncoder: VideoEncoder,
                 private val audioEncoder: AudioEncoder?,
                 outputPath: String,
                 orientationInDegrees: Int) : AutoCloseable {

    private val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var muxerStarted = false

    init {
        check(!isUiThread) { "UI thread is forbidden to avoid UI stutters" }
        checkNotNull(Looper.myLooper()) {
            "Initialization on a looper non UI thread is needed for MediaCodec"
        }
        muxer.setOrientationHint(orientationInDegrees)
    }

    private var videoTrackIndex = 0
    private var audioTrackIndex = 0

    fun start() {
        videoEncoder.start()
        audioEncoder?.start()
    }

    suspend fun stop() {
        videoEncoder.stop()
        audioEncoder?.stop()
        videoEncoder.eosChannel.receive()
        audioEncoder?.eosChannel?.receive()
        muxer.stop()
    }

    override fun close() {
        muxer.release()
        videoEncoder.close()
        audioEncoder?.close()
    }
}
