package com.example.natureobserverapp.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.example.natureobserverapp.PredefinedLists
import com.example.natureobserverapp.NatureObservationWithWeatherInfo
import com.example.natureobserverapp.R
import com.example.natureobserverapp.models.NatureObservationsWithWeatherInfoModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), LocationListener {
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var mapCategorySpinner: Spinner
    private lateinit var timeFrameFilterSpinner: Spinner
    private var timeSpinnerIndex: Int = 0
    private val observationList = mutableListOf<NatureObservationWithWeatherInfo>()
    private lateinit var lm: LocationManager
    private var currentLocation: Location? = null
    private var gpsLocationFound: Boolean? = null
    private var firstOwnLocation: Boolean? = null

    private var titleId: Long = 0
    var title: String = ""
    var description: String = ""
    private var category: String = ""
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private val categoriesList: MutableList<String> = PredefinedLists.categories.toMutableList()
    private var spinnerIndex: Int = 0
    private val sharedPrefFile = "sharedpreference"
    private val sharedPrefFileSpinner = "sharedpreferenceSpinner"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.map_title_text)

        mapCategorySpinner = view.findViewById(R.id.mapCategorySpinner)
        timeFrameFilterSpinner = view.findViewById(R.id.mapTimeFrameFilterSpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addToList()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapCategorySpinner.adapter = aa

        //This set spinner index value from sharedpreferences
        setSpinnerValue()

        val aaT = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            PredefinedLists.timeFrames
        )
        aaT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeFrameFilterSpinner.adapter = aaT

        setSpinnerValue()

        gpsLocationFound = false
        firstOwnLocation = true

        //This add all markers from saved observations
        addItemMarker()

        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        // This add Marker that show my location
        initializeMapAndMarker()

        checkLocationPermission()

        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5f, this)

        mapCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateSpinner()
            }
        }

        setTimeSpinnerValue()

        timeFrameFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    timeSpinnerIndex = position
                    updateTimeSpinner(position)
                }
            }
    }

    override fun onLocationChanged(p0: Location) {
        currentLocation = p0
        setOwnLocationMarker()

        /* When GPS location is found, the network location request is removed and the map is centered
        on the user location for a more precise location */
        if (p0.provider == LocationManager.GPS_PROVIDER && gpsLocationFound == false) {
            lm.removeUpdates(this)
            checkLocationPermission()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)
            centerMapOnOwnLocation()
            gpsLocationFound = true
        }
    }

    private fun setOwnLocationMarker() {
        if (currentLocation != null) {
            // The map is centered on user location only on the first location update
            if (firstOwnLocation == true) {
                centerMapOnOwnLocation()
                firstOwnLocation = false
            }

            marker.position = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
            /*if (Geocoder.isPresent()) {
                marker.title = getAddress(p0.latitude, p0.longitude)
            }*/
            marker.subDescription =
                "Lat: ${currentLocation!!.latitude}, Lon: ${currentLocation!!.longitude}, Alt: ${currentLocation!!.altitude}"
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    private fun centerMapOnOwnLocation() {
        map.controller.setCenter(
            GeoPoint(
                currentLocation!!.latitude,
                currentLocation!!.longitude
            )
        )
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

/*    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }*/

    // This add saved observation to map
    private fun addItemMarker() {
        val nowwim: NatureObservationsWithWeatherInfoModel by viewModels()
        nowwim.getNatureObservationsWithWeatherInfo().observe(this) { it ->

            filterObservationsByTimeFrame(timeSpinnerIndex, it)

            val map = view?.findViewById<MapView>(R.id.mapView)
            val items = ArrayList<OverlayItem>()

            val sorted = observationList.sortedBy { it.natureObservation?.category }

            for (i in sorted.indices) {
                titleId = sorted[i].natureObservation?.id!!
                title = sorted[i].natureObservation?.title.toString()
                description = sorted[i].natureObservation?.description.toString()
                category = sorted[i].natureObservation?.category.toString()
                lat = sorted[i].natureObservation?.locationLat!!
                lon = sorted[i].natureObservation?.locationLon!!

                val categoryS = mapCategorySpinner.selectedItem.toString()

                if (categoryS == category) {
                    items.add(OverlayItem(titleId.toString(), title, GeoPoint(lat, lon)))
                } else if (categoryS == "All categories") {
                    items.add(OverlayItem(titleId.toString(), title, GeoPoint(lat, lon)))
                }
            }

            val mOverlay = ItemizedOverlayWithFocus(context,
                items, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                        val markerId = item?.title?.toLong()

                        val bundle = bundleOf("pos" to markerId)
                        //val bundle = bundleOf("pos" to it[index].natureObservation?.id)
                        requireActivity().supportFragmentManager.commit {
                            setCustomAnimations(
                                R.anim.slide_in,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_out
                            )
                            setReorderingAllowed(true)
                            replace<ItemFragment>(R.id.fragmentContainer, args = bundle)
                            addToBackStack(null)
                        }
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                        return false
                    }
                })
            mOverlay.setFocusItemsOnTap(true)
            map?.overlays?.add(mOverlay)
        }
        observationList.clear()
    }

    private fun updateSpinner() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFileSpinner, Context.MODE_PRIVATE)
        val oldSpinnerValue = sharedPreference?.getInt("spinnerIndex", 0)
        val categoryS = mapCategorySpinner.selectedItem.toString()
        for (i in categoriesList.indices) {
            if (categoryS == categoriesList[i]) {
                spinnerIndex = i
            }
        }

        if (oldSpinnerValue != spinnerIndex) {
            val editor = sharedPreference?.edit()
            editor?.putInt("spinnerIndex", spinnerIndex)
            editor?.commit()

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<MapFragment>(R.id.fragmentContainer)
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
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
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
            if (categoriesList[0] != "All categories") {
                categoriesList.add(0, "All categories")
            }
        }

        if (oldCategories != null) {
            for (item in oldCategories) {
                if (item !in categoriesList) {
                    categoriesList.add(item)
                }
            }
        }

        return categoriesList
    }

    private fun initializeMapAndMarker() {
        map = view!!.findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)

        marker = Marker(map)
        marker.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_pin)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }

    private fun setSpinnerValue() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFileSpinner, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("spinnerIndex", 0)
        if (newSpinnerValue != null) {
            mapCategorySpinner.setSelection(newSpinnerValue)
        }
    }

    private fun setTimeSpinnerValue() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("mapTimeFrameFilterSpinnerIndex", 0)
        if (newSpinnerValue != null) {
            timeSpinnerIndex = newSpinnerValue
            timeFrameFilterSpinner.setSelection(newSpinnerValue)
        }
    }

    private fun updateTimeSpinner(spinnerIndex: Int) {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val oldSpinnerValue = sharedPreference?.getInt("mapTimeFrameFilterSpinnerIndex", 0)
        val editor = sharedPreference?.edit()
        editor?.putInt("mapTimeFrameFilterSpinnerIndex", spinnerIndex)
        editor?.apply()

        if (oldSpinnerValue != spinnerIndex) {
            editor?.putInt("mapTimeFrameFilterSpinnerIndex", spinnerIndex)
            editor?.commit()

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<MapFragment>(R.id.fragmentContainer)
            }
        }
    }

    private fun filterObservationsByTimeFrame(
        spinnerIndex: Int,
        list: List<NatureObservationWithWeatherInfo>
    ): MutableList<NatureObservationWithWeatherInfo> {
        val selectedTimeFrame = timeFrameFilterSpinner.getItemAtPosition(spinnerIndex)

        val currentDateCalendar = Calendar.getInstance()
        val currentYear = currentDateCalendar.get(Calendar.YEAR)
        val currentMonth = currentDateCalendar.get(Calendar.MONTH)
        val currentWeek = currentDateCalendar.get(Calendar.WEEK_OF_YEAR)
        val currentDay = currentDateCalendar.get(Calendar.DAY_OF_YEAR)

        val formatter = SimpleDateFormat("d.M.yyyy hh.mm", Locale.getDefault())
        val observationDateCalendar = Calendar.getInstance()

        for (observation in list) {
            val observationDate = formatter.parse(observation.natureObservation!!.dateAndTime)

            if (observationDate != null) {
                observationDateCalendar.time = observationDate
                val observationYear = observationDateCalendar.get(Calendar.YEAR)
                val observationMonth = observationDateCalendar.get(Calendar.MONTH)
                val observationWeek = observationDateCalendar.get(Calendar.WEEK_OF_YEAR)
                val observationDay = observationDateCalendar.get(Calendar.DAY_OF_YEAR)

                when (selectedTimeFrame) {
                    "This year" -> {
                        if (observationYear == currentYear) {
                            observationList.add(observation)
                        }
                    }
                    "This month" -> {
                        if (observationYear == currentYear && observationMonth == currentMonth) {
                            observationList.add(observation)
                        }
                    }
                    "This week" -> {
                        if (observationYear == currentYear && observationWeek == currentWeek) {
                            observationList.add(observation)
                        }
                    }
                    "Today" -> {
                        if (observationYear == currentYear && observationDay == currentDay) {
                            observationList.add(observation)
                        }
                    }
                    else -> {
                        observationList.add(observation)
                    }
                }
            }
        }
        return observationList
    }

    override fun onDestroy() {
        super.onDestroy()
        lm.removeUpdates(this)
    }
}