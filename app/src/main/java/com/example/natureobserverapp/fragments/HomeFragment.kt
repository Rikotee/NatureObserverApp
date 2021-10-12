package com.example.natureobserverapp.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.natureobserverapp.R
import com.example.natureobserverapp.services.WeatherIconApi
import com.example.natureobserverapp.models.WeatherViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeFragment : Fragment(), LocationListener {
    private var activityCallBack: HomeFragmentListener? = null
    private lateinit var weatherViewModel: WeatherViewModel
    private var currentLocation: Location? = null
    private lateinit var lm: LocationManager
    private var gpsLocationFound = false

    interface HomeFragmentListener {
        fun onNewObservationButtonClick(picturePath: String, photoURI: Uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallBack = context as HomeFragmentListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val weatherViewContainerLayout =
            rootView.findViewById<LinearLayout>(R.id.weatherViewContainerLayout)
        val weatherViewLayout = inflater.inflate(R.layout.weather_view, null, false)
        weatherViewContainerLayout.addView(weatherViewLayout)

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.app_name)

        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        checkLocationPermission()

        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 500f, this)
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 500f, this)

        val newObservationButton = view.findViewById<Button>(R.id.newObservationButton)

        newObservationButton.setOnClickListener {
            val fileName = "nature_observation_picture"
            val imgPath = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(fileName, ".jpg", imgPath)

            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.natureobserverapp",
                imageFile
            )

            val mCurrentPhotoPath = imageFile.absolutePath
            activityCallBack!!.onNewObservationButtonClick(mCurrentPhotoPath, photoURI)
        }
    }

    override fun onLocationChanged(p0: Location) {
        currentLocation = p0
        getWeatherInfo()

        // When GPS location is found, the network location request is removed
        if (p0.provider == LocationManager.GPS_PROVIDER && !gpsLocationFound) {
            lm.removeUpdates(this)
            checkLocationPermission()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 500f, this)
            gpsLocationFound = true
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    private fun getWeatherInfo() {
        if (isAdded) {
            if (currentLocation != null) {
                weatherViewModel.getWeatherLatLon(
                    requireContext(),
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
                weatherViewModel.weatherInfo.observe(requireActivity(), {
                    view?.findViewById<TextView>(R.id.descriptionTextView)?.text =
                        it.weather[0].description
                    view?.findViewById<TextView>(R.id.temperatureTextView)?.text =
                        getString(R.string.temperature_text, String.format("%.0f", it.main.temp))
                    view?.findViewById<TextView>(R.id.pressureTextView)?.text =
                        getString(R.string.pressure_text, it.main.pressure.toString())
                    view?.findViewById<TextView>(R.id.humidityTextView)?.text =
                        getString(R.string.humidity_text, it.main.humidity.toString())
                    view?.findViewById<TextView>(R.id.windSpeedTextView)?.text =
                        getString(R.string.wind_speed_text, it.wind.speed.toString())
                    view?.findViewById<TextView>(R.id.windDirectionTextView)?.text =
                        getString(R.string.wind_direction_text, it.wind.deg.toString())
                    view?.findViewById<TextView>(R.id.placeNameCountryTextView)?.text =
                        getString(R.string.place_name_country_text, it.name, it.sys.country)

                    lifecycleScope.launch(Dispatchers.Main) {
                        val iconImageBitmap = withContext(Dispatchers.IO) {
                            WeatherIconApi.getWeatherIcon(it.weather[0].icon)
                        }
                        if (iconImageBitmap != null) {
                            view?.findViewById<ImageView>(R.id.iconImageView)
                                ?.setImageBitmap(iconImageBitmap)
                        }
                    }
                })
            }
        }
    }

    private fun checkLocationPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lm.removeUpdates(this)
    }
}