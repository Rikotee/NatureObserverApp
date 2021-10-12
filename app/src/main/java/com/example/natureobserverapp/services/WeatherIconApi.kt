package com.example.natureobserverapp.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.net.URL

object WeatherIconApi {
    private const val baseURL = "http://openweathermap.org/img/wn/"

    fun getWeatherIcon(iconName: String) : Bitmap? {
        val URL = URL("$baseURL${iconName}@2x.png")
        return try {
            val connection = URL.openConnection()
            val istream = connection.getInputStream()
            BitmapFactory.decodeStream(istream)
        } catch (e: Exception) {
            Log.d("Error", "Icon download error")
            null
        }
    }
}