package com.zoe.wan.android

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zoe.wan.ad.ADSplashView

class SplashActivity : AppCompatActivity() {

    private var splashAdView: ADSplashView? = null
    private var splashJump: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashAdView = findViewById(R.id.splashAdView)
        initViewData()
    }

    fun initViewData() {
        //初始化广告
        splashAdView?.initAd {
            //广告加载失败进入首页
            jumpToTab()
        }

        splashJump?.setOnClickListener { jumpToTab() }

    }

    private fun jumpToTab() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
