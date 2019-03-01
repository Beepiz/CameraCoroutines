package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.beepiz.cameracoroutines.exceptions.CamStateException
import com.beepiz.cameracoroutines.extensions.requireHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@RequiresApi(21)
@Deprecated("Use withOpenCamera", ReplaceWith(
    "this.withOpenCamera(cameraId, block)",
    "com.beepiz.cameracoroutines.withOpenCamera"
), level = DeprecationLevel.ERROR)
@RequiresPermission(Manifest.permission.CAMERA)
suspend fun <R> CameraManager.openAndUseCamera(
    cameraId: String,
    block: suspend (CameraDevice) -> R
): R = coroutineScope {
    val openedCamera = CompletableDeferred<CameraDevice>()
    val completion: Deferred<R> = async(start = CoroutineStart.LAZY) {
        openedCamera.await().use { camera ->
            block(camera)
        }
    }
    val closed = CompletableDeferred<Unit>()
    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            openedCamera.complete(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            completion.cancel(CamStateException(CamDevice.State.Disconnected))
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            completion.cancel(CamStateException(CamDevice.State.Error(error)))
        }

        override fun onClosed(camera: CameraDevice) {
            closed.complete(Unit)
        }
    }
    openCamera(cameraId, stateCallback, coroutineContext.requireHandler())
    try {
        completion.await()
    } finally {
        closed.await()
    }
}

@RequiresApi(21)
@RequiresPermission(Manifest.permission.CAMERA)
suspend fun <R> CameraManager.withOpenCamera(
    cameraId: String,
    block: suspend CoroutineScope.(CameraDevice) -> R
): R = coroutineScope {
    val closedAsync = CompletableDeferred<Unit>()
    val openedCameraAsync = CompletableDeferred<CameraDevice>(coroutineContext[Job])
    val completionAsync = CompletableDeferred<Unit>(coroutineContext[Job])
    val cameraUsageAsync: Deferred<R> = async(start = CoroutineStart.LAZY) {
        openedCameraAsync.await().use { cameraDevice ->
            block(cameraDevice)
        }
    }
    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            openedCameraAsync.complete(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            completionAsync.completeExceptionally(CamStateException(CamDevice.State.Disconnected))
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            completionAsync.completeExceptionally(CamStateException(CamDevice.State.Error(error)))
            camera.close()
        }

        override fun onClosed(camera: CameraDevice) {
            closedAsync.complete(Unit)
        }
    }
    openCamera(cameraId, stateCallback, coroutineContext.requireHandler())
    try {
        cameraUsageAsync.await()
    } finally {
        closedAsync.await()
    }
}
