package com.example.natureobserverapp

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.setFragmentResultListener

class NewObservationFragment : Fragment() {
    private var pictureFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_observation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("picturePath") { requestKey, bundle ->
            pictureFilePath = bundle.getString("pathKey")
            val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)
            view.findViewById<ImageView>(R.id.observationImageView).setImageBitmap(imageBitmap)
        }
    }
}