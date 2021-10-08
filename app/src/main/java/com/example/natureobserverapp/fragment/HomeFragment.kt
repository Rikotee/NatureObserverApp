package com.example.natureobserverapp.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.natureobserverapp.Categories
import com.example.natureobserverapp.NatureObservation
import com.example.natureobserverapp.R
import com.example.natureobserverapp.WeatherIconApi
import com.example.natureobserverapp.model.NatureObservationsModel
import com.example.natureobserverapp.model.WeatherViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), LocationListener {
    private var activityCallBack: HomeFragmentListener? = null
    private lateinit var viewModel: WeatherViewModel

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

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.home_title_text)

        checkLocationPermission()

        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 500f, this)

        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        val newPictureButton = view.findViewById<Button>(R.id.newPictureButton)

        newPictureButton.setOnClickListener {
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
        if (isAdded) {
            viewModel.getWeatherLatLon(p0.latitude, p0.longitude)
            viewModel.hits.observe(requireActivity(), {
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

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    private fun checkLocationPermission() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }
}