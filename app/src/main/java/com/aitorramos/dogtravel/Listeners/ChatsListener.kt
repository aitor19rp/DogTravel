package com.aitorramos.dogtravel.Listeners

import com.aitorramos.dogtravel.Models.Chat


interface ChatsListener{
    fun onClick(chat: Chat, position: Int)
    fun onDelete(chat: Chat, position: Int)
}