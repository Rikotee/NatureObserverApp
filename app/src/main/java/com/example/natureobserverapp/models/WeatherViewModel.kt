package com.example.natureobserverapp.models

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.natureobserverapp.services.WeatherServiceRepository
import kotlinx.coroutines.Dispatchers

// View model for weather data retrieving
class WeatherViewModel : ViewModel() {
    private val repository: WeatherServiceRepository = WeatherServiceRepository()
    private var searchLat: Double? = null
    private var searchLon: Double? = null
    lateinit var weatherInfo: LiveData<WeatherModel.Result>

    fun getWeatherLatLon(context: Context, lat: Double, lon: Double) {
        searchLat = lat
        searchLon = lon

        // The Open Weather Map API key is fetched
        val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["openWeatherMapApiKeyValue"]
        val key = value.toString()

        weatherInfo = liveData(Dispatchers.IO) {
            try {
                val retrievedWeather = repository.getWeatherData(key, searchLat!!, searchLon!!)
                emit(retrievedWeather)
            } catch (t: Throwable) {
                Log.d("WEATHER", t.toString())
            }
        }
    }
}