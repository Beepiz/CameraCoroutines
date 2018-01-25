package com.beepiz.cameracoroutines.sample.extensions

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.hardware.camera2.CameraCharacteristics
import android.os.Build.VERSION_CODES.M

object CamCharacteristics {
    enum class LensFacing {
        FRONT, BACK, @TargetApi(M) EXTERNAL;

        val intValue: Int
            @SuppressLint("InlinedApi") get() = when (this) {
                FRONT -> CameraCharacteristics.LENS_FACING_FRONT
                BACK -> CameraCharacteristics.LENS_FACING_BACK
                EXTERNAL -> CameraCharacteristics.LENS_FACING_EXTERNAL
            }
    }
}
