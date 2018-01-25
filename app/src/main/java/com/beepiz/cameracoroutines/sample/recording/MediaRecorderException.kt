package com.beepiz.cameracoroutines.sample.recording

import android.media.MediaRecorder

data class MediaRecorderException(val what: Int, val extra: Int) : Exception() {
    private fun whatStr() = when (what) {
        MediaRecorder.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
        MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN -> "MEDIA_RECORDER_ERROR_UNKNOWN"
        else -> "MEDIA_ERROR_UNKNOWN_CODE: $what"
    }

    override val message = "${whatStr()} extra: $extra"
}
