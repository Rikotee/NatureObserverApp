package com.example.natureobserverapp.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.natureobserverapp.R
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModel
import com.example.natureobserverapp.model.NatureObservationWithWeatherInfoModelFactory

class ImageFragment : Fragment() {
    private var imageId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageId = requireArguments().getLong("imageId")

        arguments?.let {
        }
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

        if (imageId != null) {
            val nowwim: NatureObservationWithWeatherInfoModel by viewModels {
                NatureObservationWithWeatherInfoModelFactory(
                    requireActivity().application,
                    imageId!!
                )
            }

            nowwim.getNatureObservationWithWeatherInfo().observe(viewLifecycleOwner) {
                val pictureFilePath = it.natureObservation?.picturePath
                val imageBitmap = BitmapFactory.decodeFile(pictureFilePath)

                if (imageBitmap.height <= imageBitmap.width ){

                    val rotatedBitmap = imageBitmap.rotate(90f)

                    view.findViewById<ImageView>(R.id.fr_imageView).setImageBitmap(rotatedBitmap)
                }else{
                    view.findViewById<ImageView>(R.id.fr_imageView).setImageBitmap(imageBitmap)
                }
            }
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}