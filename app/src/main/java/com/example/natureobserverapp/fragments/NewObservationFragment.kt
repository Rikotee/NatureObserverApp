package com.example.natureobserverapp.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import com.example.natureobserverapp.models.WeatherViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NewObservationFragment : Fragment(), LocationListener, SensorEventListener {
    private var pictureFilePath: String? = null
    private var currentLocation: Location? = null
    private lateinit var lm: LocationManager
    private var gpsLocationFound: Boolean? = null
    private lateinit var sm: SensorManager
    private var sLight: Sensor? = null
    private var lightValue: Double? = null
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var titleEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var addCategoryEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveObservationButton: Button
    private val categoriesList: MutableList<String> = PredefinedLists.categories.toMutableList()
    private var usePredefinedCategory = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureFilePath = requireArguments().getString("picPath")
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

        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility =
            View.GONE
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.new_observation_title_text)

        val observationImageView = view.findViewById<ImageView>(R.id.observationImageView)

        // The image is decoded and rotated in a background thread with a coroutine
        GlobalScope.launch(Dispatchers.Default) {
            val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)

            if (imageBitmap.height <= imageBitmap.width) {
                val rotatedBitmap = imageBitmap.rotate(90f)

                withContext(Dispatchers.Main) {
                    observationImageView.setImageBitmap(rotatedBitmap)
                }
            } else {
                withContext(Dispatchers.Main) {
                    observationImageView.setImageBitmap(imageBitmap)
                }
            }
        }

        titleEditText = view.findViewById(R.id.observationTitleEditText)
        categorySpinner = view.findViewById(R.id.observationCategorySpinner)
        addCategoryEditText = view.findViewById(R.id.addCategoryEditText)
        descriptionEditText = view.findViewById(R.id.observationDescriptionEditText)
        saveObservationButton = view.findViewById(R.id.saveObservationButton)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getCategoriesListWithAddedCategories()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = aa

        // First the gps location found variable is false
        gpsLocationFound = false

        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        // Sensor manager is required and the availability of light sensor is checked
        sm = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sLight = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        } else {
            Log.i("SENSOR", "Your device does not have light sensor.")
        }

        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkLocationPermissionAndRequestUpdates()

        if (currentLocation != null) {
            saveObservationButton.isEnabled = true
        }

        // If the title edit text is empty, the user will be notified, and if the user creates a new
        // category and the category edit text is empty, the user will be notified too. Else the observation
        // will be saved.
        saveObservationButton.setOnClickListener {
            if (titleEditText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.empty_title_edit_text_toast,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!usePredefinedCategory && addCategoryEditText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.empty_category_edit_text_toast,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getDataAndSave()
            }
        }

        view.findViewById<RadioGroup>(R.id.categoryOptionRadioGroup)
            .setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.selectCategoryRadioButton) {
                    addCategoryEditText.visibility = View.GONE
                    categorySpinner.visibility = View.VISIBLE
                    addCategoryEditText.text.clear()
                    usePredefinedCategory = true
                } else {
                    categorySpinner.visibility = View.GONE
                    addCategoryEditText.visibility = View.VISIBLE
                    usePredefinedCategory = false
                }
            }
    }

    // The data is saved to the database including weather data that is fetched from the network
    private fun getDataAndSave() {
        val title = titleEditText.text.toString()

        val category: String

        if (usePredefinedCategory) {
            category = categorySpinner.selectedItem.toString()
        } else {
            category = addCategoryEditText.text.toString()

            val newCategoriesSet = HashSet<String>()

            val categoriesSet = SharedPreferencesFunctions.getSharedPreferenceStringSet(
                requireActivity(),
                "newCategories", newCategoriesSet
            )

            if (categoriesSet != null) {
                newCategoriesSet.addAll(categoriesSet)
            }

            newCategoriesSet.add(category)

            SharedPreferencesFunctions.putSharedPreferenceStringSet(
                requireActivity(),
                "newCategories",
                newCategoriesSet
            )
        }

        val description = descriptionEditText.text.toString()

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("d.M.yyyy hh.mm", Locale.getDefault())
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

                weatherViewModel.getWeatherLatLon(
                    requireContext(),
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
                weatherViewModel.weatherInfo.observe(requireActivity(), {
                    val wDescription = it.weather[0].description
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
                            id.await(), wDescription, icon, temp, pressure,
                            humidity, windSpeed, windDeg, country, placeName
                        )

                        requireActivity().supportFragmentManager.popBackStack()
                    }
                })
            }
        }
    }

    private fun insertNatureObservationToDatabase(
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

    private fun insertWeatherInfoToDatabase(
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

        // When GPS location is found, the network location request is removed
        if (p0.provider == LocationManager.GPS_PROVIDER && gpsLocationFound == false) {
            gpsLocationFound = true
            checkLocationPermissionAndRequestUpdates()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        lm.removeUpdates(this)
    }

    // The location permission is checked. If permission is granted, the location updates are requested.
    fun checkLocationPermissionAndRequestUpdates() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    2
                )
            }
        } else {
            // If GPS has not yet found a location, the network based location is used. When the GPS
            // location is found, it will be used only for better accuracy.
            if (gpsLocationFound == false) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5f, this)
            } else {
                lm.removeUpdates(this)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)
            }
        }
    }

    // User added categories are fetched from Shared Preferences and added to the categories list
    private fun getCategoriesListWithAddedCategories(): MutableList<String> {
        val newCategoriesSet = HashSet<String>()

        val oldCategories = SharedPreferencesFunctions.getSharedPreferenceStringSet(
            requireActivity(),
            "newCategories", newCategoriesSet
        )

        if (oldCategories != null) {
            for (item in oldCategories) {
                if (item !in categoriesList) {
                    categoriesList.add(item)
                }
            }
        }

        return categoriesList
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}