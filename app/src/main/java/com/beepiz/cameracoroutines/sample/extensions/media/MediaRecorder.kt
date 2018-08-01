package com.beepiz.cameracoroutines.sample.extensions.media

import android.media.MediaRecorder
import com.beepiz.cameracoroutines.sample.recording.MediaRecorderException
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.coroutineContext

suspend fun <R> MediaRecorder.use(
        @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
        block: suspend (MediaRecorder) -> R
): R = try {
    val completion: Deferred<R> = async(coroutineContext, start = CoroutineStart.LAZY) {
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
