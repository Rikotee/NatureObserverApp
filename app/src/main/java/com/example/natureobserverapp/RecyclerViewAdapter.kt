package com.example.natureobserverapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(
    private val items: List<NatureObservationWithWeatherInfo>?,
    private val clickListener: ClickListener
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
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.titleTextView.text = items?.get(position)?.natureObservation?.title
        holder.dayTextView.text = items?.get(position)?.natureObservation?.dateAndTime

        holder.itemView.setOnClickListener {

            clickListener.onItemClick(items?.get(position)?.natureObservation?.id)
        }
    }

    interface ClickListener {
        fun onItemClick(observation: Long?)
    }
}