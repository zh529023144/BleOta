package com.roche.ota.ui.mine.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.roche.ota.R
import kotlinx.android.synthetic.main.base_title_bar.*
import com.yzq.zxinglibrary.encode.CodeCreator
import android.graphics.BitmapFactory
import android.view.View
import com.roche.ota.model.manager.BleManager
import com.roche.ota.utils.AppUtils
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity() {

    //App 下载地址
    private val contentEtString = "http://1.nofetel.com/vend/app/ble_ota.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        base_tv_title.text = "关于"
        base_iv_back.visibility = View.VISIBLE
                base_iv_back.setOnClickListener {
                    finish()
                }

        base_iv_back.setImageResource(R.drawable.icon_back_white)
        //生成二维码
        val logo = BitmapFactory.decodeResource(resources, R.mipmap.icon_app)
        val bitmap = CodeCreator.createQRCode(contentEtString, 500, 500, logo)

        iv_scan.setImageBitmap(bitmap)

        //获取app 版本名称

        tv_app_version.text = AppUtils.getVersionName(applicationContext)


    }
}
