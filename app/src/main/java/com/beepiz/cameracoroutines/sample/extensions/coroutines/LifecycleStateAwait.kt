package com.beepiz.cameracoroutines.sample.extensions.coroutines

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun Lifecycle.awaitState(state: Lifecycle.State) {
    require(state != Lifecycle.State.DESTROYED) {
        "DESTROYED is a terminal state that is forbidden for awaitState(…), to avoid leaks."
    }
    if (currentState.isAtLeast(state)) return // Fast path
    suspendCancellableCoroutine<Unit> { c ->
        if (currentState == Lifecycle.State.DESTROYED) { // Fast path to cancellation
            c.cancel()
            return@suspendCancellableCoroutine
        }
        val observer = object : GenericLifecycleObserver {
            override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event) {
                if (currentState.isAtLeast(state)) {
                    removeObserver(this)
                    c.resume(Unit)
                } else if (currentState == Lifecycle.State.DESTROYED) {
                    c.cancel()
                }
            }
        }
        addObserver(observer)
        c.invokeOnCancellation { removeObserver(observer) }
    }
}
