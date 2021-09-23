package com.example.natureobserverapp

class WebServiceRepository() {
    private val call = WeatherApi.service

    suspend fun getCustomPosts(q: String) = call.getWeather(q)

}