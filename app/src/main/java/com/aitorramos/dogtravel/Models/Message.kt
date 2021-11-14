package com.aitorramos.dogtravel.Models

import java.util.*

data class Message(var sender: String = "", var message: String = "", var sendAt: Date = Date(), var chatId: String = "", var photo: String = "")