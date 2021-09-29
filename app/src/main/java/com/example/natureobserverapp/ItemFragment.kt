package com.example.natureobserverapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class ItemFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = requireArguments().getLong("pos")

        Log.d("DBG", "onCreate id: $id")


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = requireArguments().getLong("pos")

        Log.d("DBG", "onCreate id: $id")

        val cmp: NatureObservationWithWeatherInfoModel by viewModels {
            NatureObservationWithWeatherInfoModelFactory(
                this.requireActivity().application,
                id
            )
        }

        cmp.getNatureObservationWithWeatherInfo().observe(viewLifecycleOwner) {

            val name = view.findViewById<TextView>(R.id.observationNameView)
            val category = view.findViewById<TextView>(R.id.categoryView)
            val description = view.findViewById<TextView>(R.id.infoTextView)
            val temp = view.findViewById<TextView>(R.id.weatherTempView)
            val weatherDes = view.findViewById<TextView>(R.id.weatherDesView)

            name?.text = it.natureObservation?.title.toString()
            category?.text = it.natureObservation?.category.toString()
            description?.text = it.natureObservation?.description.toString()
            temp?.text = it.weatherInfo?.temp.toString()
            weatherDes?.text = it.weatherInfo?.description.toString()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_card_layout, container, false)
    }
}
