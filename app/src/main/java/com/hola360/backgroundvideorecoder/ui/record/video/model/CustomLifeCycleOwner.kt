package com.hola360.backgroundvideorecoder.ui.record.video.model

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class CustomLifeCycleOwner: LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    fun doOnResume() {
        lifecycleRegistry.markState(Lifecycle.State.RESUMED)
    }

    fun doOnDestroy() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}