package com.example.natureobserverapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.natureobserverapp.service.WebServiceRepository
import kotlinx.coroutines.Dispatchers

class WeatherViewModel : ViewModel() {
    private val repository: WebServiceRepository = WebServiceRepository()

    private var searchLat: Double = 0.0
    private var searchLon: Double = 0.0

    lateinit var weatherInfo: LiveData<WeatherModel.Result>

    fun getWeatherLatLon(lat: Double, lon: Double) {
        searchLat = lat
        searchLon = lon

        weatherInfo = liveData(Dispatchers.IO) {
            try {
                val retrievedWeather = repository.getWeatherData(searchLat, searchLon)
                emit(retrievedWeather)
            } catch (t: Throwable) {
                Log.d("WEATHER", t.toString())
            }
        }
    }
}