package com.example.natureobserverapp.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.natureobserverapp.NatureObservation
import com.example.natureobserverapp.NatureObservationDB
import com.example.natureobserverapp.NatureObservationWithWeatherInfo
import com.example.natureobserverapp.WeatherInfo

class NatureObservationsModel(application: Application) : AndroidViewModel(application) {
    private val natureObservations: LiveData<List<NatureObservation>> =
        NatureObservationDB.get(getApplication()).natureObservationDao().getAll()

    fun getNatureObservations() = natureObservations
}

class NatureObservationModel(application: Application, natureObservationId: Long) :
    AndroidViewModel(application) {
    private val natureObservation: LiveData<NatureObservation> =
        NatureObservationDB.get(getApplication()).natureObservationDao()
            .getNatureObservation(natureObservationId)

    fun getNatureObservation() = natureObservation
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

class WeatherInfosModel(application: Application, natureObservationId: Long) :
    AndroidViewModel(application) {
    private val weatherInfos: LiveData<List<WeatherInfo>> =
        NatureObservationDB.get(getApplication()).weatherInfoDao()
            .getWeatherInfosOfNatureObservation(natureObservationId)

    fun getWeatherInfos() = weatherInfos
}

class NatureObservationModelFactory(
    private val application: Application,
    private val natureObservationId: Long
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        NatureObservationModel(application, natureObservationId) as T
}

class NatureObservationWithWeatherInfoModelFactory(
    private val application: Application,
    private val natureObservationId: Long
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        NatureObservationWithWeatherInfoModel(application, natureObservationId) as T
}

class WeatherInfosModelFactory(
    private val application: Application,
    private val natureObservationId: Long
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        WeatherInfosModel(application, natureObservationId) as T
}