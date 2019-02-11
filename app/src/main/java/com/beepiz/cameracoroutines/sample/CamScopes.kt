package com.beepiz.cameracoroutines.sample

import android.os.Handler
import android.os.HandlerThread
import com.beepiz.cameracoroutines.extensions.HandlerElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun withCamContext(block: suspend CoroutineScope.() -> Unit) {
    val handlerThread = HandlerThread("cam")
    try {
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        @Suppress("DEPRECATION")
        withContext(handler.asCoroutineDispatcher() + HandlerElement(handler), block)
    } finally {
        handlerThread.quitSafely()
    }
}
