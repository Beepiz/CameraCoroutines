package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.os.Bundle
import android.os.HandlerThread
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.extensions.HandlerElement
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics
import com.beepiz.cameracoroutines.sample.extensions.media.recordVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.views.dsl.core.setContentView
import timber.log.Timber

@RequiresApi(21)
class CamTestActivity : AppCompatActivity() {

    private lateinit var testJob: Job

    private val camThread by lazy { HandlerThread("camera") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ui = CamTestUi(this)
        setContentView(ui)
        camThread.start()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        testJob = testCamera()
    }

    override fun onStop() {
        super.onStop()
        testJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        camThread.quitSafely()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun testCamera() = GlobalScope.launch(Dispatchers.Main.immediate + HandlerElement(camThread)) {
        try {
            val externalFilesDir = getExternalFilesDir(null).absolutePath
            val backVideoPath = "$externalFilesDir/BackRecorded.mp4"
            val frontVideoPath = "$externalFilesDir/FrontRecorded.mp4"
            recordVideo(CamCharacteristics.LensFacing.Back, backVideoPath) {
                delay(10_000)
            }
            recordVideo(CamCharacteristics.LensFacing.Front, frontVideoPath) {
                delay(10_000)
            }
        } catch (e: CameraAccessException) {
            Timber.e(e)
        } catch (e: CamException) {
            Timber.e(e)
        } catch (e: Exception) {
            Timber.e(e)
        }
        finish()
    }
}
