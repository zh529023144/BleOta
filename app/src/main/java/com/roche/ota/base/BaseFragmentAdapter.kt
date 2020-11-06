package com.roche.ota.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentAdapter : FragmentPagerAdapter {

    private var fragmentList: List<Fragment>? = ArrayList()
    private var mTitles: List<String>? = null

    constructor(fm: FragmentManager, fragmentList: ArrayList<Fragment>) : super(fm) {
        this.fragmentList = fragmentList
    }

    constructor(
        fm: FragmentManager,
        fragmentList: ArrayList<Fragment>,
        mTitles: List<String>
    ) : super(fm) {
        this.fragmentList = fragmentList
        this.mTitles = mTitles
    }

    private fun setFragment(
        fm: FragmentManager,
        fragmentList: List<Fragment>,
        mTitles: List<String>
    ) {
        this.mTitles = mTitles
        if (this.fragmentList != null) {
            val fragmentTransaction = fm.beginTransaction()
            fragmentList.forEach {
                fragmentTransaction.remove(it)
            }
            fragmentTransaction?.commitAllowingStateLoss()
            fm.executePendingTransactions()
        }
        this.fragmentList = fragmentList
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {

        return if (null != mTitles) mTitles!![position] else ""
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList!![position]
    }

    override fun getCount(): Int {
        return fragmentList!!.size
    }
}