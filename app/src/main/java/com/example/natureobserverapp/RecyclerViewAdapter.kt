package com.example.natureobserverapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(
    private val items: List<NatureObservationWithWeatherInfo>?,
    private val clickListener: ClickListener,
    private val imageBitmapList: List<Bitmap>
) :
    RecyclerView.Adapter<RecyclerViewAdapter.ObservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row, parent, false)

        return ObservationViewHolder(view)
    }

    override fun getItemCount() = items?.size ?: 0

    inner class ObservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleTextView: TextView = view.findViewById(R.id.titleTextView)
        var dayTextView: TextView = view.findViewById(R.id.dayTextView)
        var oImageView: ImageView = view.findViewById(R.id.oImageView)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        /*val pictureFilePath = items?.get(position)?.natureObservation?.picturePath
        val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)*/
        holder.titleTextView.text = items?.get(position)?.natureObservation?.title
        holder.dayTextView.text = items?.get(position)?.natureObservation?.dateAndTime
        holder.oImageView.setImageBitmap(imageBitmapList[position])

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(items?.get(position)?.natureObservation?.id)
        }
    }

    interface ClickListener {
        fun onItemClick(observation: Long?)
    }
}