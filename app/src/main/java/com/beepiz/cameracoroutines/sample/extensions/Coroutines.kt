package com.beepiz.cameracoroutines.sample.extensions

import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

suspend fun coroutineContext(): CoroutineContext = suspendCoroutineOrReturn { cont -> cont.context }
