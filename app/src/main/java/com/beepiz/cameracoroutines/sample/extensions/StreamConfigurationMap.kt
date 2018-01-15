package com.beepiz.cameracoroutines.sample.extensions

import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Size

inline fun <reified T> StreamConfigurationMap.outputSizes(): Array<Size> {
    return getOutputSizes(T::class.java)!!
}
