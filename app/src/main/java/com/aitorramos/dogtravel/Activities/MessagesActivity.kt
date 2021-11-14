package com.aitorramos.dogtravel.Activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitorramos.dogtravel.Adapters.MessagesAdapter
import com.aitorramos.dogtravel.Models.Message
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.aitorramos.mylibrary.ToolbarActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_messages.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class MessagesActivity : ToolbarActivity() {

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var messagesDb: CollectionReference
    private lateinit var chatDb: CollectionReference
    private lateinit var blockDb: CollectionReference
    private lateinit var travelDb: CollectionReference
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var messagesList: ArrayList<Message> = ArrayList()
    private lateinit var adapter: MessagesAdapter
    private var travel = Travel()
    private var chatId = ""
    private var flag = ""
    private var userToBlock = ""
    private var chatSubscription: ListenerRegistration? = null

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAA3dy1ivU:APA91bGMcijXt0UrkM212artJi9cf0_D1iEmu2hjNGJ3MjbIiKux61LVtiFCT0CpjS1WtgXeFZjkCv47Sdu_b2mQd9MX8cQ46h7yd3RBbnYm_GpxKuktQCaKYnvbPqulEu5gTtwyF5yb"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        toolbarToLoad(toolbar as Toolbar)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
        enableHomeDisplay(true)
        setTitle("")

        setUpTravelDb()

        flag = intent.extras!!.getString("flag").toString()
        if(flag == "1") chatId = UUID.randomUUID().toString()
        else chatId = intent.extras!!.getString("chatId").toString()

        setUpCurrentUser()
        setUpMessagesDb()
        setUpChatDb()
        setUpTravelDb()
        setUpBlockDb()
        setUpRecyclerView()
        getMessages()

        if(intent.extras!!.getString("sender").toString() == currentuser.uid)
            userToBlock = intent.extras!!.getString("receiver").toString()
        else userToBlock = intent.extras!!.getString("sender").toString()

        ivLook.setOnClickListener{
            if(travel.user == currentuser.uid) {
                goToActivity<NewTravelActivity>{ putExtra(
                    "travel", travel as Serializable
                ) }
            }else{
                goToActivity<ReserveActivity>{ putExtra(
                    "travel", travel as Serializable
                ) }
            }
        }

        ivSendPhoto.setOnClickListener{
            openGalleryForImage()
        }

        btSendMessage.setOnClickListener{
            val messageText = etMessage.text.toString()
            if(messageText.isNotEmpty()){
                val message = Message(currentuser.uid, messageText, Date(), chatId)
                sendMessage(message)
                etMessage.setText("")
            }
        }
    }

    private fun setData(){
        tvDataTime.text = travel.date + ", " + travel.time
        tvCityFrom.text = travel.placeFrom
        tvCityTo.text = travel.placeTo
        tvStatus.text = travel.status
        if(travel.status == "Cancelado"){
            etMessage.isEnabled = false
            ivSendPhoto.isEnabled = false
            btSendMessage.isEnabled= false
        }else{
            etMessage.isEnabled = true
            ivSendPhoto.isEnabled = true
            btSendMessage.isEnabled= true
        }
        getBlocks()
    }

    private fun getTravel(){
        travelDb.whereEqualTo("id", intent.extras!!.getString("travelId").toString())
            .get()
            .addOnSuccessListener {
                for(document in it){
                    travel.id = document.getString("id").toString()
                    travel.user = document.getString("user").toString()
                    travel.dogSize = document.getString("dogSize").toString()
                    travel.placeFrom = document.getString("placeFrom").toString()
                    travel.placeTo = document.getString("placeTo").toString()
                    travel.date = document.getString("date").toString()
                    travel.time = document.getString("time").toString()
                    travel.photo = document.getString("photo").toString()
                    travel.name = document.getString("name").toString()
                    travel.status = document.getString("status").toString()
                }
                setData()
            }
    }

    private fun blockUnblockUser(){
        blockDb
            .whereEqualTo("user", currentuser.uid)
            .whereEqualTo("blocked", userToBlock)
            .get()
            .addOnSuccessListener {
                var flag = 0
                for(document in it){
                    flag = 1
                    blockDb.document(document.id).delete().addOnSuccessListener {
                        toast("Usuario desbloqueado")
                        onBackPressed()
                    }
                }
                if(flag == 0){
                    val block = hashMapOf(
                        "user" to currentuser.uid,
                        "blocked" to userToBlock
                    )

                    blockDb.add(block).addOnSuccessListener {
                        toast("Usuario bloqueado")
                        etMessage.setText("Usuario bloqueado")
                        onBackPressed()
                    }
                }
            }
    }

    private fun getBlocks(){
        blockDb
            .whereEqualTo("user", currentuser.uid)
            .whereEqualTo("blocked", userToBlock)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot>{
                override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                    p1?.let {
                        toast("Error al obtener los datos")
                        Log.e("ERROR", it.toString())
                        return
                    }

                    p0?.let {
                        if(it.size() > 0) {
                            etMessage.setText("Usuario bloqueado")
                            etMessage.isEnabled = false
                            btSendMessage.isEnabled = false
                            ivSendPhoto.isEnabled = false
                        }
                    }
                }
            })

        blockDb
            .whereEqualTo("blocked", currentuser.uid)
            .whereEqualTo("user", userToBlock)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot>{
                override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                    p1?.let {
                        toast("Error al obtener los datos")
                        Log.e("ERROR", it.toString())
                        return
                    }

                    p0?.let {
                        if(it.size() > 0) {
                            etMessage.setText("El usuario te ha bloqueado")
                            etMessage.isEnabled = false
                            btSendMessage.isEnabled = false
                            ivSendPhoto.isEnabled = false
                        }
                    }
                }
            })
    }

    private fun updateChat(message: String, user: String){
        chatDb.whereEqualTo("chatId", chatId)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    if(message != "") {
                        chatDb.document(document.id)
                            .update(
                                "lastMessage", message,
                                "lastDate", Date(),
                                "notification", user
                            )
                    }else{
                        if(document.getString("notification").toString() != currentuser.uid && document.getString("notification").toString() != "")
                        chatDb.document(document.id)
                            .update("notification", user)
                    }
                }
            }
    }

    private fun addChat(message: String){
        val chat = hashMapOf(
            "chatId" to chatId,
            "sender" to currentuser.uid,
            "receiver" to travel.user,
            "data1" to currentuser.uid + "#&" + travel.photo + "#&" + travel.name,
            "data2" to travel.user + "#&" + currentuser.photoUrl.toString() + "#&" + currentuser.displayName!!.split(" ").get(0),
            "travel" to travel.id,
            "lastMessage" to message,
            "lastDate" to Date(),
            "notification" to currentuser.uid)

        chatDb.add(chat).addOnFailureListener {
            toast("Error al enviar")
            Log.e("ERROR", it.toString())
        }.addOnSuccessListener { setUpNotification() }
    }

    private fun sendPhoto(url: String){
        val message = Message(currentuser.uid, "Imagen", Date(), chatId, url)
        val newMessage = hashMapOf(
            "sender" to message.sender,
            "message" to message.message,
            "sendAt" to message.sendAt,
            "chatId" to message.chatId,
            "photo" to message.photo )

        messagesDb.add(newMessage).addOnFailureListener{
            toast("Error al enviar")
            Log.e("ERROR", it.toString())
        }.addOnSuccessListener {
            if(messagesList.size == 1) {
                addChat(message.message)
            }else{
                updateChat(message.message, currentuser.uid)
            }
        }
    }

    private fun sendMessage(message: Message){
        val newMessage = hashMapOf(
            "sender" to message.sender,
            "message" to message.message,
            "sendAt" to message.sendAt,
            "chatId" to message.chatId)

        messagesDb.add(newMessage).addOnFailureListener{
            toast("Error al enviar")
            Log.e("ERROR", it.toString())
        }.addOnSuccessListener {
            if(messagesList.size == 1) {
                addChat(message.message)
            }else{
                updateChat(message.message, currentuser.uid)
            }
        }
    }

    private fun checkExists(){
        travelDb.whereEqualTo("id", travel.id)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot> {
                override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                    p0?.let {
                        if (it.isEmpty) {
                            ivSendPhoto.isEnabled = false
                            etMessage.isEnabled = false
                            btSendMessage.isEnabled = false
                            etMessage.setText("El viaje ha sido eliminado")
                        }else{
                            ivSendPhoto.isEnabled = true
                            etMessage.isEnabled = true
                            btSendMessage.isEnabled = true
                            etMessage.setText("")
                        }
                    }
                }
            })
    }

    private fun getMessages(){
        chatSubscription = messagesDb
            .orderBy("sendAt", Query.Direction.DESCENDING)
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot>{
            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                p1?.let {
                    toast("Error al obtener los datos")
                    Log.e("ERROR", it.toString())
                    return
                }

                p0?.let {
                    messagesList.clear()
                    val messages = it.toObjects(Message::class.java)
                    messagesList.addAll(messages.asReversed())
                    adapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(messagesList.size)
                }
            }

        })
    }

    private fun setUpBlockDb(){
        blockDb = store.collection("blocks")
    }

    private fun setUpTravelDb(){
        travelDb = store.collection("travels")
    }

    private fun setUpChatDb(){
        chatDb = store.collection("chats")
    }

    private fun setUpMessagesDb(){
        messagesDb = store.collection("messages")
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        adapter = MessagesAdapter(messagesList, currentuser.uid)

        recyclerView.adapter = adapter
    }

    private fun setUpNotification(){
        val topic = "/topics/" + travel.user

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", "Nueva solicitud")
            notifcationBody.put("message", currentuser.displayName!!.split(" ").get(0) + " ha contactado contigo")   //Enter your notification message
            notification.put("to", topic)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
                //msg.setText("")
            },
            Response.ErrorListener {
                Toast.makeText(this, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private fun uploadImage(bitmap: Bitmap) {
        val ref = storageReference.child("chat/" + UUID.randomUUID() + ".jpeg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = ref.putBytes(data)
        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                sendPhoto("$it")
            }.addOnFailureListener() {
                toast("Error al enviar imagen")
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1 && data != null && data.data != null){
            val imagePath = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagePath)

            uploadImage(bitmap)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        if(intent.extras!!.get("travel") != null) {
            travel = intent.extras!!.get("travel") as Travel
            setData()
            checkExists()
        }else{
            getTravel()
        }
        super.onResume()
    }

    override fun onDestroy() {
        chatSubscription?.remove()
        super.onDestroy()
    }

    override fun onBackPressed() {
        updateChat("", "")
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_messages,  menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.block -> {
                blockUnblockUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}