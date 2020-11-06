package com.roche.ota.base

interface IPresenter<in V : IBaseView> {
    fun attachView(mRootView: V)

    fun detachView()
}