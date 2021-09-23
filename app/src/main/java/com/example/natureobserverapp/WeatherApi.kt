package com.example.natureobserverapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object WeatherApi {

    const val URL = "http://api.openweathermap.org/"

    interface Service {
        @GET("data/2.5/weather?&units=metric&APPID=b208e0a34797d6a37c6c9dd719be6cc4")
        suspend fun getWeather(

            @Query("q") q: String,

        ): WeatherModel.Result
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(Service::class.java)!!
}