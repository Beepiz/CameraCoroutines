package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresPermission
import android.support.v7.app.AppCompatActivity
import com.beepiz.cameracoroutines.exceptions.CamException
import com.beepiz.cameracoroutines.sample.autorecorder.recordVideo
import com.beepiz.cameracoroutines.sample.viewdsl.setContentView
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class CamTestActivity : AppCompatActivity() {

    private lateinit var testJob: Job

    private val camThread by kotlin.lazy { HandlerThread("camera") }

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
    private fun testCamera() = launch(UI) {
        try {
            val camHandler = Handler(camThread.looper)
            val externalFilesDir = getExternalFilesDir(null).absolutePath
            val backRecording = async(parent = coroutineContext[Job]) {
                val backVideoPath = "$externalFilesDir/BackRecorded.mp4"
                recordVideo(frontCamera = false, outputPath = backVideoPath, bgHandler = camHandler)
            }
            backRecording.await()
            val frontRecording = async(parent = coroutineContext[Job]) {
                val frontVideoPath = "$externalFilesDir/FrontRecorded.mp4"
                recordVideo(frontCamera = true, outputPath = frontVideoPath, bgHandler = camHandler)
            }
            frontRecording.await()
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
