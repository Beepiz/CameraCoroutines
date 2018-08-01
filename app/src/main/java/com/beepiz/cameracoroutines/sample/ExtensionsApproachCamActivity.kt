package com.beepiz.cameracoroutines.sample

import android.arch.lifecycle.Lifecycle.Event
import android.hardware.camera2.CameraAccessException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.exceptions.CamStateException
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics
import com.beepiz.cameracoroutines.sample.extensions.coroutines.createJob
import com.beepiz.cameracoroutines.sample.extensions.media.recordVideo
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import splitties.exceptions.illegal
import splitties.toast.longToast
import splitties.toast.toast
import splitties.viewdsl.appcompat.textView
import splitties.viewdsl.core.add
import splitties.viewdsl.core.lParams
import splitties.viewdsl.core.v
import splitties.viewdsl.core.verticalLayout
import splitties.views.gravityCenterHorizontal
import timber.log.Timber

class ExtensionsApproachCamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(v(::verticalLayout) {
            add(v(::textView) {
                text = "Extensions approach!"
            }, lParams(gravity = gravityCenterHorizontal))
        })
    }

    override fun onStart() {
        super.onStart()
        val tillOnStop = lifecycle.createJob(Event.ON_STOP)
        launch(UI.immediate, parent = tillOnStop) {
            try {
                val externalFilesDir = getExternalFilesDir(null).absolutePath
                val videoPath = "$externalFilesDir/ExtensionsApproachVideoRecord.mp4"
                recordVideo(CamCharacteristics.LensFacing.BACK, videoPath) {
                    toast("Recording…")
                    delay(6000)
                    longToast("Recording succeeded!")
                }
            } catch (e: SecurityException) {
                Timber.e(e)
                longToast("Missing permission!")
            } catch (e: Exception) {
                Timber.e(e)
                when (e) {
                    is CameraAccessException -> longToast("Couldn't access the camera")
                    is CamStateException -> {
                        val state = e.nonOpenState
                        longToast(when (state) {
                            is CamDevice.State.Error -> state.errorString()
                            CamDevice.State.Disconnected -> "Disconnected"
                            CamDevice.State.Closed -> "Closed"
                            CamDevice.State.Opened -> illegal()
                        })
                    }
                    is CancellationException -> toast("Cancelled normally")
                    else -> longToast("Unknown error (${e.message})")
                }
            }
        }
    }
}
