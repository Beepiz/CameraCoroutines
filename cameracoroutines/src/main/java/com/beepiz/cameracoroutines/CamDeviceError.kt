package com.beepiz.cameracoroutines

import android.hardware.camera2.CameraDevice

class CamDeviceException(val errorCode: Int) {

    companion object {
        fun humanReadableErrorCode(errorCode: Int) = when(errorCode) {
            CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> "ERROR_CAMERA_IN_USE"
            CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> "ERROR_MAX_CAMERAS_IN_USE"
            CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> "ERROR_CAMERA_DISABLED"
            CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> "ERROR_CAMERA_DEVICE"
            CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> "ERROR_CAMERA_SERVICE"
            else -> "$errorCode"
        }
    }
}
