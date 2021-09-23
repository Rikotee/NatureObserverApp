package com.example.natureobserverapp

class WebServiceRepository() {
    private val call = WeatherApi.service

    // city
    //suspend fun getCustomPosts(q: String) = call.getWeather(q)

    // latitude, longitude
    suspend fun getCustomPosts(lat: Double, lon: Double) = call.getWeather(lat, lon)
}