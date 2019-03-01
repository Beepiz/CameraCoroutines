package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.beepiz.cameracoroutines.sample.doublecam.DoubleCamTestActivity
import splitties.activities.start
import splitties.views.dsl.core.add
import splitties.views.dsl.core.button
import splitties.views.dsl.core.contentView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.verticalLayout
import splitties.views.onClick

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = verticalLayout {
            val lp = lParams(gravity = Gravity.CENTER_HORIZONTAL)
            if (SDK_INT >= 23) add(button {
                text = "Request camera permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
                }
            }, lp)
            if (SDK_INT >= 23) add(button {
                text = "Request microphone permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                }
            }, lp)
            if (SDK_INT >= 21) add(button {
                text = "Open Cam test"
                onClick { start<CamTestActivity>() }
            }, lp)
            if (SDK_INT >= 21) add(button {
                text = "Double cam test"
                onClick { start<DoubleCamTestActivity>() }
            }, lp)
            if (SDK_INT >= 21) add(button {
                text = "Launch extensions approach Activity"
                onClick { start<ExtensionsApproachCamActivity>() }
            }, lp)
        }
    }
}
