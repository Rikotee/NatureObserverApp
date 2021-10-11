package com.example.natureobserverapp.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.natureobserverapp.R
import com.example.natureobserverapp.WeatherIconApi
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModel
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ItemFragment : Fragment() {
    private var observationId: Long? = null
    private var lightText: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observationId = requireArguments().getLong("pos")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_card_layout, container, false)

        val cardWeatherInfoContainer =
            rootView.findViewById<LinearLayout>(R.id.cardWeatherInfoContainer)
        val weatherViewLayout = inflater.inflate(R.layout.weather_view, null, false)
        cardWeatherInfoContainer.addView(weatherViewLayout)

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.item_fragment_menu, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.item_title_text)

        if (observationId != null) {
            val nowwim: NatureObservationWithWeatherInfoModel by viewModels {
                NatureObservationWithWeatherInfoModelFactory(
                    requireActivity().application,
                    observationId!!
                )
            }

            nowwim.getNatureObservationWithWeatherInfo().observe(viewLifecycleOwner) {
                val pictureFilePath = it.natureObservation?.picturePath
                val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)

                if (imageBitmap.height <= imageBitmap.width ){

                    val rotatedBitmap = imageBitmap.rotate(90f)

                    view.findViewById<ImageView>(R.id.photoView).setImageBitmap(rotatedBitmap)
                }else{
                    view.findViewById<ImageView>(R.id.photoView).setImageBitmap(imageBitmap)
                }

                val imageView = view.findViewById(R.id.photoView) as ImageView

                imageView.setOnClickListener {
                    onItemClick(observationId)
                }

                it.natureObservation?.lightValue?.let { it1 -> lightValueToText(it1) }

                val name = view.findViewById<TextView>(R.id.observationNameView)
                val date = view.findViewById<TextView>(R.id.dateTextView)
                val category = view.findViewById<TextView>(R.id.categoryView)
                val description = view.findViewById<TextView>(R.id.infoTextView)
                val light = view.findViewById<TextView>(R.id.lightInfoTextView)

                name?.text = it.natureObservation?.title.toString()
                category?.text = it.natureObservation?.category.toString()
                description?.text = it.natureObservation?.description.toString()
                date?.text = it.natureObservation?.dateAndTime.toString()

                light?.text = getString(R.string.light_description_text, lightText)

                view.findViewById<TextView>(R.id.descriptionTextView)?.text =
                    it.weatherInfo?.description
                view.findViewById<TextView>(R.id.temperatureTextView)?.text =
                    getString(
                        R.string.temperature_text,
                        String.format("%.0f", it.weatherInfo?.temp)
                    )
                view.findViewById<TextView>(R.id.pressureTextView)?.text =
                    getString(R.string.pressure_text, it.weatherInfo?.pressure)
                view.findViewById<TextView>(R.id.humidityTextView)?.text =
                    getString(R.string.humidity_text, it.weatherInfo?.humidity)
                view.findViewById<TextView>(R.id.windSpeedTextView)?.text =
                    getString(R.string.wind_speed_text, it.weatherInfo?.windSpeed)
                view.findViewById<TextView>(R.id.windDirectionTextView)?.text =
                    getString(R.string.wind_direction_text, it.weatherInfo?.windDeg)
                view.findViewById<TextView>(R.id.placeNameCountryTextView)?.text = getString(
                    R.string.place_name_country_text,
                    it.weatherInfo?.placeName,
                    it.weatherInfo?.country
                )

                lifecycleScope.launch(Dispatchers.Main) {
                    val iconImageBitmap = withContext(Dispatchers.IO) {
                        it.weatherInfo?.icon?.let { icon -> WeatherIconApi.getWeatherIcon(icon) }
                    }
                    if (iconImageBitmap != null) {
                        view.findViewById<ImageView>(R.id.iconImageView)
                            ?.setImageBitmap(iconImageBitmap)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editObservationItem -> {
                val bundle = bundleOf("observationId" to observationId)

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<EditFragment>(R.id.flFragment, args = bundle)
                    addToBackStack(null)
                }
                true
            }
            R.id.deleteObservationItem -> {
                val deleteDialogFragment = DeleteDialogFragment()
                if (observationId != null) {
                    deleteDialogFragment.arguments = bundleOf("observationId" to observationId!!)
                }
                deleteDialogFragment.show(requireActivity().supportFragmentManager, "delete")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onItemClick(observation: Long?) {
        val bundle = bundleOf("imageId" to observation)

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ImageFragment>(R.id.flFragment, args = bundle)
            addToBackStack(null)
        }
    }

    private fun lightValueToText(lightValue: Double){

        when (lightValue) {
            in 0.0..3.0 -> lightText = getText(R.string.Darkness).toString()
            in 3.0..200.0 -> lightText = getText(R.string.Very_dark_overcast_day).toString()
            in 200.0..320.0 -> lightText = getText(R.string.Train_station_platforms).toString()
            in 320.0..500.0 -> lightText = getText(R.string.Office_lighting).toString()
            in 500.0..1000.0 -> lightText =
                getText(R.string.Sunrise_or_sunset_on_a_clear_day).toString()
            in 1000.0..10000.0 -> lightText =
                getText(R.string.Overcast_day_or_typical_TV_studio_lighting).toString()
            in 10000.0..25000.0 -> lightText =
                getText(R.string.Full_daylight_but_not_direct_sun).toString()
            in 25000.0..100000.0 -> lightText = getText(R.string.Direct_sunlight).toString()
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}