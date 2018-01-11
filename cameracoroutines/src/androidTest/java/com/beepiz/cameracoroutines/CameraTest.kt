package com.beepiz.cameracoroutines

import android.hardware.camera2.CameraCharacteristics.LENS_FACING
import android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
import com.beepiz.cameracoroutines.extensions.cameraManager

suspend fun testCamera() {
    val camManager = cameraManager
    val backCamId = camManager.cameraIdList.firstOrNull {
        val characteristics = camManager.getCameraCharacteristics(it)
        characteristics[LENS_FACING] == LENS_FACING_BACK
    } ?: throw NoSuchElementException("No back camera found")

}
