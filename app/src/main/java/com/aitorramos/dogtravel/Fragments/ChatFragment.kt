package com.aitorramos.dogtravel.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aitorramos.dogtravel.Adapters.PageAdapterChat
import com.aitorramos.dogtravel.R
import kotlinx.android.synthetic.main.fragment_chat.view.*

class ChatFragment : Fragment() {

    lateinit var fragmentAdapter :PageAdapterChat
    private lateinit var _view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view =  inflater.inflate(R.layout.fragment_chat, container, false)

        fragmentAdapter = PageAdapterChat(childFragmentManager)
        _view.view_pager.adapter = fragmentAdapter
        _view.tab.setupWithViewPager(_view.view_pager)
        _view.tab.getTabAt(0)!!.setText("Mis viajes")
        _view.tab.getTabAt(1)!!.setText("Mis solicitudes")

        return _view
    }

}