package com.beepiz.cameracoroutines.sample

import android.os.HandlerThread
import com.beepiz.cameracoroutines.sample.extensions.useHandlerWithContext
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import org.junit.Test
import splitties.toast.toast

class SuspendCrashTest {

    @Test
    fun crashTestSuspendCrossinline() = runBlocking {
        HandlerThread("crash test").useHandlerWithContext {
            withContext(UI) {
                toast("Yo")
            }
        }
    }
}
