package com.example.natureobserverapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NatureObservationModel(application: Application) : AndroidViewModel(application) {
    private val natureObservations: LiveData<List<NatureObservation>> =
        NatureObservationDB.get(getApplication()).natureObservationDao().getAll()

    fun getNatureObservations() = natureObservations
}

class NatureObservationWithWeatherInfoModel(application: Application, natureObservationId: Long) :
    AndroidViewModel(application) {
    private val natureObservationWithWeatherInfo: LiveData<NatureObservationWithWeatherInfo> =
        NatureObservationDB.get(getApplication()).natureObservationDao()
            .getNatureObservationWithWeatherInfo(natureObservationId)

    fun getNatureObservationWithWeatherInfo() = natureObservationWithWeatherInfo
}

class NatureObservationsWithWeatherInfoModel(application: Application) :
    AndroidViewModel(application) {
    private val natureObservationsWithWeatherInfo: LiveData<List<NatureObservationWithWeatherInfo>> =
        NatureObservationDB.get(getApplication()).natureObservationDao()
            .getAllNatureObservationsWithWeatherInfo()

    fun getNatureObservationsWithWeatherInfo() = natureObservationsWithWeatherInfo
}

class WeatherInfoModel(application: Application, natureObservationId: Long) :
    AndroidViewModel(application) {
    private val weatherInfos: LiveData<List<WeatherInfo>> =
        NatureObservationDB.get(getApplication()).weatherInfoDao()
            .getWeatherInfosOfNatureObservation(natureObservationId)

    fun getWeatherInfos() = weatherInfos
}

class NatureObservationWithWeatherInfoModelFactory(
    private val application: Application,
    private val natureObservationId: Long
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        NatureObservationWithWeatherInfoModel(application, natureObservationId) as T
}

class WeatherInfoModelFactory(
    private val application: Application,
    private val natureObservationId: Long
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        WeatherInfoModel(application, natureObservationId) as T
}