package com.aitorramos.dogtravel.Models

import java.util.*

data class Chat(
    var chatId: String = "",
    var sender: String = "",
    var receiver: String = "",
    val data1: String = "",
    val data2: String = "",
    var photo: String = "",
    var name: String = "",
    var travel: String = "",
    var lastMessage: String = "",
    var lastDate: Date = Date(),
    var notification: String = "")