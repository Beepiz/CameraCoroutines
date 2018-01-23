package com.beepiz.cameracoroutines.sample.recording

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Looper
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import splitties.concurrency.isUiThread

class AudioEncoder(private val parentJob: Job,
                   audioMediaFormat: MediaFormat): AutoCloseable {

    private val audioRecord: AudioRecord

    private val audioCodec: MediaCodec

    init {
        check(!isUiThread) { "UI thread is forbidden to avoid UI stutters" }
        checkNotNull(Looper.myLooper()) {
            "Initialization on a looper non UI thread is needed for MediaCodec"
        }

        val audioSource = MediaRecorder.AudioSource.MIC
        val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setSampleRate(44100)
                .build()
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(audioFormat.sampleRate, channelConfig, audioFormat.encoding)
        audioRecord = if (SDK_INT >= M) {
            AudioRecord.Builder()
                    .setAudioSource(audioSource)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .build()
        } else AudioRecord(audioSource, audioFormat.sampleRate, channelConfig, audioFormat.encoding, bufferSizeInBytes)

        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecName = codecList.findEncoderForFormat(audioMediaFormat)
        audioCodec =  MediaCodec.createByCodecName(codecName)
    }

    private val _eosChannel = Channel<Unit>()
    internal val eosChannel: ReceiveChannel<Unit> get() = _eosChannel

    fun start() {
        audioRecord.startRecording()
        TODO("Start codec")
    }

    fun stop() {
        audioRecord.stop()
        TODO()
    }

    override fun close() {
        audioRecord.release()
    }
}
