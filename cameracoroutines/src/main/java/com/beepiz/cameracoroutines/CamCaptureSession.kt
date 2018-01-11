package com.beepiz.cameracoroutines

import android.annotation.TargetApi
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Build.VERSION_CODES.O
import android.os.Handler
import android.view.Surface
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedChannel

private typealias CCS = CameraCaptureSession

class CamCaptureSession internal constructor(
        private val cameraDevice: CameraDevice, handler: Handler?
) {

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

    private val _stateChannel = ConflatedChannel<State>()
    private val _preparedSurfaceChannel = Channel<Surface>()

    private lateinit var captureSession: CameraCaptureSession

    internal val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CCS) = stateCallback(session, State.Configured)
        override fun onConfigureFailed(session: CCS) = stateCallback(session, State.Closed.ConfigureFailed)
        override fun onReady(session: CCS) = stateCallback(session, State.Configured.InputQueueEmpty.Ready)
        override fun onActive(session: CCS) = stateCallback(session, State.Configured.Active)
        override fun onCaptureQueueEmpty(session: CCS) = stateCallback(session, State.Configured.InputQueueEmpty)

        override fun onSurfacePrepared(session: CCS, surface: Surface) {
            _preparedSurfaceChannel.offer(surface)
        }

        override fun onClosed(session: CCS) = stateCallback(session, State.Closed)
    }

    private fun stateCallback(session: CameraCaptureSession, newState: State) {
        check(session.device == cameraDevice) {
            "The same callback has been used for different cameras! Expected: $cameraDevice but " +
                    "got :${session.device}"
        }
        captureSession = session
        _stateChannel.offer(newState)
    }
}
