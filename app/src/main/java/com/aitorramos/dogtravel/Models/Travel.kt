package com.aitorramos.dogtravel.Models

import java.io.Serializable

data class Travel(var id: String = "",
                  var placeFrom: String = "",
                  var placeTo: String = "",
                  var date: String = "",
                  var time: String = "",
                  var dogSize: String = "",
                  var user: String = "",
                  var millis: Long = 0,
                  var photo: String = "",
                  var name: String = "",
                  var idDocument: String = "",
                  var status: String = "") : Serializable