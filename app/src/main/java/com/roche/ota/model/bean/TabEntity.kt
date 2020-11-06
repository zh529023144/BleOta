package com.roche.ota.model.bean

import com.flyco.tablayout.listener.CustomTabEntity

data class TabEntity(

    var title: String = "",
    var selectedIcon: Int = 0,
    var unSelectedIcon: Int = 0
) : CustomTabEntity {


    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}