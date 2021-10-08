package com.example.natureobserverapp.service

class WebServiceRepository() {
    private val call = WeatherApi.service

    // latitude, longitude
    suspend fun getWeatherPosts(lat: Double, lon: Double) = call.getWeather(lat, lon)
}