package com.aitorramos.dogtravel.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitorramos.dogtravel.Activities.MessagesActivity
import com.aitorramos.dogtravel.Adapters.ChatsAdapter
import com.aitorramos.dogtravel.Listeners.ChatsListener
import com.aitorramos.dogtravel.Models.Chat
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.dogtravel.Utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_my_request_tab.view.*

class MyRequestTab : Fragment() {

    private lateinit var _view: View
    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var chatsDb: CollectionReference
    private var chatsList: ArrayList<Chat> = ArrayList()
    private lateinit var adapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_my_request_tab, container, false)

        setUpCurrentUser()
        setUpChatsDb()
        setUpRecyclerView()
        getChats()

        return _view
    }

    private fun getChats(){
        chatsDb.whereEqualTo("sender", currentuser.uid)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot> {
                override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                    p1?.let {
                        activity!!.toast("Error al obtener los datos")
                        return
                    }

                    p0?.let {
                        chatsList.clear()
                        for(document in it.documents){
                            var chat = Chat()
                            var data1: List<String>
                            var data2: List<String>
                            chat.chatId = document.getString("chatId").toString()
                            chat.sender = document.getString("sender").toString()
                            chat.receiver = document.getString("receiver").toString()
                            chat.travel = document.getString("travel").toString()
                            chat.lastMessage = document.getString("lastMessage").toString()
                            chat.lastDate = document.getTimestamp("lastDate")!!.toDate()
                            chat.notification = document.getString("notification").toString()
                            data1 = document.getString("data1").toString().split("#&")
                            data2 = document.getString("data2").toString().split("#&")
                            if(data1[0] == currentuser.uid){
                                chat.photo = data1[1]
                                chat.name = data1[2]
                            }else{
                                chat.photo = data2[1]
                                chat.name = data2[2]
                            }
                            chatsList.add(chat)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            })
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        _view.recyclerView.setHasFixedSize(true)
        _view.recyclerView.layoutManager = layoutManager
        _view.recyclerView.itemAnimator = DefaultItemAnimator()

        adapter = ChatsAdapter(chatsList, object: ChatsListener {
            override fun onClick(chat: Chat, position: Int) {
                activity!!.goToActivity<MessagesActivity>{
                    putExtra("chatId", chat.chatId)
                    putExtra("travelId", chat.travel)
                    putExtra("sender", chat.sender)
                    putExtra("receiver", chat.receiver)
                }
            }

            override fun onDelete(chat: Chat, position: Int) {
            }
        })

        _view.recyclerView.adapter = adapter
    }

    private fun setUpChatsDb(){
        chatsDb = store.collection("chats")
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
    }
}