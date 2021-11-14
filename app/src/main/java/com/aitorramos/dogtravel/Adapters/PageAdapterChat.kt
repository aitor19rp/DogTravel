package com.aitorramos.dogtravel.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.aitorramos.dogtravel.Fragments.MyRequestTab
import com.aitorramos.dogtravel.Fragments.MyTravelsTab

class PageAdapterChat(fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> MyTravelsTab()
            else -> MyRequestTab()
        }
    }

    override fun getCount(): Int {
        return 2
    }

}