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
import androidx.lifecycle.ViewModelProvider
import com.example.natureobserverapp.*
import com.example.natureobserverapp.model.WeatherViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NewObservationFragment : Fragment(), LocationListener, SensorEventListener {
    private var pictureFilePath: String? = null
    private var currentLocation: Location? = null
    private lateinit var categorySpinner: Spinner
    private lateinit var sm: SensorManager
    private var sLight: Sensor? = null
    private var lightValue: Double? = null
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }
    private lateinit var viewModel: WeatherViewModel
    private lateinit var saveObservationButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var addCategoryEditText: EditText
    private val sharedPrefFile = "sharedpreference"
    private val categoriesList: MutableList<String> = Categories.categories.toMutableList()

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

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.new_observation_title_text)

        saveObservationButton = view.findViewById(R.id.saveObservationButton)
        titleEditText = view.findViewById(R.id.observationTitleEditText)
        addCategoryEditText = view.findViewById(R.id.addCategoryEditText)

        pictureFilePath = requireArguments().getString("picPath")
        val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)
        view.findViewById<ImageView>(R.id.observationImageView).setImageBitmap(imageBitmap)

        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        categorySpinner = view.findViewById(R.id.observationCategorySpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addToList()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = aa

        sm = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sLight = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        } else {
            Log.i("SENSOR", "Your device does not have light sensor.")
        }

        checkLocationPermission()

        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 3f, this)

        saveObservationButton.setOnClickListener {
            if (titleEditText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.empty_title_edit_text_toast,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getDataAndSave()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun getDataAndSave() {
        val title = titleEditText.text.toString()
        val addedCategory = addCategoryEditText.text.toString()
        var category = categorySpinner.selectedItem.toString()

        if (addedCategory != "") {
            category = addedCategory

            val newCategoriesSet = HashSet<String>()

            val sharedPreference =
                this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

            val categoriesSet = sharedPreference?.getStringSet("newCategories", newCategoriesSet)

            if (categoriesSet != null) {
                newCategoriesSet.addAll(categoriesSet)
            }

            newCategoriesSet.add(category)

            val editor = sharedPreference?.edit()
            editor?.putStringSet("newCategories", newCategoriesSet)
            editor?.apply()
        }

        val description =
            view?.findViewById<EditText>(R.id.observationDescriptionEditText)?.text.toString()

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd.M.yyyy hh.mm", Locale.getDefault())
        val currentDate = formatter.format(date)

        val lightValue = lightValue ?: 0.0

        if (pictureFilePath != null && currentLocation != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val id = GlobalScope.async(Dispatchers.IO) {
                    insertNatureObservationToDatabase(
                        title, category, description, pictureFilePath!!, currentDate,
                        currentLocation!!.latitude, currentLocation!!.longitude, lightValue
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
        saveObservationButton.isEnabled = true
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
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

    private fun checkLocationPermission() {
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
    }

    private fun addToList(): MutableList<String> {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val newCategoriesSet = HashSet<String>()

        val oldCategories = sharedPreference?.getStringSet(
            "newCategories",
            newCategoriesSet
        )

        for (i in categoriesList.indices) {
            if (categoriesList[0] != "All") {
                categoriesList.add(0, "All")
            }
        }

        if (oldCategories != null) {
            for (item in oldCategories){
                if (item !in categoriesList) { categoriesList.add(item) }
            }
        }

        return categoriesList
    }
}