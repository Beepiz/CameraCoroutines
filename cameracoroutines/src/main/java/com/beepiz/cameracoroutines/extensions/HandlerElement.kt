package com.beepiz.cameracoroutines.extensions

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import splitties.exceptions.illegal
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class HandlerElement(val handler: Handler) : AbstractCoroutineContextElement(Key) {
    @Suppress("NOTHING_TO_INLINE")
    companion object Key : CoroutineContext.Key<HandlerElement> {
        inline operator fun invoke(looper: Looper) = HandlerElement(Handler(looper))
        inline operator fun invoke(thread: HandlerThread) = HandlerElement(Handler(thread.looper))
    }
}

fun CoroutineContext.requireHandler(): Handler = this[HandlerElement]?.handler
        ?: illegal("Required HandlerElement not found in the coroutineContext")
