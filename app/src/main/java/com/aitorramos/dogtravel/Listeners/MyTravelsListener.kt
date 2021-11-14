package com.aitorramos.dogtravel.Listeners

import com.aitorramos.dogtravel.Models.Travel

interface MyTravelsListener{
    fun onClick(travel: Travel, position: Int)
    fun onDelete(travel: Travel, position: Int)
}