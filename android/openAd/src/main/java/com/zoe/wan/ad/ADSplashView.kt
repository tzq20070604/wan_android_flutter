package com.zoe.wan.ad

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk

/**
 * 开屏页广告
 */
class ADSplashView(context: Context, attr: AttributeSet) : FrameLayout(context, attr) {

    fun initAd(loadAdFailed: () -> Unit) {

        val ttNative: TTAdNative = TTAdSdk.getAdManager().createAdNative(context)
        TTAdManagerHolder.get().requestPermissionIfNecessary(context)
        //step3:可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        val adSlot = AdSlot.Builder()
            .setCodeId("889096894")
            .setImageAcceptedSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
            .setAdLoadType(TTAdLoadType.PRELOAD)
            .setSupportDeepLink(true)
//            .supportRenderControl() //支持模板样式
//            .setExpressViewAcceptedSize(350F, 300F)//设置模板宽高（dp）
            .build()


        //获取广告数据
        ttNative.loadSplashAd(adSlot, object : TTAdNative.CSJSplashAdListener {
            //开屏素材加载成功
            override fun onSplashLoadSuccess(p0: CSJSplashAd?) {
                LogUtils.d("获取广告数据 onSplashLoadSuccess ")
            }

            //加载开屏素材失败
            override fun onSplashLoadFail(p0: CSJAdError?) {
                //开发者处理跳转到APP主页面逻辑
                loadAdFailed.invoke()
                LogUtils.d("获取广告数据 onSplashLoadFail ${p0?.msg}")
            }

            //开屏渲染成功，可以展示开屏
            override fun onSplashRenderSuccess(ad: CSJSplashAd?) {
                LogUtils.d("获取广告数据 onSplashRenderSuccess ")
                if (ad == null) {
                    return
                }
                this@ADSplashView.removeAllViews()
                ad.showSplashView(this@ADSplashView)

            }

            override fun onSplashRenderFail(p0: CSJSplashAd?, p1: CSJAdError?) {
                //开发者处理跳转到APP主页面逻辑
                loadAdFailed.invoke()
                LogUtils.d("获取广告数据 onSplashRenderFail p0=${p0} p1=${p1?.msg}")
            }


        }, 300)

    }

}
