package com.example.natureobserverapp.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.natureobserverapp.R
import com.example.natureobserverapp.WeatherIconApi
import com.example.natureobserverapp.model.NatureObservationModel
import com.example.natureobserverapp.model.NatureObservationModelFactory
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModel
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFragment : Fragment() {
    var observationId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observationId = requireArguments().getLong("observationId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.edit_title_text)
        setCurrentObservationValues()
    }

    private fun setCurrentObservationValues() {
        if (observationId != null) {
            val nom: NatureObservationModel by viewModels {
                NatureObservationModelFactory(
                    requireActivity().application,
                    observationId!!
                )
            }

            nom.getNatureObservation().observe(viewLifecycleOwner) {
                view?.findViewById<EditText>(R.id.editFragmentTitleEditText)?.setText(it.title)
                view?.findViewById<EditText>(R.id.editFragmentDescriptionEditText)
                    ?.setText(it.description)

            }
        }
    }
}