package com.beepiz.cameracoroutines.sample

import android.hardware.camera2.CameraAccessException
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.beepiz.cameracoroutines.CamDevice
import com.beepiz.cameracoroutines.exceptions.CamStateException
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics.LensFacing.BACK
import com.beepiz.cameracoroutines.sample.extensions.coroutines.awaitState
import com.beepiz.cameracoroutines.sample.extensions.coroutines.coroutineScope
import com.beepiz.cameracoroutines.sample.extensions.coroutines.createScope
import com.beepiz.cameracoroutines.sample.extensions.media.recordVideo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.toast.longToast
import splitties.toast.toast
import splitties.views.dsl.core.add
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.styles.AndroidStyles
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.gravityCenterHorizontal
import timber.log.Timber
import java.io.IOException

class ExtensionsApproachCamActivity : AppCompatActivity() {

    private val androidStyles = AndroidStyles(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(verticalLayout {
            add(textView {
                text = "Extensions approach!"
            }, lParams(gravity = gravityCenterHorizontal))
            add(androidStyles.progressBar.default(), lParams(gravity = gravityCenterHorizontal))
        })
    }

    override fun onStart() {
        super.onStart()
        lifecycle.coroutineScope.launch {
            lifecycle.awaitState(Lifecycle.State.STARTED)
            lifecycle.createScope(activeWhile = Lifecycle.State.STARTED).launch {
                recordSampleVideo()
            }
        }
    }

    private suspend fun recordSampleVideo() {
        try {
            withCamContext {
                val externalFilesDir = getExternalFilesDir(null)?.absolutePath
                        ?: throw IOException("External storage unavailable")
                val videoPath = "$externalFilesDir/ExtensionsApproachVideoRecord.mp4"
                recordVideo(lensFacing = BACK, outputPath = videoPath) {
                    withContext(Dispatchers.Main) {
                        toast("Recording…")
                        delay(6000)
                        longToast("Recording succeeded!")
                    }
                }
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
                        CamDevice.State.Opened -> throw IllegalStateException()
                    })
                }
                is CancellationException -> toast("Cancelled normally")
                else -> longToast("Unknown error (${e.message})")
            }
        }
    }
}
