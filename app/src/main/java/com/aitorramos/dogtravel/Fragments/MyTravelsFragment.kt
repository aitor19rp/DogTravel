package com.aitorramos.dogtravel.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitorramos.dogtravel.Activities.NewTravelActivity
import com.aitorramos.dogtravel.Adapters.MyTravelsAdapter
import com.aitorramos.dogtravel.Listeners.MyTravelsListener
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_my_travels.view.*
import java.io.Serializable

class MyTravelsFragment : Fragment() {

    private lateinit var _view: View

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var travelsDb: CollectionReference
    private var travelList: ArrayList<Travel> = ArrayList()
    private lateinit var adapter: MyTravelsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _view = inflater.inflate(R.layout.fragment_my_travels, container, false)
        _view.fab.setOnClickListener{
            activity!!.goToActivity<NewTravelActivity>()
        }

        setUpCurrentUser()
        setUpTravelsDb()
        setUpRecyclerView()
        getMyTravels()

        return _view
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        _view.recyclerView.setHasFixedSize(true)
        _view.recyclerView.layoutManager = layoutManager
        _view.recyclerView.itemAnimator = DefaultItemAnimator()

        adapter = MyTravelsAdapter(travelList, object: MyTravelsListener {
            override fun onClick(travels: Travel, position: Int) {
                activity!!.goToActivity<NewTravelActivity>{
                    putExtra("travel", travels as Serializable)
                }
            }

            override fun onDelete(travel: Travel, position: Int) {
            }

        })

        _view.recyclerView.adapter = adapter
    }

    private fun getMyTravels(){
        travelsDb
            .orderBy("millis", Query.Direction.DESCENDING)
            .whereEqualTo("user", currentuser.uid)
            .addSnapshotListener(object : java.util.EventListener, EventListener<QuerySnapshot>{
                override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                    p1?.let {
                        activity!!.toast("Error al obtener los datos")
                        Log.e("ERROR", it.toString())
                        return
                    }

                    p0?.let {
                        travelList.clear()
                        val travels = it.toObjects(Travel::class.java)
                        travelList.addAll(travels)
                        adapter.notifyDataSetChanged()

                        if(travelList.isEmpty()){
                            _view.tvNoData.visibility = View.VISIBLE
                            _view.recyclerView.visibility = View.GONE
                        }else{
                            _view.tvNoData.visibility = View.GONE
                            _view.recyclerView.visibility = View.VISIBLE
                        }
                    }
                }

            })
    }

    private fun setUpTravelsDb(){
        travelsDb = store.collection("travels")
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }



}