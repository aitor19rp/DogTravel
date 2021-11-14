package com.aitorramos.dogtravel.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitorramos.dogtravel.Models.Message
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.inflate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_chat_left.view.*
import kotlinx.android.synthetic.main.adapter_chat_left_photo.view.*
import kotlinx.android.synthetic.main.adapter_chat_right.view.*
import kotlinx.android.synthetic.main.adapter_chat_right_photo.view.*
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(val items: List<Message>, val userId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val GLOBAL_MESSAGE = 1
    private val MY_MESSAGE = 2
    private val MY_MESSAGE_PHOTO = 3
    private val GLOBAL_MESSAGE_PHOTO = 4

    private val layoutRight = R.layout.adapter_chat_right
    private val layoutLeft = R.layout.adapter_chat_left
    private val layoutRightPhoto = R.layout.adapter_chat_right_photo
    private val layoutLeftPhoto = R.layout.adapter_chat_left_photo

    class ViewHolderR(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(message: Message) = with(itemView){
            tvMessageRight.text = message.message
            if(SimpleDateFormat("dd/MM/yyyy").format(message.sendAt) < SimpleDateFormat("dd/MM/yyyy").format(Date())) tvTimeRight.text = SimpleDateFormat("dd/MM/yyyy").format(message.sendAt)
            else tvTimeRight.text = SimpleDateFormat("HH:mm").format(message.sendAt)
        }
    }

    class ViewHolderL(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(message: Message) = with(itemView){
            tvMessageLeft.text = message.message
            if(SimpleDateFormat("dd/MM/yyyy").format(message.sendAt) < SimpleDateFormat("dd/MM/yyyy").format(Date())) tvTimeLeft.text = SimpleDateFormat("dd/MM/yyyy").format(message.sendAt)
            else tvTimeLeft.text = SimpleDateFormat("HH:mm").format(message.sendAt)
        }
    }

    class ViewHolderRP(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(message: Message) = with(itemView){
            Picasso.get().load(message.photo).into(ivPhotoRight)
            if(SimpleDateFormat("dd/MM/yyyy").format(message.sendAt) < SimpleDateFormat("dd/MM/yyyy").format(Date())) tvTimeRightPhoto.text = SimpleDateFormat("dd/MM/yyyy").format(message.sendAt)
            else tvTimeRightPhoto.text = SimpleDateFormat("HH:mm").format(message.sendAt)
        }
    }

    class ViewHolderLP(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(message: Message) = with(itemView){
            Picasso.get().load(message.photo).into(ivPhotoLeft)
            if(SimpleDateFormat("dd/MM/yyyy").format(message.sendAt) < SimpleDateFormat("dd/MM/yyyy").format(Date())) tvTimeLeftPhoto.text = SimpleDateFormat("dd/MM/yyyy").format(message.sendAt)
            else tvTimeLeftPhoto.text = SimpleDateFormat("HH:mm").format(message.sendAt)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(items[position].sender == userId && items[position].photo == "") return MY_MESSAGE
        else if(items[position].sender != userId && items[position].photo == "") return GLOBAL_MESSAGE
        else if(items[position].sender == userId && items[position].photo != "") return MY_MESSAGE_PHOTO
        else if(items[position].sender != userId && items[position].photo != "") return GLOBAL_MESSAGE_PHOTO

        return 0
    }
    //override fun getItemViewType(position: Int) = if(items[position].sender == userId) MY_MESSAGE else GLOBAL_MESSAGE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            MY_MESSAGE -> ViewHolderR(parent.inflate(layoutRight))
            GLOBAL_MESSAGE -> ViewHolderL(parent.inflate(layoutLeft))
            MY_MESSAGE_PHOTO -> ViewHolderRP(parent.inflate(layoutRightPhoto))
            else -> ViewHolderLP(parent.inflate(layoutLeftPhoto))
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            MY_MESSAGE -> (holder as ViewHolderR).bind(items[position])
            GLOBAL_MESSAGE -> (holder as ViewHolderL).bind(items[position])
            MY_MESSAGE_PHOTO -> (holder as ViewHolderRP).bind(items[position])
            GLOBAL_MESSAGE_PHOTO -> (holder as ViewHolderLP).bind(items[position])
        }
    }

}