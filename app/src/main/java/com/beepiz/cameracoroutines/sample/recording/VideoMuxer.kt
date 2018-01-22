package com.beepiz.cameracoroutines.sample.recording

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Build.VERSION.SDK_INT
import android.os.Looper
import android.view.Surface
import com.beepiz.cameracoroutines.sample.extensions.hasFlag
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import splitties.concurrency.isUiThread

class VideoMuxer(private val parentJob: Job,
                 videoFormat: MediaFormat,
                 outputPath: String,
                 orientationInDegrees: Int) : AutoCloseable {

    private val videoCodec: MediaCodec
    private val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var muxerStarted = false

    private val audioRecord: AudioRecord

    init {
        check(!isUiThread) { "UI thread is forbidden to avoid UI stutters" }
        checkNotNull(Looper.myLooper()) {
            "Initialization on a looper non UI thread is needed for MediaCodec"
        }
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecName = codecList.findEncoderForFormat(videoFormat)
        videoCodec = MediaCodec.createByCodecName(codecName)
        muxer.setOrientationHint(orientationInDegrees)

        val audioSource = MediaRecorder.AudioSource.MIC
        val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setSampleRate(44100)
                .build()
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(audioFormat.sampleRate, channelConfig, audioFormat.encoding)
        audioRecord = if (SDK_INT >= 23) {
            AudioRecord.Builder()
                    .setAudioSource(audioSource)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .build()
        } else AudioRecord(audioSource, audioFormat.sampleRate, channelConfig, audioFormat.encoding, bufferSizeInBytes)
    }

    private val eosChannel = Channel<Unit>()
    private var videoTrackIndex = 0
    private var audioTrackIndex = 0

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            try {
                val encodedData = codec.getOutputBuffer(index)
                if (info.flags.hasFlag(MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
                    info.size = 0
                }
                if (info.size != 0) {
                    check(muxerStarted) { "Muxer hasn't started!" }
                    // According to Android Tests (CameraRecordingStream),
                    // It is sometimes necessary to adjust the
                    // ByteBuffer values to match BufferInfo.
                    encodedData.position(info.offset)
                    encodedData.limit(info.offset + info.size)

                    muxer.writeSampleData(videoTrackIndex, encodedData, info)
                }
                codec.releaseOutputBuffer(index, false)
                if (info.flags.hasFlag(MediaCodec.BUFFER_FLAG_END_OF_STREAM)) {
                    muxer.stop()
                    launch(parent = parentJob) { eosChannel.send(Unit) }
                }
            } catch (t: Throwable) {
                parentJob.cancel(t)
            }
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) = throw UnsupportedOperationException()

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            try {
                check(!muxerStarted) { "Format changed twice!" }
                videoTrackIndex = muxer.addTrack(format)
                muxer.start()
                muxerStarted = true
            } catch (t: Throwable) {
                parentJob.cancel(t)
            }
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            parentJob.cancel(e)
        }
    }

    init {
        videoCodec.setCallback(codecCallback)
        videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    /**
     * **Don't forget to call [Surface.release] when done.**
     */
    fun createInputSurface(): Surface = videoCodec.createInputSurface()

    fun start() {
        videoCodec.start()
        audioRecord.startRecording()
    }

    fun stop(): Deferred<Unit> {
        videoCodec.signalEndOfInputStream()
        audioRecord.stop()
        return async(parent = parentJob) { eosChannel.receive() }
    }

    override fun close() {
        muxer.release()
        videoCodec.release()
        audioRecord.release()
    }
}
