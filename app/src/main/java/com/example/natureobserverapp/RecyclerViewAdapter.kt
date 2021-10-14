package com.example.natureobserverapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        var oImageView: ImageView = view.findViewById(R.id.oImageView)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.titleTextView.text = items?.get(position)?.natureObservation?.title
        holder.dayTextView.text = items?.get(position)?.natureObservation?.dateAndTime

        GlobalScope.launch(Dispatchers.Default) {
            val imageBitmap = image(position)

            if (imageBitmap != null) {
                if (imageBitmap.height <= imageBitmap.width) {
                    val rotatedBitmap = imageBitmap.rotate(90f)

                    withContext(Dispatchers.Main) {
                        holder.oImageView.setImageBitmap(rotatedBitmap)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        holder.oImageView.setImageBitmap(imageBitmap)
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(items?.get(position)?.natureObservation?.id)
        }
    }

    private fun image(position: Int): Bitmap? {
        val pictureFilePath = items?.get(position)?.natureObservation?.picturePath
        val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)
        return getResizedBitmap(imageBitmap, 200)
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    interface ClickListener {
        fun onItemClick(observation: Long?)
    }
}