package com.beepiz.cameracoroutines

import android.annotation.TargetApi
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.TEMPLATE_MANUAL
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.hardware.camera2.CameraDevice.TEMPLATE_RECORD
import android.hardware.camera2.CameraDevice.TEMPLATE_STILL_CAPTURE
import android.hardware.camera2.CameraDevice.TEMPLATE_VIDEO_SNAPSHOT
import android.hardware.camera2.CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.support.annotation.RequiresApi
import android.view.Surface
import com.beepiz.cameracoroutines.CamDevice.Template.Manual
import com.beepiz.cameracoroutines.CamDevice.Template.Preview
import com.beepiz.cameracoroutines.CamDevice.Template.Record
import com.beepiz.cameracoroutines.CamDevice.Template.StillCapture
import com.beepiz.cameracoroutines.CamDevice.Template.VideoSnapshot
import com.beepiz.cameracoroutines.CamDevice.Template.ZeroShutterLag
import com.beepiz.cameracoroutines.exceptions.CamCaptureSessionStateException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

@ExperimentalCoroutinesApi
class CamCaptureSession internal constructor(
    private val cameraDevice: CameraDevice,
    private val handler: Handler?
) : AutoCloseable {

    sealed class State {
        sealed class Configured() : State() {
            companion object : Configured()
            sealed class InputQueueEmpty : Configured() {
                @TargetApi(26)
                companion object : InputQueueEmpty()

                object Ready : InputQueueEmpty()
            }

            object Active : Configured()
        }

        sealed class Closed : State() {
            companion object : Closed()
            object ConfigureFailed : Closed()
        }
    }

    private val stateBroadcastChannel = BroadcastChannel<State>(capacity = Channel.CONFLATED)
    val stateChannel get() = stateBroadcastChannel.openSubscription()
    private val preparedSurfaceChannel = Channel<Surface>()

    private var captureSession: CameraCaptureSession? = null

    @RequiresApi(23)
    suspend fun prepareSurface(surface: Surface) {
        captureSession?.prepare(surface) ?: noSessionException
        check(preparedSurfaceChannel.receive() === surface)
    }

    internal val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CCS) = stateCallback(session, State.Configured)
        override fun onCaptureQueueEmpty(session: CCS) =
            stateCallback(session, State.Configured.InputQueueEmpty)

        override fun onReady(session: CCS) =
            stateCallback(session, State.Configured.InputQueueEmpty.Ready)

        override fun onActive(session: CCS) = stateCallback(session, State.Configured.Active)

        override fun onSurfacePrepared(session: CCS, surface: Surface) {
            runCatching { preparedSurfaceChannel.offer(surface) }
        }

        override fun onConfigureFailed(session: CCS) =
            stateCallback(session, State.Closed.ConfigureFailed)

        override fun onClosed(session: CCS) {
            stateCallback(session, State.Closed)
            stateBroadcastChannel.close()
        }
    }

    /**
     * Resumes on any [CamCaptureSession.State.Configured] state, and throws a
     * [CamCaptureSessionStateException] if a [CamCaptureSession.State.Closed] is received instead.
     */
    @Throws(CamCaptureSessionStateException::class)
    suspend fun awaitConfiguredState() {
        stateChannel.consumeEach { state ->
            when (state) {
                is State.Configured -> return
                is State.Closed -> throw CamCaptureSessionStateException(state)
            }
        }
    }

    inline fun createCaptureRequest(
        template: CamDevice.Template,
        block: (CaptureRequest.Builder) -> Unit
    ): CaptureRequest = createCaptureRequestBuilder(template).also(block).build()

    @PublishedApi
    internal fun createCaptureRequestBuilder(template: CamDevice.Template): CaptureRequest.Builder {
        return when (template) {
            Preview -> TEMPLATE_PREVIEW
            StillCapture -> TEMPLATE_STILL_CAPTURE
            Record -> TEMPLATE_RECORD
            VideoSnapshot -> TEMPLATE_VIDEO_SNAPSHOT
            ZeroShutterLag -> TEMPLATE_ZERO_SHUTTER_LAG
            Manual -> TEMPLATE_MANUAL
        }.let { cameraDevice.createCaptureRequest(it) }
    }

    fun setRepeatingRequest(
        request: CaptureRequest,
        captureCallback: CameraCaptureSession.CaptureCallback? = null
    ) = captureSession?.setRepeatingRequest(request, captureCallback, handler) ?: noSessionException

    fun stopRepeating() = captureSession?.stopRepeating() ?: noSessionException

    private fun stateCallback(session: CCS, newState: State) {
        check(session.device == cameraDevice) {
            "The same callback has been used for different cameras! Expected: $cameraDevice but " +
                    "got :${session.device}"
        }
        captureSession = session
        runCatching { stateBroadcastChannel.offer(newState) }
    }

    private val noSessionException: Nothing get() = error("No capture session!")

    override fun close() {
        captureSession?.close()
        preparedSurfaceChannel.close()
    }
}
