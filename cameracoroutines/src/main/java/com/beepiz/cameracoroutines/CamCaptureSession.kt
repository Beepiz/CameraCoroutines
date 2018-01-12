package com.beepiz.cameracoroutines

import android.annotation.TargetApi
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.*
import android.hardware.camera2.CaptureRequest
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION_CODES.O
import android.os.Handler
import android.support.annotation.RequiresApi
import android.view.Surface
import com.beepiz.cameracoroutines.CamDevice.Template.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

private typealias CCS = CameraCaptureSession

class CamCaptureSession internal constructor(
        private val cameraDevice: CameraDevice,
        private val handler: Handler?
) : AutoCloseable {

    sealed class State {
        sealed class Configured() : State() {
            companion object : Configured()
            sealed class InputQueueEmpty : Configured() {
                @TargetApi(O) companion object : InputQueueEmpty()
                object Ready : InputQueueEmpty()
            }

            object Active : Configured()
        }

        sealed class Closed : State() {
            companion object : Closed()
            object ConfigureFailed : Closed()
        }
    }

    val stateChannel = ConflatedBroadcastChannel<State>()
    private val preparedSurfaceChannel = Channel<Surface>()

    private var captureSession: CameraCaptureSession? = null

    @RequiresApi(M) suspend fun prepareSurface(surface: Surface) {
        captureSession?.prepare(surface) ?: noSessionException
        check(preparedSurfaceChannel.receive() === surface)
    }

    internal val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CCS) = stateCallback(session, State.Configured)
        override fun onCaptureQueueEmpty(session: CCS) = stateCallback(session, State.Configured.InputQueueEmpty)
        override fun onReady(session: CCS) = stateCallback(session, State.Configured.InputQueueEmpty.Ready)
        override fun onActive(session: CCS) = stateCallback(session, State.Configured.Active)

        override fun onSurfacePrepared(session: CCS, surface: Surface) {
            preparedSurfaceChannel.offer(surface)
        }

        override fun onConfigureFailed(session: CCS) = stateCallback(session, State.Closed.ConfigureFailed)
        override fun onClosed(session: CCS) = stateCallback(session, State.Closed)
    }

    inline fun createCaptureRequest(
            template: CamDevice.Template,
            block: (CaptureRequest.Builder) -> Unit
    ): CaptureRequest = createCaptureRequestBuilder(template).also(block).build()

    @PublishedApi
    internal fun createCaptureRequestBuilder(template: CamDevice.Template): CaptureRequest.Builder {
        return cameraDevice.createCaptureRequest(when (template) {
            PREVIEW -> TEMPLATE_PREVIEW
            STILL_CAPTURE -> TEMPLATE_STILL_CAPTURE
            RECORD -> TEMPLATE_RECORD
            VIDEO_SNAPSHOT -> TEMPLATE_VIDEO_SNAPSHOT
            ZERO_SHUTTER_LAG -> TEMPLATE_ZERO_SHUTTER_LAG
            MANUAL -> TEMPLATE_MANUAL
        })
    }

    fun setRepeatingRequest(
            request: CaptureRequest,
            captureCallback: CameraCaptureSession.CaptureCallback? = null
    ) = captureSession?.setRepeatingRequest(request, captureCallback, handler) ?: noSessionException

    private fun stateCallback(session: CCS, newState: State) {
        check(session.device == cameraDevice) {
            "The same callback has been used for different cameras! Expected: $cameraDevice but " +
                    "got :${session.device}"
        }
        captureSession = session
        stateChannel.offer(newState)
    }

    private val noSessionException: Nothing get() = throw IllegalStateException("No capture session!")

    override fun close() {
        captureSession?.close()
        preparedSurfaceChannel.close()
        stateChannel.close()
    }
}
