package com.aitorramos.dogtravel.Activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitorramos.dogtravel.Adapters.SearchResultAdapter
import com.aitorramos.dogtravel.Listeners.MyTravelsListener
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.aitorramos.mylibrary.ToolbarActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.synthetic.main.activity_new_travel.*
import kotlinx.android.synthetic.main.activity_search_result_acitivity.*
import kotlinx.android.synthetic.main.activity_search_result_acitivity.adView
import kotlinx.android.synthetic.main.activity_search_result_acitivity.toolbar
import java.io.Serializable

class SearchResultAcitivity : ToolbarActivity() {

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var travelsDb: CollectionReference
    private var travel = Travel()
    private var travelList: ArrayList<Travel> = ArrayList()
    private var sortedList: List<Travel> = ArrayList()
    private lateinit var adapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result_acitivity)
        toolbarToLoad(toolbar as Toolbar)
        setTitle("Viajes encontrados")
        enableHomeDisplay(true)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        setUpCurrentUser()
        setUpTravelsDb()
        setUpRecyclerView()
        travel = intent.extras!!.get("travel") as Travel
        getTravels()

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }

    private fun getTravels(){
        var size = travel.dogSize.toInt() + 1
        travelList.clear()
        travelsDb
            .whereLessThan("dogSize", size.toString())
            .orderBy("dogSize")
            .orderBy("millis", Query.Direction.DESCENDING)
            .whereEqualTo("date", travel.date)
            .whereEqualTo("placeFrom", travel.placeFrom)
            .whereEqualTo("placeTo", travel.placeTo)
            .whereEqualTo("status", "")
            .get()
            .addOnSuccessListener {
                for(document in it){
                    var travel = Travel()
                    travel.date = document.getString("date").toString()
                    travel.time = document.getString("time").toString()
                    travel.placeFrom = document.getString("placeFrom").toString()
                    travel.placeTo = document.getString("placeTo").toString()
                    travel.dogSize = document.getString("dogSize").toString()
                    travel.photo = document.getString("photo").toString()
                    travel.name = document.getString("name").toString()
                    travel.user = document.getString("user").toString()
                    travel.id = document.getString("id").toString()
                    travel.millis = document.getLong("millis")!!

                    if(travel.user != currentuser.uid){ travelList.add(travel) }
                }
                sortedList = travelList.sortedWith(compareBy { it.time })
                travelList.clear()
                travelList.addAll(sortedList)
                adapter.notifyDataSetChanged()

                if(travelList.isEmpty()){
                    tvNoData.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }else{
                    if(travelList.size == 1) setTitle("${travelList.size} viaje encontrado")
                    else setTitle("${travelList.size} viajes encontrados")
                    tvNoData.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener{
                toast("Error al obtener los datos")
                Log.e("ERROR", it.toString())
            }

    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        adapter = SearchResultAdapter(travelList, object: MyTravelsListener {
            override fun onClick(travel: Travel, position: Int) {
                goToActivity<ReserveActivity>{ putExtra(
                    "travel", travel as Serializable
                ) }
            }

            override fun onDelete(travel: Travel, position: Int) {

            }
        })

        recyclerView.adapter = adapter
    }

    private fun setUpTravelsDb(){
        travelsDb = store.collection("travels")
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}