package com.example.natureobserverapp.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import com.example.natureobserverapp.R
import java.io.File

class HomeFragment : Fragment() {
    private lateinit var mCurrentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newPictureButton = view.findViewById<Button>(R.id.newPictureButton)
        newPictureButton.setOnClickListener {
            val fileName = "nature_observation_picture"
            val imgPath = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(fileName, ".jpg", imgPath)

            val photoURI: Uri = FileProvider.getUriForFile(requireContext(),
                "com.example.natureobserverapp",
                imageFile)

            mCurrentPhotoPath = imageFile.absolutePath

            takePicture.launch(photoURI)
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            result ->
        if(result) {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<NewObservationFragment>(R.id.flFragment)
                addToBackStack(null)
                setFragmentResult("picturePath", bundleOf("pathKey" to mCurrentPhotoPath))
            }
        }
    }
}