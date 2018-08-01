package com.beepiz.cameracoroutines.sample.extensions.coroutines

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Lifecycle.Event.ON_DESTROY
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.Job

fun Lifecycle.createJob(cancelEvent: Lifecycle.Event = ON_DESTROY): Job = Job().also { job ->
    addObserver(object : GenericLifecycleObserver {
        override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event) {
            if (event == cancelEvent) {
                removeObserver(this)
                job.cancel()
            }
        }
    })
}

private val lifecycleJobs = mutableMapOf<Lifecycle, Job>()

val Lifecycle.job: Job
    get() = lifecycleJobs[this] ?: createJob().also {
        lifecycleJobs[this] = it
        it.invokeOnCompletion { lifecycleJobs -= this }
    }
