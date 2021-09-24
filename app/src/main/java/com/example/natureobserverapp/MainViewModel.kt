package com.example.natureobserverapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class MainViewModel: ViewModel() {
    private val repository: WebServiceRepository = WebServiceRepository()

    private var searchCity: String = ""
    private var searchLat: Double = 0.0
    private var searchLon: Double = 0.0

    val hits = liveData(Dispatchers.IO) {
        try {
        val retrievedWeather = repository.getCustomPosts(searchLat, searchLon)
        emit(retrievedWeather)
        } catch (t: Throwable) {
            Log.d("WEATHER", t.toString())
        }
    }

    // city
    fun getWeatherCity(search: String){
        searchCity = search
    }

    // latitude, longitude
    fun getWeatherLatLon(lat: Double, lon: Double){
        searchLat = lat
        searchLon = lon
    }
}