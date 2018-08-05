package com.beepiz.cameracoroutines.sample.extensions

import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.withContext

inline fun <R> HandlerThread.useHandler(
        quitSafely: Boolean = true,
        block: (Handler) -> R
) = try {
    start()
    block(Handler(looper))
} finally {
    if (quitSafely) quitSafely() else quit()
}

@Deprecated("Don't use this method as it leads to compile errors or crashes")
suspend inline fun <R> HandlerThread.useHandlerWithContext(
        quitSafely: Boolean = true,
        crossinline block: suspend (Handler) -> R
): R = try {
    start()
    val handler = Handler(looper)
    withContext(HandlerContext(handler)) {
        block(handler)
    }
} finally {
    if (quitSafely) quitSafely() else quit()
}
