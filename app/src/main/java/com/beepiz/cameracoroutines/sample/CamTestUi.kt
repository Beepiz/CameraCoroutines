package com.beepiz.cameracoroutines.sample

import android.app.Activity
import android.view.Gravity
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.styles.AndroidStyles

class CamTestUi(override val ctx: Activity) : Ui {

    private val androidStyles = AndroidStyles(ctx)

    override val root = frameLayout {
        add(androidStyles.progressBar.default(), lParams(gravity = Gravity.CENTER))
    }
}
