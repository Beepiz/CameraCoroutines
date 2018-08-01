package com.beepiz.cameracoroutines

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.view.Surface

internal typealias CCS = CameraCaptureSession

suspend fun CameraDevice.createAndUseSession(
        outputs: List<Surface>,
        handler: Handler? = null,
        block: suspend (CamCaptureSession) -> Unit
) = CamCaptureSession(this, handler).use {
    createCaptureSession(outputs, it.sessionStateCallback, handler)
    block(it)
}
