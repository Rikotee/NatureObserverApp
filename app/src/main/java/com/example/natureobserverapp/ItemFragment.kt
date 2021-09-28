package com.example.natureobserverapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels

class ItemFragment() : Fragment() {

    private var observationId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
        }

        setFragmentResultListener("id") { requestKey, bundle ->
            observationId = bundle.getLong("idKey")

        }

        Log.d("DBG", "setFragmentResultListener $observationId")


/*        val cmp: NatureObservationWithWeatherInfoModel by viewModels {
            WeatherInfoModelFactory(this.requireActivity().application,
            observationId)
             }
         cmp.getNatureObservationsWithWeatherInfo().observe(viewLifecycleOwner) {


         }*/
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

/*    companion object {
        @JvmStatic
        fun newInstance(id: String) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, id)
                    Log.d("DBG", "ItemFragment newInstance id $id")
                }
            }
    }*/
}
