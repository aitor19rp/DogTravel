package com.aitorramos.dogtravel.Activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.CircleTransform
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.aitorramos.mylibrary.ToolbarActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_new_travel.*
import kotlinx.android.synthetic.main.activity_reserve.*
import kotlinx.android.synthetic.main.activity_reserve.adView
import kotlinx.android.synthetic.main.activity_reserve.btReserve
import kotlinx.android.synthetic.main.activity_reserve.ivBig
import kotlinx.android.synthetic.main.activity_reserve.ivMedium
import kotlinx.android.synthetic.main.activity_reserve.ivSmall
import java.io.Serializable

class ReserveActivity : ToolbarActivity() {

    private var travel = Travel()
    private lateinit var chatDb: CollectionReference
    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserve)
        toolbarToLoad(toolbar as Toolbar)
        setTitle("Detalles del viaje")
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
        enableHomeDisplay(true)

        travel = intent.extras!!.get("travel") as Travel
        setData()
        setUpCurrentUser()
        setUpChatDb()
        btReserve.setOnClickListener{
            getChat()
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }

    private fun setUpChatDb(){
        chatDb = store.collection("chats")
    }
    private fun getChat(){
        chatDb
            .whereEqualTo("travel", travel.id)
            .whereEqualTo("sender", currentuser.uid)
            .get()
            .addOnSuccessListener {
                if(it.size() >= 1) toast("Ya has solicitado una reserva para este viaje")
                else goToActivity<MessagesActivity>{
                    putExtra("travel", travel as Serializable)
                    putExtra("flag", "1")
                }
            }

    }

    private fun setData(){
        tvDateTime.text = travel.date + ", " + travel.time
        tvCityFrom.text = travel.placeFrom
        tvCityTo.text = travel.placeTo
        tvName.text = travel.name
        Picasso.get().load(travel.photo).transform(CircleTransform()).into(ivProfile)
        if(travel.dogSize == "1"){
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(156,156,156))
            ivBig.setColorFilter(Color.rgb(156,156,156))
        }else if(travel.dogSize == "2"){
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(0,0,0))
            ivBig.setColorFilter(Color.rgb(156,156,156))
        }else{
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(0,0,0))
            ivBig.setColorFilter(Color.rgb(0,0,0))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}