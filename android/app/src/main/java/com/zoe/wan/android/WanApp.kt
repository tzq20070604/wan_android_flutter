package com.zoe.wan.android

import android.app.Application
import com.zoe.wan.ad.TTAdManager

class WanApp:Application() {
    override fun onCreate() {
        super.onCreate()
        TTAdManager.initAd(this)
    }
}
