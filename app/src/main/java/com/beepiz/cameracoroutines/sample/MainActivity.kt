package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import com.beepiz.cameracoroutines.sample.extensions.onClick
import com.beepiz.cameracoroutines.sample.viewdsl.add
import com.beepiz.cameracoroutines.sample.viewdsl.button
import com.beepiz.cameracoroutines.sample.viewdsl.contentView
import com.beepiz.cameracoroutines.sample.viewdsl.lParams
import com.beepiz.cameracoroutines.sample.viewdsl.v
import com.beepiz.cameracoroutines.sample.viewdsl.verticalLayout

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = v(::verticalLayout) {
            val lp = lParams(gravity = Gravity.CENTER_HORIZONTAL)
            if (SDK_INT >= M) add(::button, lp) {
                text = "Request camera permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
                }
            }
            if (SDK_INT >= M) add(::button, lp) {
                text = "Request microphone permission"
                onClick {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                }
            }
            add(::button, lp) {
                text = "Open Cam test"
                onClick {
                    startActivity(Intent(this@MainActivity, CamTestActivity::class.java))
                }
            }
        }
    }
}
