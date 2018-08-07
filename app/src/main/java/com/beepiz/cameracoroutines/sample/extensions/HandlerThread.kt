package com.beepiz.cameracoroutines.sample.extensions

import android.os.Handler
import android.os.HandlerThread
import com.beepiz.cameracoroutines.extensions.HandlerElement
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.withContext

suspend fun <R> HandlerThread.useWithHandlerInContext(
        quitSafely: Boolean = true,
        block: suspend () -> R
): R = try {
    start()
    val handler = Handler(looper)
    withContext(HandlerContext(handler) + HandlerElement(handler), block = block)
} finally {
    if (quitSafely) quitSafely() else quit()
}
