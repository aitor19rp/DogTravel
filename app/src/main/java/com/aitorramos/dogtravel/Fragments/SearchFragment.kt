package com.aitorramos.dogtravel.Fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aitorramos.dogtravel.Activities.SearchResultAcitivity
import com.aitorramos.dogtravel.Dialogs.DatePickerFragment
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.aitorramos.dogtravel.Utils.twoDigits
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.fragment_search.view.etDate
import java.io.Serializable
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var _view: View

    val apiKey = "AIzaSyAtHA_VgPqiNJ9nZrfykr4rYM2Xuw1UbDY"
    var cityFrom = "Salamanca"
    var cityTo = "Madrid"
    var size = 0
    var date = ""

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var travelsDb: CollectionReference
    private var travel = Travel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_search, container, false)

        //setupAutoComplete(R.id.gCityFrom, 0)
        //setupAutoComplete(R.id.gCityTo, 1)

        _view.etDate.setOnClickListener{
            showDatePickerDialog()
        }

        _view.ivSmall.setOnClickListener{
            _view.ivSmall.setColorFilter(Color.rgb(0,0,0))
            _view.ivMedium.setColorFilter(Color.rgb(156,156,156))
            _view.ivBig.setColorFilter(Color.rgb(156,156,156))
            size = 1
        }
        _view.ivMedium.setOnClickListener{
            _view.ivSmall.setColorFilter(Color.rgb(0,0,0))
            _view.ivMedium.setColorFilter(Color.rgb(0,0,0))
            _view.ivBig.setColorFilter(Color.rgb(156,156,156))
            size = 2
        }
        _view.ivBig.setOnClickListener{
            _view.ivSmall.setColorFilter(Color.rgb(0,0,0))
            _view.ivMedium.setColorFilter(Color.rgb(0,0,0))
            _view.ivBig.setColorFilter(Color.rgb(0,0,0))
            size = 3
        }

        _view.btReserve.setOnClickListener{
            searchTravel()
        }

        val adRequest = AdRequest.Builder().build()
        _view.adView.loadAd(adRequest)

        return _view
    }

    private fun searchTravel(){
        if(date.isBlank() || cityFrom.isBlank() || cityTo.isBlank() || size == 0)
        {
            activity!!.toast("Todos los datos deben estar rellenos")
        }else {
            travel.date = date;
            travel.placeFrom = cityFrom
            travel.placeTo = cityTo
            travel.dogSize = size.toString()
            activity!!.goToActivity<SearchResultAcitivity> {
                putExtra(
                    "travel", travel as Serializable
                )
            }
        }
    }

    private fun setupAutoComplete(fragment: Int, flag: Int){

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(fragment) as AutocompleteSupportFragment?

        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
            ?.setOnClickListener { view ->
                if(flag == 0) cityFrom = ""
                else cityTo = ""
                autocompleteFragment.setText("")
                view.visibility = View.GONE
            }

        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(activity!!, Locale.getDefault())
                val addresses: List<Address> = geocoder.getFromLocation(place.latLng!!.latitude, place.latLng!!.longitude, 1)
                //Log.e("CITY", "Place: " + addresses.get(0).locality)
                if(flag == 0){
                    cityFrom = addresses.get(0).locality
                }else{
                    cityTo = addresses.get(0).locality
                }
            }

            override fun onError(status: Status) {
                Log.e("ERROR", "An error occurred: $status")
            }
        })
    }

    private fun showDatePickerDialog(){
        val newFragment = DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener(){ _, year, month, day ->
            val selectedDate = day.twoDigits() + "/" + (month + 1).twoDigits() + "/" + year
            date = selectedDate
            _view.etDate.setText(selectedDate)
        })

        newFragment.show(fragmentManager!!, "datePicker")
    }
}