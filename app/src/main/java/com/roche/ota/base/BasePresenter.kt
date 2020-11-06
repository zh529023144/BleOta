package com.roche.ota.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter<V : IBaseView> : IPresenter<V>, LifecycleObserver {

    lateinit var mView: V
    private var compositeDisposable = CompositeDisposable()


    override fun attachView(mRootView: V) {
        this.mView = mRootView
    }


    override fun detachView() {
        // 保证Activity 销毁时 取消所有任务
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
    }

    private val isViewAttached: Boolean
        get() = mView != null

    fun checkVIewAttached() {
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        detachView()
    }

    private class MvpViewNotAttachedException internal constructor() :
        RuntimeException("please Call Ipresenter.attachView")
}