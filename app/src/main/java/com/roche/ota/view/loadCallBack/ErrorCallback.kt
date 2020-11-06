package com.roche.ota.view.loadCallBack

import com.kingja.loadsir.callback.Callback
import com.roche.ota.R


class ErrorCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_error
    }

}