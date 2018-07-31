package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.support.annotation.RequiresPermission
import com.beepiz.cameracoroutines.exceptions.CamStateException
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.coroutineContext

@RequiresPermission(Manifest.permission.CAMERA)
suspend fun CameraManager.openAndUseCamera(
        cameraId: String,
        handler: Handler? = null,
        block: suspend (CameraDevice) -> Unit
) {
    val context = if (handler == null) coroutineContext else coroutineContext + HandlerContext(handler).immediate
    var job: Job? = null
    val completion = CompletableDeferred<Unit>()
    val closed = CompletableDeferred<Unit>()
    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            job = launch(context) {
                try {
                    camera.use { camera ->
                        yield()
                        block(camera)
                        completion.complete(Unit)
                    }
                } catch (t: Throwable) {
                    completion.completeExceptionally(t)
                }
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            job?.cancel(CamStateException(CamDevice.State.Disconnected))
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            job?.cancel(CamStateException(CamDevice.State.Error(error)))
        }

        override fun onClosed(camera: CameraDevice) {
            closed.complete(Unit)
        }
    }
    openCamera(cameraId, stateCallback, handler)
    try {
        completion.await()
    } finally {
        closed.await()
    }
}
