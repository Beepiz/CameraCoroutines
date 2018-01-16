package com.beepiz.cameracoroutines.sample

import android.view.SurfaceHolder
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

class SurfaceHolderState(holder: SurfaceHolder) {

    val createdChannel = ConflatedBroadcastChannel(holder.surface?.isValid ?: false)
    val isCreated: Boolean inline get() = createdChannel.value

    suspend fun awaitCreated() = createdChannel.consumeEach { created ->
        if (created) return
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder?) {
            createdChannel.offer(true)
        }

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            createdChannel.offer(false)
        }
    }

    init {
        holder.addCallback(surfaceCallback)
    }
}
