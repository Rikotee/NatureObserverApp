package com.example.natureobserverapp.fragment

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
import com.example.natureobserverapp.Categories
import com.example.natureobserverapp.R
import com.example.natureobserverapp.model.NatureObservationsWithWeatherInfoModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import java.util.*

class MapFragment : Fragment(), LocationListener {
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var mapCategorySpinner: Spinner
    private lateinit var lm: LocationManager
    private var currentLocation: Location? = null
    private var gpsLocationFound = false

    private val sharedPrefFile = "sharedpreference"
    private val sharedPrefFileSpinner = "sharedpreferenceSpinner"

    private var titleId: Long = 0
    var title: String = ""
    var description: String = ""
    private var category: String = ""
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private val categoriesList: MutableList<String> = Categories.categories.toMutableList()
    private var spinnerIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.map_title_text)

        mapCategorySpinner = view.findViewById(R.id.mapCategorySpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addToList()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapCategorySpinner.adapter = aa

        //This set spinner index value from sharedpreferences
        setSpinnerValue()

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
    }

    override fun onLocationChanged(p0: Location) {
        currentLocation = p0
        setOwnLocationMarker()

        // When GPS location is found, the network location request is removed
        if (p0.provider == LocationManager.GPS_PROVIDER && !gpsLocationFound) {
            lm.removeUpdates(this)
            checkLocationPermission()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)
            gpsLocationFound = true
        }
    }

    private fun setOwnLocationMarker() {
        if (currentLocation != null) {
            map.controller.setCenter(
                GeoPoint(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
            )

            marker.position = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
/*        if (Geocoder.isPresent()) {
            marker.title = getAddress(p0.latitude, p0.longitude)
        }*/
            marker.subDescription =
                "Lat: ${currentLocation!!.latitude}, Lon: ${currentLocation!!.longitude}, Alt: ${currentLocation!!.altitude}"
            map.overlays.add(marker)
            map.invalidate()
        }
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
        nowwim.getNatureObservationsWithWeatherInfo().observe(this) {

            val map = view?.findViewById<MapView>(R.id.mapView)
            val items = ArrayList<OverlayItem>()

            for (i in it.indices) {
                titleId = it[i].natureObservation?.id!!
                title = it[i].natureObservation?.title.toString()
                description = it[i].natureObservation?.description.toString()
                category = it[i].natureObservation?.category.toString()
                lat = it[i].natureObservation?.locationLat!!
                lon = it[i].natureObservation?.locationLon!!

                val categoryS = mapCategorySpinner.selectedItem.toString()

                if (categoryS == category) {
                    items.add(OverlayItem(titleId.toString(), title, GeoPoint(lat, lon)))
                } else if (categoryS == "All") {
                    items.add(OverlayItem(titleId.toString(), title, GeoPoint(lat, lon)))
                }
            }

            val mOverlay = ItemizedOverlayWithFocus(context,
                items, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                        val markerId = item?.title?.toLong()

                        val bundle = bundleOf("pos" to markerId)
                        //val bundle = bundleOf("pos" to it[index].natureObservation?.id)
                        requireActivity().supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace<ItemFragment>(R.id.flFragment, args = bundle)
                            addToBackStack(null)
                        }
                        return false
                    }
                })
            mOverlay.setFocusItemsOnTap(true)
            map?.overlays?.add(mOverlay)
        }
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
                replace<MapFragment>(R.id.flFragment)
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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

    override fun onDestroy() {
        super.onDestroy()
        lm.removeUpdates(this)
    }
}