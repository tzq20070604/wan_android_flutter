package com.zoe.wan.ad

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.blankj.utilcode.util.LogUtils
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdNative.NativeExpressAdListener
import com.bytedance.sdk.openadsdk.TTAppDownloadListener
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd.ExpressAdInteractionListener
import java.lang.ref.WeakReference

/**
 * 广告banner
 * 模板渲染方式
 */
class ADBannerView(context: Context, attr: AttributeSet) : FrameLayout(context, attr) {

    private var activity: WeakReference<Activity?>? = null
    private var mTTAd: TTNativeExpressAd? = null
    private var startTime: Long = 0
    private var mHasShowDownloadActive = false

    //创建TTAdNative对象
    private val mTTAdNative: TTAdNative by lazy {
        TTAdManagerHolder.get().createAdNative(context)
    }

    /**
     * 初始化banner广告
     */
    fun initAdBanner(activity: Activity?) {
        this.activity = WeakReference(activity)
        initTTSDKConfig()
        loadExpressAd("956865979", 300, 150)
    }

    private fun initTTSDKConfig() {
        //(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(context)
    }

    private fun loadExpressAd(codeId: String, expressViewWidth: Int, expressViewHeight: Int) {
        this@ADBannerView.removeAllViews()
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId) //广告位id
            .setAdCount(3) //请求广告数量为1到3条
            .setExpressViewAcceptedSize(
                expressViewWidth.toFloat(),
                expressViewHeight.toFloat()
            ) //期望模板广告view的size,单位dp
            .build()
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerExpressAd(adSlot, object : NativeExpressAdListener {
            override fun onError(code: Int, message: String) {
                LogUtils.d("ADBannerView load error : $code, $message")
                this@ADBannerView.removeAllViews()
            }

            override fun onNativeExpressAdLoad(ads: List<TTNativeExpressAd>) {
                if (ads == null || ads.size == 0) {
                    return
                }
                /*******************
                 * 如果旧广告对象不使用了，在替换成新广告对象前，必须进行销毁，否则可能导致多个广告对象同时存在，影响SSR
                 */
                if (mTTAd != null) {
                    mTTAd?.destroy()
                }
                /** */
                mTTAd = ads[0]
                mTTAd?.setSlideIntervalTime(30 * 1000)
                bindAdListener(mTTAd)
                //开始渲染广告view
                mTTAd?.render()
                startTime = System.currentTimeMillis()
                LogUtils.d("ADBannerView load success!")
            }
        })
    }

    private fun bindAdListener(ad: TTNativeExpressAd?) {
        ad?.setExpressInteractionListener(object : ExpressAdInteractionListener {
            override fun onAdClicked(view: View, type: Int) {
                LogUtils.d("ADBannerView bindAdListener onAdClicked 广告被点击")
            }

            override fun onAdShow(view: View, type: Int) {
                LogUtils.d("ADBannerView bindAdListener onAdShow 广告展示")
            }

            override fun onRenderFail(view: View, msg: String, code: Int) {
                LogUtils.d("ADBannerView bindAdListener onRenderFail 广告展示 $msg code:$code")
            }

            override fun onRenderSuccess(view: View, width: Float, height: Float) {
                LogUtils.d("ADBannerView bindAdListener onRenderSuccess 渲染成功")
                //返回view的宽高 单位 dp
                this@ADBannerView.removeAllViews()
                this@ADBannerView.addView(view)
            }
        })
        //dislike设置
        bindDislike(ad, false)
        if (ad?.interactionType != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return
        }
        ad.setDownloadListener(object : TTAppDownloadListener {
            override fun onIdle() {
                LogUtils.d("ADBannerView onIdle 点击开始下载")
            }

            override fun onDownloadActive(
                totalBytes: Long,
                currBytes: Long,
                fileName: String,
                appName: String
            ) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true
                    LogUtils.d("ADBannerView onDownloadActive 下载中，点击暂停")
                }
            }

            override fun onDownloadPaused(
                totalBytes: Long,
                currBytes: Long,
                fileName: String,
                appName: String
            ) {
                LogUtils.d("ADBannerView onDownloadPaused 下载中，点击继续")
            }

            override fun onDownloadFailed(
                totalBytes: Long,
                currBytes: Long,
                fileName: String,
                appName: String
            ) {
                LogUtils.d("ADBannerView onDownloadFailed 下载中，点击重新下载")
            }

            override fun onInstalled(fileName: String, appName: String) {
                LogUtils.d("ADBannerView onInstalled 安装完成，点击图片打开")
            }

            override fun onDownloadFinished(totalBytes: Long, fileName: String, appName: String) {
                LogUtils.d("ADBannerView onDownloadFinished 点击安装")
            }
        })
    }

    /**
     * 设置广告的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private fun bindDislike(ad: TTNativeExpressAd?, customStyle: Boolean) {
        if (customStyle) {
            //使用自定义样式
            val dislikeInfo = ad?.dislikeInfo
            if (dislikeInfo == null || dislikeInfo.filterWords == null || dislikeInfo.filterWords.isEmpty()) {
                return
            }
            val dislikeDialog = DislikeDialog(context, dislikeInfo)
            dislikeDialog.setOnDislikeItemClick { filterWord -> //屏蔽广告
                LogUtils.d("ADBannerView bindDislike 点击${filterWord.name}")
                //用户选择不喜欢原因后，移除广告展示
                this@ADBannerView.removeAllViews()
            }
            ad.setDislikeDialog(dislikeDialog)
            return
        }
        //使用默认模板中默认dislike弹出样式
        ad?.setDislikeCallback(activity?.get(), object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {}
            override fun onSelected(position: Int, value: String, enforce: Boolean) {
                LogUtils.d("ADBannerView bindDislike onSelected 点击${value}")
                this@ADBannerView.removeAllViews()
                //用户选择不喜欢原因后，移除广告展示
                if (enforce) {
                    LogUtils.d("ADBannerView onSelected 穿山甲sdk强制将view关闭了")
                }
            }

            override fun onCancel() {
                LogUtils.d("ADBannerView onCancel 点击取消")
            }
        })
    }
}
