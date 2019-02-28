package com.beepiz.cameracoroutines.exceptions

import com.beepiz.cameracoroutines.CamCaptureSession
import com.beepiz.cameracoroutines.CamDevice

sealed class CamException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class CamStateException(val nonOpenState: CamDevice.State) : CamException() {

    init {
        require(nonOpenState != CamDevice.State.Opened)
    }

    override val message = "$nonOpenState"
}

class CamCaptureSessionStateException(val state: CamCaptureSession.State.Closed) : CamException() {
    override val message = "$state"
}
