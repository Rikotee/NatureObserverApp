package com.example.natureobserverapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class MainViewModel: ViewModel() {
    private val repository: WebServiceRepository = WebServiceRepository()

    var searchCity: String = ""

    val hits = liveData(Dispatchers.IO) {
        try {
        val retrievedWeather = repository.getCustomPosts(searchCity)
        emit(retrievedWeather)
        } catch (t: Throwable) {
            Log.d("WEATHER", t.toString())
        }
    }

    fun getWeather(search: String){
        searchCity = search
    }
}