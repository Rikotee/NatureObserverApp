package com.example.natureobserverapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.net.URL

object WeatherIconApi {
    const val baseURL = "http://openweathermap.org/img/wn/"

    fun getWeatherIcon(iconName: String) : Bitmap? {
        val URL = URL("${baseURL}${iconName}@2x.png")
        try {
            val connection = URL.openConnection()
            val istream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(istream)
            return bitmap
        } catch (e: Exception) {
            Log.d("Error", "Icon download error")
            return null
        }
    }
}