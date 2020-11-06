package com.roche.ota.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.roche.ota.R
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.roche.ota.model.bean.TabEntity
import com.roche.ota.ui.mine.MineFragment
import com.roche.ota.ui.statistic.StatisticFragment
import com.roche.ota.ui.update.UpdateFragment
import com.roche.ota.utils.StatusBarUtil
import com.roche.ota.utils.showToast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), BottomNavigationBar.OnTabSelectedListener {

    val TAG = "MainActivity"

    var currentIndex = 0

    val mFragment = arrayListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFragment.add(UpdateFragment.newInstance())
        mFragment.add(StatisticFragment.newInstance())
        mFragment.add(MineFragment.newInstance())

        bottom_navigation_bar.setMode(BottomNavigationBar.MODE_FIXED)
        bottom_navigation_bar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
        bottom_navigation_bar.setBarBackgroundColor(R.color.white)

        bottom_navigation_bar
            .addItem(
                BottomNavigationItem(R.drawable.icon_update_active, "升级")
                    .setActiveColorResource(R.color.text_active)
                    .setInactiveIconResource(R.drawable.icon_update_inactive)
                    .setInActiveColorResource(R.color.text_inactive)
            )
            .addItem(
                BottomNavigationItem(R.drawable.icon_list_active, "列表")
                    .setActiveColorResource(R.color.text_active)
                    .setInactiveIconResource(R.drawable.icon_list_inactive)
                    .setInActiveColorResource(R.color.text_inactive)
            )
            .addItem(
                BottomNavigationItem(R.drawable.icon_mine_active, "我的")
                    .setActiveColorResource(R.color.text_active)
                    .setInactiveIconResource(R.drawable.icon_mine_inactive)
                    .setInActiveColorResource(R.color.text_inactive)
            )
            .initialise()

        bottom_navigation_bar.setTabSelectedListener(this)

        onTabUnselected(-1)


    }

    override fun onTabReselected(position: Int) {
        Log.e(TAG, "onTabReselected   $position")
    }

    override fun onTabUnselected(position: Int) {
        Log.e(TAG, "onTabUnselected   $position")

        supportFragmentManager.beginTransaction().apply {
            val fragment = mFragment[currentIndex]
            if (!fragment.isAdded) {
                add(R.id.home_activity_frag_container, fragment)
            } else {
                show(fragment)
            }

            if (position != -1) {
                val lastFragment = mFragment[position]
                hide(lastFragment)
            }
        }.commitAllowingStateLoss()
    }

    override fun onTabSelected(position: Int) {
        Log.e(TAG, "onTabSelected   $position    ")
        currentIndex = position

    }

    private var isExit: Boolean = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            val handler = Handler()

            if ((!isExit)) {
                isExit = true
                showToast("再按一次退出应用")
                handler.postDelayed({ isExit = false }, 1000 * 2) //x秒后没按就取消

            } else {
                finish()
                exitProcess(0)
            }
        }
        return false
    }

}
