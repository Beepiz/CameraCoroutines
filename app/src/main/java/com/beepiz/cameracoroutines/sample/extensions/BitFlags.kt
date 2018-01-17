@file:Suppress("NOTHING_TO_INLINE")

package com.beepiz.cameracoroutines.sample.extensions

import android.support.annotation.CheckResult
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

@CheckResult inline fun Long.hasFlag(flag: Long) = flag and this == flag
@CheckResult inline fun Long.withFlag(flag: Long) = this or flag
@CheckResult inline fun Long.minusFlag(flag: Long) = this and flag.inv()

@CheckResult inline fun Int.hasFlag(flag: Int) = flag and this == flag
@CheckResult inline fun Int.withFlag(flag: Int) = this or flag
@CheckResult inline fun Int.minusFlag(flag: Int) = this and flag.inv()

@CheckResult inline fun Short.hasFlag(flag: Short) = flag and this == flag
@CheckResult inline fun Short.withFlag(flag: Short) = this or flag
@CheckResult inline fun Short.minusFlag(flag: Short) = this and flag.inv()

@CheckResult inline fun Byte.hasFlag(flag: Byte) = flag and this == flag
@CheckResult inline fun Byte.withFlag(flag: Byte) = this or flag
@CheckResult inline fun Byte.minusFlag(flag: Byte) = this and flag.inv()
