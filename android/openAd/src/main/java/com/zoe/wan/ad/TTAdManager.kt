package com.zoe.wan.ad

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.bytedance.sdk.openadsdk.BuildConfig
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk

object TTAdManager {
    fun initAd(application: Application) {
//        TTAdManagerHolder.init(application.applicationContext)
        val config = TTAdConfig.Builder().appId("5516677")
            .appName("Android资讯_android").debug(BuildConfig.DEBUG).build()
        TTAdSdk.init(application.applicationContext, config)
//        if (TTAdSdk.isSdkReady()) {
        // sdk初始化完成，可以进行广告加载等后续操作
        TTAdSdk.start(object : TTAdSdk.Callback {
            override fun success() {
                LogUtils.d("TTAdManager 广告SDK启动成功")
            }

            override fun fail(p0: Int, p1: String?) {
                LogUtils.d("TTAdManager 广告SDK启动失败 p0=${p0}  p1=${p1}")
            }

        })
//        }

    }
}
