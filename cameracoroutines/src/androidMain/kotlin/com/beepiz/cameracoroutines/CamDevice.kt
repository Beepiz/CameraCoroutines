package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.os.Handler
import androidx.annotation.RequiresPermission
import android.view.Surface
import com.beepiz.cameracoroutines.exceptions.CamStateException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import splitties.systemservices.cameraManager
import timber.log.Timber

class CamDevice
@Deprecated("Use withOpenCamera instead of instantiating this class.")
@RequiresPermission(Manifest.permission.CAMERA) constructor(
        private val camId: String,
        private val handler: Handler? = null) : AutoCloseable {

    sealed class State {
        object Opened : State()
        class Error(val errorCode: Int) : State() {
            fun errorString(): String = when (errorCode) {
                StateCallback.ERROR_CAMERA_IN_USE -> "ERROR_CAMERA_IN_USE"
                StateCallback.ERROR_MAX_CAMERAS_IN_USE -> "ERROR_MAX_CAMERAS_IN_USE"
                StateCallback.ERROR_CAMERA_DISABLED -> "ERROR_CAMERA_DISABLED"
                StateCallback.ERROR_CAMERA_DEVICE -> "ERROR_CAMERA_DEVICE"
                StateCallback.ERROR_CAMERA_SERVICE -> "ERROR_CAMERA_SERVICE"
                else -> "Unknown error state: $errorCode"
            }

            override fun toString() = errorString()
        }

        object Disconnected : State()
        object Closed : State()
    }

    enum class Template {
        Preview, StillCapture, Record, VideoSnapshot, ZeroShutterLag, Manual
    }

    private val camManager = cameraManager

    private val camState = Channel<State>(Channel.CONFLATED)
    private var cam: CameraDevice? = null
    private val camOrThrow: CameraDevice
        get() = cam ?: throw IllegalStateException("Camera not opened!")
    private var closed = false

    @Deprecated("Use withOpenCamera instead of instantiating this class.")
    private val camStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Timber.v("onOpened($camera)")
            cam = camera
            if (closed) camera.close()
            else camState.offer(State.Opened)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Timber.v("onDisconnected($camera)")
            camState.offer(State.Disconnected)
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Timber.v("onError($camera, $error)")
            camState.offer(State.Error(error))
            camera.close()
        }

        override fun onClosed(camera: CameraDevice) {
            Timber.v("onClosed($camera)")
            camState.offer(State.Closed)
        }
    }

    @Deprecated("Use withOpenCamera instead of instantiating this class.")
    @RequiresPermission(Manifest.permission.CAMERA)
    suspend fun open() {
        camManager.openCamera(camId, camStateCallback, handler)
        val state = camState.receive()
        when (state) {
            State.Opened -> return
            else -> throw CamStateException(state)
        }
    }

    @Deprecated("Use withOpenCamera instead of instantiating this class.")
    @ExperimentalCoroutinesApi
    fun createCaptureSession(outputs: List<Surface>): CamCaptureSession {
        return CamCaptureSession(camOrThrow, handler).also {
            camOrThrow.createCaptureSession(outputs, it.sessionStateCallback, handler)
        }
    }

    override fun close() {
        closed = true
        cam?.close()
    }
}
