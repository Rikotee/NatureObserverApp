package com.example.natureobserverapp.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.natureobserverapp.*
import com.example.natureobserverapp.model.MainViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NewObservationFragment : Fragment(), LocationListener, SensorEventListener {
    private var pictureFilePath: String? = null
    private var currentLocation: Location? = null
    private lateinit var categorySpinner: Spinner
    private lateinit var sm: SensorManager
    private var sLight: Sensor? = null
    private var lightValue: Double? = 500.0
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_observation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.new_observation_title_text)

        val pictureFilePath = requireArguments().getString("picPath")
        val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)
        view.findViewById<ImageView>(R.id.observationImageView).setImageBitmap(imageBitmap)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        categorySpinner = view.findViewById(R.id.observationCategorySpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Categories.categories
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = aa

        sm = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sLight = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        } else {
            Log.i("SENSOR", "Your device does not have light sensor.")
        }

        if ((Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(
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

        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 3f, this)

        view.findViewById<Button>(R.id.saveObservationButton).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.observationTitleEditText).text.toString()
            val category = categorySpinner.selectedItem.toString()
            val description =
                view.findViewById<EditText>(R.id.observationDescriptionEditText).text.toString()

            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd.M.yyyy hh.mm", Locale.getDefault())
            val currentDate = formatter.format(date)

            GlobalScope.launch(Dispatchers.Main) {
                val id = GlobalScope.async(Dispatchers.IO) {
                    insertNatureObservationToDatabase(
                        title, category, description, pictureFilePath!!, currentDate,
                        currentLocation!!.latitude, currentLocation!!.longitude, lightValue!!
                    )
                }

                viewModel.getWeatherLatLon(currentLocation!!.latitude, currentLocation!!.longitude)
                viewModel.hits.observe(requireActivity(), {
                    val description = it.weather[0].description
                    val icon = it.weather[0].icon
                    val temp = it.main.temp
                    val pressure = it.main.pressure
                    val humidity = it.main.humidity
                    val windSpeed = it.wind.speed
                    val windDeg = it.wind.deg
                    val country = it.sys.country
                    val placeName = it.name

                    GlobalScope.launch(Dispatchers.IO) {
                        insertWeatherInfoToDatabase(
                            id.await(), description, icon, temp, pressure,
                            humidity, windSpeed, windDeg, country, placeName
                        )
                    }
                })
            }
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private suspend fun insertNatureObservationToDatabase(
        title: String,
        category: String,
        description: String,
        picturePath: String,
        dateAndTime: String,
        locationLat: Double,
        locationLon: Double,
        lightValue: Double
    ): Long {
        val id = db.natureObservationDao().insert(
            NatureObservation(
                0, title, category, description,
                picturePath, dateAndTime, locationLat, locationLon, lightValue
            )
        )
        Log.d("Database", "Nature observation inserted, id: $id")
        return id
    }

    private suspend fun insertWeatherInfoToDatabase(
        observationId: Long,
        description: String,
        icon: String,
        temp: Double,
        pressure: Long,
        humidity: Long,
        windSpeed: Double,
        windDeg: Long,
        country: String,
        placeName: String
    ) {
        val id = db.weatherInfoDao().insert(
            WeatherInfo(
                0,
                observationId,
                description,
                icon,
                temp,
                pressure,
                humidity,
                windSpeed,
                windDeg,
                country,
                placeName
            )
        )
        Log.d("Database", "Weather info inserted, id: $id")
    }

    override fun onLocationChanged(p0: Location) {
        currentLocation = p0
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor == sLight) {
            lightValue = event.values[0].toDouble()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i("SENSOR", "Sensor accuracy changed.")
    }

    override fun onResume() {
        super.onResume()
        sLight?.also {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sm.unregisterListener(this)
    }
}