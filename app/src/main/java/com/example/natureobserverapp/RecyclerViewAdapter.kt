package com.example.natureobserverapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RecyclerViewAdapter(private val items: List<NatureObservationWithWeatherInfo>?) :
    RecyclerView.Adapter<RecyclerViewAdapter.ObservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row, parent, false)

        return ObservationViewHolder(view)
    }

     override fun getItemCount() = items?.size ?: 0

    inner class ObservationViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var titleTextView: TextView = view.findViewById(R.id.titleTextView)
    }


    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.titleTextView.text = items?.get(position)?.natureObservation?.title
        // Example of weather info
        Log.d("DBG", "${items?.get(position)?.weatherInfo?.temp}, ${items?.get(position)?.weatherInfo?.placeName}")
    }
}