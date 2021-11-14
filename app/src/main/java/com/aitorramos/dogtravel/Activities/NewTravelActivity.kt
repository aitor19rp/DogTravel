package com.aitorramos.dogtravel.Activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.aitorramos.dogtravel.Dialogs.DatePickerFragment
import com.aitorramos.dogtravel.Dialogs.TimePickerFragment
import com.aitorramos.dogtravel.MainActivity
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.aitorramos.dogtravel.Utils.twoDigits
import com.aitorramos.mylibrary.ToolbarActivity
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
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_new_travel.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*

class NewTravelActivity : ToolbarActivity() {

    val apiKey = "AIzaSyAtHA_VgPqiNJ9nZrfykr4rYM2Xuw1UbDY"
    var cityFrom = "Salamanca"
    var cityTo = "Madrid"
    var size = 0
    var date = ""
    var time = ""
    val cSelected = Calendar.getInstance()

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var travelsDb: CollectionReference
    private lateinit var chatsDb: CollectionReference
    private lateinit var messagesDb: CollectionReference
    private var travel = Travel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_travel)
        toolbarToLoad(toolbar as Toolbar)
        setTitle("Publicar viaje")
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
        enableHomeDisplay(true)

        setUpCurrentUser()
        setUpTravelsDb()
        setUpChatsDb()
        setUpMessagesDb()

        etDate.setOnClickListener{
            showDatePickerDialog()
        }
        etTime.setOnClickListener{
            showTimePickerDialog()
        }

        ivSmall.setOnClickListener{
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(156,156,156))
            ivBig.setColorFilter(Color.rgb(156,156,156))
            size = 1
        }
        ivMedium.setOnClickListener{
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(0,0,0))
            ivBig.setColorFilter(Color.rgb(156,156,156))
            size = 2
        }
        ivBig.setOnClickListener{
            ivSmall.setColorFilter(Color.rgb(0,0,0))
            ivMedium.setColorFilter(Color.rgb(0,0,0))
            ivBig.setColorFilter(Color.rgb(0,0,0))
            size = 3
        }

        if(intent.extras != null) {
            travel = intent.extras!!.get("travel") as Travel
            btReserve.text = "Editar"
            setTitle("Editar Viaje")
            cityFrom = travel.placeFrom
            cityTo = travel.placeTo
            date = travel.date
            tvStatus.text = travel.status
            etDate.setText(travel.date)
            cSelected.set(Calendar.DAY_OF_MONTH, travel.date.split("/").get(0).toInt())
            cSelected.set(Calendar.MONTH, travel.date.split("/").get(1).toInt())
            cSelected.set(Calendar.YEAR, travel.date.split("/").get(2).toInt())
            time = travel.time
            etTime.setText(travel.time)
            if(travel.dogSize == "1") ivSmall.callOnClick()
            else if(travel.dogSize == "2") ivMedium.callOnClick()
            else ivBig.callOnClick()
        }

        btReserve.setOnClickListener{
            if(cityFrom.isBlank() || cityTo.isBlank() || date.isBlank() || time.isBlank() || size == 0){
                toast("Todos los datos deben estar rellenos")
            }else{
                if(travel.placeFrom == "") addTravel()
                else{
                    if(travel.status == "Cancelado") toast("El viaje esta cancelado")
                    else editTravel()
                }
            }
        }

        setupAutoComplete(R.id.gCityFrom, 0)
        setupAutoComplete(R.id.gCityTo, 1)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun addTravel(){
        val travel = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "placeFrom" to cityFrom,
            "placeTo" to cityTo,
            "date" to date,
            "time" to time,
            "dogSize" to size.toString(),
            "user" to currentuser.uid,
            "millis" to cSelected.timeInMillis,
            "photo" to currentuser.photoUrl.toString(),
            "name" to currentuser.displayName!!.split(" ").get(0),
            "status" to "")

        travelsDb.add(travel)
            .addOnSuccessListener {
                toast("Tu viaje ha sido publicado")
                onBackPressed()
            }
            .addOnFailureListener{
                toast("Error al crear viajer")
                Log.e("ERROR", it.toString())
            }
    }

    private fun editTravel(){
        travelsDb.whereEqualTo("id", travel.id)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    travelsDb.document(document.id).update("placeFrom", cityFrom, "placeTo", cityTo, "dogSize", size.toString(), "date", date, "time", time)
                }
                toast("Tu viaje ha sido editado")
                onBackPressed()
            }
            .addOnFailureListener{ toast("Error al editar el viaje")}
    }

    private fun deleteTravel()
    {
        travelsDb
            .whereEqualTo("id", travel.id)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    travelsDb.document(document.id).delete()
                }
                chatsDb
                    .whereEqualTo("travel", travel.id)
                    .get()
                    .addOnSuccessListener {
                        for(document in it){
                            messagesDb.whereEqualTo("chatId", document.getString("chatId").toString())
                                .get()
                                .addOnSuccessListener {
                                    for(document in it){
                                        messagesDb.document(document.id).delete()
                                    }
                                }
                            chatsDb.document(document.id).delete()
                        }
                    }.addOnFailureListener{
                        toast("Error al eliminar el viaje")
                    }
                goToActivity<MainActivity>()
            }.addOnFailureListener{
                toast("Error al eliminar el viaje")
            }

    }

    private fun travelStatus(type: String){
        travelsDb.whereEqualTo("id", travel.id)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    if(type == "cancel"){
                        travelsDb.document(document.id).update("status", "Cancelado")
                        toast("Viaje cancelado")
                        onBackPressed()
                    }else if(type == "uncancel"){
                        travelsDb.document(document.id).update("status", "")
                        toast("Viaje habilitado")
                        onBackPressed()
                    }else if(type == "full"){
                        travelsDb.document(document.id).update("status", "Lleno")
                        toast("Viaje marcado como lleno")
                        onBackPressed()
                    }else if(type == "unfull"){
                        travelsDb.document(document.id).update("status", "")
                        toast("Viaje desmarcado como lleno")
                        onBackPressed()
                    }
                }
            }
    }

    private fun noYesDialog(message: String, type: String) {
        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setTitle("¿Estás seguro?")
        mAlertDialog.setMessage(message)
        mAlertDialog.setPositiveButton("Si") { dialog, id ->
            if(type == "cancel") travelStatus("cancel")
            if(type == "full") travelStatus("full")
            if(type == "delete") deleteTravel()
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }

    private fun setUpMessagesDb(){
        messagesDb = store.collection("messages")
    }

    private fun setUpChatsDb(){
        chatsDb = store.collection("chats")
    }

    private fun setUpTravelsDb(){
        travelsDb = store.collection("travels")
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }

    private fun showDatePickerDialog(){
        val newFragment = DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener(){ _, year, month, day ->
            val selectedDate = day.twoDigits() + "/" + (month + 1).twoDigits() + "/" + year
            cSelected.set(Calendar.DAY_OF_MONTH, day)
            cSelected.set(Calendar.MONTH, month)
            cSelected.set(Calendar.YEAR, year)
            date = selectedDate
            etDate.setText(selectedDate)
        })

        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun showTimePickerDialog(){
        val newFragment = TimePickerFragment.newInstance(TimePickerDialog.OnTimeSetListener(){_, hour, min ->
            val cNow = Calendar.getInstance()
            cSelected.set(Calendar.HOUR_OF_DAY, hour)
            cSelected.set(Calendar.MINUTE, min)

            val selectedTime = "${hour.twoDigits()}:${min.twoDigits()}"

            if(cSelected.timeInMillis < cNow.timeInMillis){
                toast("Fecha y hora menor a la actual")
            }else {
                time = selectedTime
                etTime.setText(selectedTime)
            }
        })

        newFragment.show(supportFragmentManager, "timePicker")
    }

    private fun setupAutoComplete(fragment: Int, flag: Int){

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(fragment) as AutocompleteSupportFragment?

        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        if(travel != null){
            if(flag == 0) autocompleteFragment.setText(travel.placeFrom)
            else autocompleteFragment.setText(travel.placeTo)
        }

        autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
            ?.setOnClickListener { view ->
                if(flag == 0) cityFrom = ""
                else cityTo = ""
                autocompleteFragment.setText("")
                view.visibility = View.GONE
            }

        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(this@NewTravelActivity, Locale.getDefault())
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(travel.placeFrom != "") menuInflater.inflate(R.menu.edit_travel_menu,  menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cancel -> {
                if(travel.status != "Cancelado"){
                    noYesDialog("No recibirás más solicitudes ni mensajes", "cancel")
                }else{
                    travelStatus("uncancel")
                }
                return true
            }
            R.id.full -> {
                if(travel.status != "Lleno"){
                    noYesDialog("No recibirás más solicitudes", "full")
                }else{
                    travelStatus("unfull")
                }
            }
            R.id.delete -> {
                noYesDialog("Se borrarán todas las solicitudes", "delete")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}