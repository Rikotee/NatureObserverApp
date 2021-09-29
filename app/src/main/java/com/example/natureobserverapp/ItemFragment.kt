package com.example.natureobserverapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class ItemFragment() : Fragment() {

    private var id: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val position = requireArguments().getLong("pos")
        id = position
        Log.d("DBG", "onCreate id: $id")

        val cmp: NatureObservationWithWeatherInfoModel by viewModels {
            WeatherInfoModelFactory(this.requireActivity().application,
            id)
             }
         cmp.getNatureObservationsWithWeatherInfo().observe(viewLifecycleOwner) {

         }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_card_layout, container, false)

//        val name = view?.findViewById<TextView>(R.id.observationNameView)
//        name?.text = itemId
    }
}
