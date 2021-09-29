package com.example.natureobserverapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.*
import java.io.File

class HomeFragment : Fragment() {
    private lateinit var mCurrentPhotoPath: String
    internal var activityCallBack: HomeFragmentListener? = null

    interface HomeFragmentListener {
        fun onNewObservationButtonClick(picturePath: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallBack = context as HomeFragmentListener
    }

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

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home_title_text)

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
            activityCallBack!!.onNewObservationButtonClick(mCurrentPhotoPath)
        }
    }
}