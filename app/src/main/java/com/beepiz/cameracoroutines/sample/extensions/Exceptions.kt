package com.beepiz.cameracoroutines.sample.extensions

import android.content.Intent

fun unexpectedValue(value: Any?): Nothing {
    throw IllegalStateException("unexpected value: $value")
}

fun illegal(errorMessage: String? = null): Nothing = throw IllegalStateException(errorMessage)
fun illegalArg(errorMessage: String? = null): Nothing = throw IllegalArgumentException(errorMessage)
fun illegalArg(arg: Any? = null): Nothing = throw IllegalArgumentException("Illegal argument: $arg")

fun unsupported(errorMessage: String? = null): Nothing = throw UnsupportedOperationException(errorMessage)

fun unsupportedAction(intent: Intent): Nothing = unsupported("Unsupported action: ${intent.action}")
