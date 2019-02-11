package com.beepiz.cameracoroutines.sample.extensions.media

import android.media.MediaRecorder
import com.beepiz.cameracoroutines.sample.recording.MediaRecorderException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun <R> MediaRecorder.use(
        @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
        block: suspend (MediaRecorder) -> R
): R = coroutineScope {
    try {
        val completion = async(coroutineContext, start = CoroutineStart.LAZY) {
            block(this@use)
        }
        setOnErrorListener { _, what, extra ->
            val e = MediaRecorderException(what, extra)
            completion.cancel(e)
        }
        completion.await()
    } finally {
        release()
    }
}
