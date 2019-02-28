package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import com.beepiz.cameracoroutines.sample.doublecam.DoubleCamTestActivity
import splitties.activities.start
import splitties.views.dsl.core.*
import splitties.views.onClick

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = verticalLayout {
            val lp = lParams(gravity = Gravity.CENTER_HORIZONTAL)
            if (SDK_INT >= M) add(button {
                text = "Request camera permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
                }
            }, lp)
            if (SDK_INT >= M) add(button {
                text = "Request microphone permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                }
            }, lp)
            add(button {
                text = "Open Cam test"
                onClick { start<CamTestActivity>() }
            }, lp)
            add(button {
                text = "Double cam test"
                onClick { start<DoubleCamTestActivity>() }
            }, lp)
            add(button {
                text = "Launch extensions approach Activity"
                onClick { start<ExtensionsApproachCamActivity>() }
            }, lp)
        }
    }
}
