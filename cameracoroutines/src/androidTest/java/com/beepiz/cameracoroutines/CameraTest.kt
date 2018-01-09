package com.beepiz.cameracoroutines

import android.hardware.camera2.CameraCharacteristics.LENS_FACING
import android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import com.beepiz.cameracoroutines.extensions.cameraManager
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun testCamera() {
    val camManager = cameraManager
    val backCamId = camManager.cameraIdList.firstOrNull {
        val characteristics = camManager.getCameraCharacteristics(it)
        characteristics[LENS_FACING] == LENS_FACING_BACK
    } ?: throw NoSuchElementException("No back camera found")

}

private suspend fun CameraManager.openCam(camId: String) = suspendCoroutine<CameraDevice> {
    val camCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            TODO()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            TODO()
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            TODO()
        }
    }
    openCamera(camId, camCallback, null)
    //TODO: See if CameraDevice.StateCallback is automatically unregistered after error or disconnection.
    //TODO: Write a camera wrapper in GattConnection style
}
