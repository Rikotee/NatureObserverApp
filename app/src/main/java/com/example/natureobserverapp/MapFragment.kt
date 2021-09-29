package com.example.natureobserverapp

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
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

        //////////////////////////////////////


        getLonLanAddMarker(6)






////////////////////////////////////////////////


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

            val city = view?.findViewById<TextView>(R.id.cityView)
            if (city != null) {
                city.text =
                    it.name + "\ntemp: " + it.main.temp.toString() + " °C\n" + it.weather[0].description
            }
            Log.d("NATURE", it.name)
            Log.d("NATURE", "temp: " + it.main.temp.toString() + " °C")
            Log.d("NATURE", it.weather[0].description)

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

// when take picture this adds marker to map

    private fun addItemMarker(id: Long, lat: Double, lon: Double, title: String, snippet: String) {

        val map = view?.findViewById<MapView>(R.id.mapView)

        val items = ArrayList<OverlayItem>()
        items.add(OverlayItem(title, snippet, GeoPoint(lat, lon)))
        val mOverlay = ItemizedOverlayWithFocus(context,
            items, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    val bundle = bundleOf("pos" to id)
                    requireActivity().supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ItemFragment>(R.id.flFragment, args = bundle)
                        addToBackStack(null)
                        setFragmentResult("id", bundleOf("idKey" to id))
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    val bundle = bundleOf("pos" to id)
                    requireActivity().supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ItemFragment>(R.id.flFragment, args = bundle)
                        addToBackStack(null)
                        setFragmentResult("id", bundleOf("idKey" to id))
                    }
                    return false
                }
            })
        mOverlay.setFocusItemsOnTap(true)
        map?.overlays?.add(mOverlay)
    }

    private fun getLonLanAddMarker(id: Int){

        val idL: Long = id.toLong()

        val cmp: NatureObservationWithWeatherInfoModel by viewModels {
            NatureObservationWithWeatherInfoModelFactory(
                this.requireActivity().application,
                idL
            )
        }

        cmp.getNatureObservationWithWeatherInfo().observe(viewLifecycleOwner) {

            val lat = it.natureObservation?.locationLat
            val lon = it.natureObservation?.locationLon
            Log.d("DBG", lat.toString())
            Log.d("DBG", lon.toString())

            if (lat != null) {
                if (lon != null) {
                    addItemMarker(idL, lat, lon, "testTitle", "testSnippet")
                }
            }
        }
    }
}