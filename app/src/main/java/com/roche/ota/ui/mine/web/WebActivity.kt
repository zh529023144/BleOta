package com.roche.ota.ui.mine.web

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import com.just.agentweb.AgentWeb
import com.roche.ota.R
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.base_title_bar.*

class WebActivity : AppCompatActivity() {

    private var mAgentWeb: AgentWeb? = null

    private val mUrl="http://roche-life.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        base_tv_title.text="萝趣官网"
        base_iv_back.visibility = View.VISIBLE
        base_iv_back.setImageResource(R.drawable.icon_back_white)


        base_iv_back.setOnClickListener {
            mAgentWeb?.let { web ->
                if (web.webCreator.webView.canGoBack()) {
                    web.webCreator.webView.goBack()
                }else{
                    finish()
                }
            }
        }
        //加载网页
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(webcontent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .createAgentWeb()
            .ready()
            .go(mUrl).apply {
                agentWebSettings.webSettings.loadWithOverviewMode = true// 缩放至屏幕的大小
                agentWebSettings.webSettings.useWideViewPort = true// //将图片调整到适合webview的大小
            }



        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                mAgentWeb?.let { web ->
                    if (web.webCreator.webView.canGoBack()) {
                        web.webCreator.webView.goBack()
                    }else{
                        finish()
                    }
                }
            }
        })
    }

    override fun onPause() {
        mAgentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        mAgentWeb?.webLifeCycle?.onDestroy()
        super.onDestroy()
    }
}
