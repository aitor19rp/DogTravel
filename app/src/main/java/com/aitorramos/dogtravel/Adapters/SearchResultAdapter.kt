package com.aitorramos.dogtravel.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitorramos.dogtravel.Listeners.MyTravelsListener
import com.aitorramos.dogtravel.Models.Travel
import com.aitorramos.dogtravel.R
import com.aitorramos.dogtravel.Utils.CircleTransform
import com.aitorramos.dogtravel.Utils.inflate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_search_result.view.*


class SearchResultAdapter (private val items: List<Travel>, private val listener: MyTravelsListener)
    : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>(){

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(travel: Travel, listener: MyTravelsListener) = with(itemView){

            tvCityFrom.text = travel.placeFrom
            tvCityTo.text = travel.placeTo
            tvDateTime.text = "${travel.date}, ${travel.time}"
            tvName.text = travel.name
            Picasso.get().load(travel.photo).transform(CircleTransform()).into(ivProfile)

            setOnClickListener{listener.onClick(travel, adapterPosition)}
            //delete.setOnClickListener{listener.onDelete(turn, adapterPosition)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.adapter_search_result))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: SearchResultAdapter.ViewHolder, position: Int) = holder.bind(items[position], listener)
}