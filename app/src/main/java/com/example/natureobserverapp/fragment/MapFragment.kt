package com.example.natureobserverapp.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import com.example.natureobserverapp.Categories
import com.example.natureobserverapp.NatureObservationsWithWeatherInfoModel
import com.example.natureobserverapp.R
import com.example.natureobserverapp.model.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import java.util.ArrayList

class MapFragment : Fragment(), LocationListener {
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var viewModel: MainViewModel
    private lateinit var mapCategorySpinner: Spinner
    private lateinit var updateButton: Button

    private val sharedPrefFile = "sharedpreference"

    var titleId: Long = 0
    var title: String = ""
    var description: String = ""
    var category: String = ""
    var lat: Double = 0.0
    var lon: Double = 0.0

    var spinnerValue: Int = 0
    var spinnerIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mapCategorySpinner = view.findViewById(R.id.mapCategorySpinner)
        updateButton = view.findViewById(R.id.btnUpdate)

        updateButton.setOnClickListener{
            updateMarkers()
        }

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Categories.categoriesAll
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapCategorySpinner.adapter = aa

        val sharedPreference = this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("spinnerIndex",0)
        if (newSpinnerValue != null) {
            spinnerValue = newSpinnerValue
        }
        Log.d("DBG", "onviewcreated  spinnervalue: $newSpinnerValue")

        if (newSpinnerValue != null) {
            mapCategorySpinner.setSelection(newSpinnerValue)
        }

        //This add all markers from saved observations
        addItemMarker()

        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        map = view.findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)

        marker = Marker(map)
        marker.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_pin)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

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
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3f, this)
    }

    override fun onLocationChanged(p0: Location) {
        Log.d("NATURE", "new latitude: ${p0.latitude} and longitude: ${p0.longitude}")

        // weather info
        viewModel.getWeatherLatLon(p0.latitude, p0.longitude)
        viewModel.hits.observe(this, {

/*            val city = view?.findViewById<TextView>(R.id.cityView)
            if (city != null) {
                city.text =
                    it.name + "\ntemp: " + it.main.temp.toString() + " °C\n" + it.weather[0].description
            }
            Log.d("NATURE", it.name)
            Log.d("NATURE", "temp: " + it.main.temp.toString() + " °C")
            Log.d("NATURE", it.weather[0].description)*/

        })

        map.controller.setCenter(GeoPoint(p0.latitude, p0.longitude))

        marker.position = GeoPoint(p0.latitude, p0.longitude)
        if (Geocoder.isPresent()) {
            marker.title = getAddress(p0.latitude, p0.longitude)
        }
        marker.subDescription = "Lat: ${p0.latitude}, Lon: ${p0.longitude}, Alt: ${p0.altitude}"
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }


    // This add saved observation to map
    private fun addItemMarker() {

        val cmp: NatureObservationsWithWeatherInfoModel by viewModels()
        cmp.getNatureObservationsWithWeatherInfo().observe(this) {

            val map = view?.findViewById<MapView>(R.id.mapView)
            val items = ArrayList<OverlayItem>()

            //Log.d("DBG", items.size.toString())

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

    private fun updateMarkers(){

        val categoryS = mapCategorySpinner.selectedItem.toString()

        when (categoryS) {
            "All" -> spinnerIndex = 0
            "Animal" -> spinnerIndex = 1
            "Bird" -> spinnerIndex = 2
            "Fish" -> spinnerIndex = 3
            "Insect" -> spinnerIndex = 4
            "Tree" -> spinnerIndex = 5
            "Plant" -> spinnerIndex = 6
            "Flower" -> spinnerIndex = 7
            "Rock" -> spinnerIndex = 8
            "Nest" -> spinnerIndex = 9
            "Cocoon" -> spinnerIndex = 10
            "Spider" -> spinnerIndex = 11
        }

        val sharedPreference = this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putInt("spinnerIndex",spinnerIndex)
        editor?.commit()

        Log.d("DBG", "updatemarker  spinnerindex: $spinnerIndex")

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<MapFragment>(R.id.flFragment)
            addToBackStack(null)
        }
    }
}