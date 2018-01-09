package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.support.annotation.RequiresPermission
import android.view.Surface
import com.beepiz.cameracoroutines.extensions.cameraManager
import timber.log.Timber
import kotlin.coroutines.experimental.suspendCoroutine

class CamDevice
@RequiresPermission(Manifest.permission.CAMERA)
constructor(camId: String, handler: Handler? = null) {

    private val camManager = cameraManager
    private val camStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Timber.v("onOpened($camera)")
            return
            camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            TODO()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Timber.v("onDisconnected($camera)")
            camera.close()
            return
            TODO()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Timber.v("onError($camera, $error)")
            camera.close()
            return
            TODO()
        }

        override fun onClosed(camera: CameraDevice) {
            Timber.v("onClosed($camera)")
        }
    }

    init {
        camManager.openCamera(camId, camStateCallback, handler)
    }
}

private suspend fun CameraDevice.createCaptureSession(outputs: List<Surface>, handler: Handler? = null) = suspendCoroutine<CameraCaptureSession> {
    val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            it.resume(session)
        }

        override fun onConfigureFailed(session: CameraCaptureSession?) {
            it.resumeWithException(Error())
        }
    }
    createCaptureSession(outputs, sessionStateCallback, handler)
}
