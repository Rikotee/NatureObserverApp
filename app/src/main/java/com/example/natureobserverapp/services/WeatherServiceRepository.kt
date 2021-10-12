package com.example.natureobserverapp.services

class WeatherServiceRepository() {
    private val call = WeatherApi.service

    suspend fun getWeatherData(key: String, lat: Double, lon: Double) = call.getWeather(key, lat, lon)
}