package com.beepiz.cameracoroutines.sample

import android.os.Handler
import android.os.HandlerThread
import com.beepiz.cameracoroutines.extensions.HandlerElement
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.withContext

suspend fun withCamContext(block: suspend CoroutineScope.() -> Unit) {
    val handlerThread = HandlerThread("cam")
    try {
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        @Suppress("DEPRECATION")
        withContext(HandlerContext(handler) + HandlerElement(handler), block)
    } finally {
        handlerThread.quitSafely()
    }
}
