package com.aitorramos.dogtravel.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitorramos.dogtravel.Listeners.ChatsListener
import com.aitorramos.dogtravel.Models.Chat
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.CircleTransform
import com.aitorramos.dogtravel.Utils.inflate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_chat.view.*
import java.text.SimpleDateFormat

class ChatsAdapter (private val items: List<Chat>, private val listener: ChatsListener)
    : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var currentuser: FirebaseUser
        private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        fun bind(chat: Chat, listener: ChatsListener) = with(itemView) {

            Picasso.get().load(chat.photo).transform(CircleTransform()).into(ivProfile)
            tvName.text = chat.name
            tvLastMessage.text = chat.lastMessage
            tvLastDate.text = SimpleDateFormat("dd/MM/yyy, HH:mm").format(chat.lastDate)

            currentuser = mAuth.currentUser!!
            if(chat.notification != currentuser.uid) ivNotification.visibility = View.VISIBLE
            else ivNotification.visibility = View.INVISIBLE

            if(chat.notification == "") ivNotification.visibility = View.INVISIBLE

            if(tvLastMessage.length() == 30) {
                tvLastMessage.text = tvLastMessage.text.substring(0, 27) + "..."
            }

            setOnClickListener { listener.onClick(chat, adapterPosition) }
            //delete.setOnClickListener{listener.onDelete(turn, adapterPosition)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.adapter_chat))

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ChatsAdapter.ViewHolder, position: Int) =
        holder.bind(items[position], listener)
}