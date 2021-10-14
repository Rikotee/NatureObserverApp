package com.example.natureobserverapp.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.natureobserverapp.R
import com.example.natureobserverapp.models.NatureObservationModel
import com.example.natureobserverapp.models.NatureObservationModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : Fragment() {
    private var imageId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageId = requireArguments().getLong("imageId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (imageId != null) {
            val nom: NatureObservationModel by viewModels {
                NatureObservationModelFactory(
                    requireActivity().application,
                    imageId!!
                )
            }

            nom.getNatureObservation().observe(viewLifecycleOwner) {
                val pictureFilePath = it.picturePath
                val imageView = view.findViewById<ImageView>(R.id.fr_imageView)

                // The image is decoded and rotated in a background thread with a coroutine
                GlobalScope.launch(Dispatchers.Default) {
                    val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)

                    if (imageBitmap.height <= imageBitmap.width) {
                        val rotatedBitmap = imageBitmap.rotate(90f)

                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(rotatedBitmap)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(imageBitmap)
                        }
                    }
                }
            }
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}