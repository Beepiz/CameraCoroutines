package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.support.annotation.RequiresPermission
import com.beepiz.cameracoroutines.exceptions.CamStateException
import com.beepiz.cameracoroutines.extensions.requireHandler
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.coroutineContext

@RequiresPermission(Manifest.permission.CAMERA)
suspend fun <R> CameraManager.openAndUseCamera(
        cameraId: String,
        block: suspend (CameraDevice) -> R
): R {
    val context = coroutineContext
    val openedCamera = CompletableDeferred<CameraDevice>()
    val completion: Deferred<R> = async(context, start = CoroutineStart.LAZY) {
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
    openCamera(cameraId, stateCallback, context.requireHandler())
    return try {
        completion.await()
    } finally {
        closed.await()
    }
}
