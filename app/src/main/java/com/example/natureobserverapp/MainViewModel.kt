package com.example.natureobserverapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class MainViewModel: ViewModel() {
    private val repository: WebServiceRepository = WebServiceRepository()

    var searchCity: String = ""
    var searchLat: Double = 0.0
    var searchLot: Double = 0.0

    val hits = liveData(Dispatchers.IO) {
        try {
        val retrievedWeather = repository.getCustomPosts(searchLat, searchLot)
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
    fun getWeatherLatLot(lat: Double, lon: Double){
        searchLat = lat
        searchLot = lon
    }
}