package com.roche.ota.ui.login

import android.Manifest.permission.*
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.roche.ota.R
import com.roche.ota.base.BaseActivity
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.LoginResponse
import com.roche.ota.ui.MainActivity
import com.roche.ota.utils.Preference
import com.roche.ota.utils.showToast
import com.roche.ota.view.CustomDrawerPopupView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<LoginPresenter>(), ILoginView {


    private var userName: String by Preference("name", "")
    private var passWord: String by Preference("pass", "")
    private var isOpen: Boolean = true
    private var token: String by Preference("token", "")
    private var isPhone: Boolean = false


    override val mPresenter: LoginPresenter by lazy {
        LoginPresenter().apply {
            attachView(this@LoginActivity)
        }
    }

    override fun layoutId(): Int = R.layout.activity_login


    override fun initData() {
        //权限申请
        val dis = RxPermissions(this).request(
            CAMERA,
            BLUETOOTH,
            BLUETOOTH_ADMIN,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,
            WRITE_EXTERNAL_STORAGE
        )
            .subscribe {

                if (it) {
                    showToast("权限通过")
                } else {
                    showToast("权限未通过")
                }
            }
        //登陆
        bn_login.setOnClickListener {
            when {
                isPhone -> {
                    //手机号码登录
                    val phone = et_phone.text.toString().trim()
                    val code = et_code.text.toString().trim()
                    if (phone.isEmpty()) {
                        showToast("请输入手机号码")
                        return@setOnClickListener
                    }

                    if (code.isEmpty()) {
                        showToast("请输入验证码")
                        return@setOnClickListener
                    }
//                    userName = name
//                    passWord = pass
//
//                    mPresenter.login(name, pass)

                }
                else -> {
                    //账号密码登录
                    val name = et_user.text.toString().trim()
                    val pass = et_password.text.toString().trim()
                    if (name.isEmpty()) {
                        showToast(getString(R.string.login_empty_hint))
                        return@setOnClickListener
                    }

                    if (pass.isEmpty()) {
                        showToast(getString(R.string.pass_empty_hint))
                        return@setOnClickListener
                    }
                    userName = name
                    passWord = pass

                    mPresenter.login(name, pass)

//                     XPopup.Builder(this)
//                        .hasStatusBarShadow(true)
//                        .asCustom(CustomDrawerPopupView(this))
//                        .show()
                }
            }


        }
        //切换登录方式  默认账号密码
        tv_phone.setOnClickListener {
            if (isPhone) {
                isPhone = false
                ll_login_phone.visibility = View.GONE
                ll_login_user.visibility = View.VISIBLE
                tv_phone.text = "手机号验证码登录"
            } else {
                isPhone = true
                ll_login_phone.visibility = View.VISIBLE
                ll_login_user.visibility = View.GONE
                tv_phone.text = "账号密码登录"
            }
        }

        //显示光标
        et_user.setOnClickListener {
            et_user.isCursorVisible = true
        }

        et_phone.setOnClickListener {
            et_phone.isCursorVisible = true
        }

        //显示密码 明文
        iv_login_eye.setOnClickListener {
            if (isOpen) {
                isOpen = false
                et_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                iv_login_eye.setImageResource(R.drawable.login_openeye)
            } else {
                isOpen = true
                et_password.transformationMethod = PasswordTransformationMethod.getInstance()
                iv_login_eye.setImageResource(R.drawable.login_closeye)
            }
            et_password.setSelection(et_password.text.toString().length)
        }

        //清除账号密码
        iv_login_close.setOnClickListener {
            et_user.text = Editable.Factory.getInstance().newEditable("")
            et_password.text = Editable.Factory.getInstance().newEditable("")
            iv_login_close.visibility = View.INVISIBLE
            iv_login_eye.visibility = View.INVISIBLE
            bn_login.isEnabled = false
        }

        bn_login_code.setOnClickListener {

        }

    }

    override fun initView() {

        //暂时不开发手机号登录
        tv_phone.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }


        //保存的用户信息
        if (userName.isNotEmpty()) {
            et_user.text = Editable.Factory.getInstance().newEditable(userName)
            et_password.text = Editable.Factory.getInstance().newEditable(passWord)
            et_user.isCursorVisible = false

            closeKeyBoard(et_user, applicationContext)
            bn_login.isEnabled = true
        }
        //监听账号输入
        et_user.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    iv_login_close.visibility = View.VISIBLE
                    //密码同时存在  可登录
                    val password = et_password.text.toString().trim()
                    if (password.isNotEmpty()) {
                        bn_login.isEnabled = true
                    }
                } else {
                    iv_login_close.visibility = View.INVISIBLE
                    bn_login.isEnabled = false
                }
            }

        })
        //监听密码输入
        et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    iv_login_eye.visibility = View.VISIBLE
                    //账号同时存在 可登录
                    val name = et_password.text.toString().trim()
                    if (name.isNotEmpty()) {
                        bn_login.isEnabled = true
                    }
                } else {
                    iv_login_eye.visibility = View.INVISIBLE
                    bn_login.isEnabled = false
                }
            }
        })

    }

    override fun onLoginSucceed(response: LoginResponse) {
        token = response.result[0].token
        goToActivity<MainActivity>()
        finish()
    }

    override fun onLoginError(error: String, errorCode: Int) {
        showToast(error)
    }

    override fun showLoading() {
        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }


}

