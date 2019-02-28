package com.beepiz.cameracoroutines

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.Surface
import com.beepiz.cameracoroutines.extensions.requireHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.coroutineContext

internal typealias CCS = CameraCaptureSession

suspend fun CameraDevice.createAndUseSession(
    outputs: List<Surface>,
    block: suspend (CamCaptureSession) -> Unit
) {
    val handler = coroutineContext.requireHandler()
    CamCaptureSession(this, handler).use {
        createCaptureSession(outputs, it.sessionStateCallback, handler)
        block(it)
    }
}

@ExperimentalCoroutinesApi
suspend fun <R> CameraDevice.withSession(
    outputs: List<Surface>,
    block: suspend (CamCaptureSession) -> R
): R {
    val handler = coroutineContext.requireHandler()
    return CamCaptureSession(this@withSession, handler).use {
        createCaptureSession(outputs, it.sessionStateCallback, handler)
        block(it)
    }
}
