package com.example.natureobserverapp.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.natureobserverapp.NatureObservationWithWeatherInfoModel
import com.example.natureobserverapp.NatureObservationWithWeatherInfoModelFactory
import com.example.natureobserverapp.R

class ItemFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.item_title_text)

        val id = requireArguments().getLong("pos")

        Log.d("DBG", "onCreate id: $id")

        val nowwim: NatureObservationWithWeatherInfoModel by viewModels {
            NatureObservationWithWeatherInfoModelFactory(
                this.requireActivity().application,
                id
            )
        }

        nowwim.getNatureObservationWithWeatherInfo().observe(viewLifecycleOwner) {
            val pictureFilePath = it.natureObservation?.picturePath
            val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)
            view.findViewById<ImageView>(R.id.photoView).setImageBitmap(imageBitmap)

            val name = view.findViewById<TextView>(R.id.observationNameView)
            val category = view.findViewById<TextView>(R.id.categoryView)
            val description = view.findViewById<TextView>(R.id.infoTextView)

            name?.text = it.natureObservation?.title.toString()
            category?.text = it.natureObservation?.category.toString()
            description?.text = it.natureObservation?.description.toString()

            view.findViewById<TextView>(R.id.descriptionTextView)?.text =
                it.weatherInfo?.description
            view.findViewById<TextView>(R.id.temperatureTextView)?.text =
                getString(R.string.temperature_text, String.format("%.0f", it.weatherInfo?.temp))
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