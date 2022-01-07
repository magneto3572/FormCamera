package com.sample.customcamera

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.sample.customcamera.databinding.FragmentHomefragmentBinding
import java.io.File
import java.lang.NullPointerException

class Homefragment : Fragment(R.layout.fragment_homefragment) {

    private var binding: FragmentHomefragmentBinding? = null
    private val PICK_IMAGE_ID = 1009 // the number doesn't matter
    private var filePath : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("requestKey", this,
            { requestKey: String?, bundle: Bundle ->
                val IMAGE_REQ_CODE = bundle.getInt("IMAGE_REQ_CODE")
                if (IMAGE_REQ_CODE == PICK_IMAGE_ID) {
                    try {
                        filePath = bundle.getString("filepath")
                        loadImage(filePath!!)
                    } catch (e: NullPointerException) {
                        Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "no image captured!", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomefragmentBinding.inflate(inflater)


        binding?.apply {
            buttonHome0.setOnClickListener{
                val b = Bundle()
                b.putInt("cameraselector", 0)
                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
            }

            buttonHome1.setOnClickListener{
                val b = Bundle()
                b.putInt("cameraselector", 1)
                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
            }

            buttonHome2.setOnClickListener{
                val b = Bundle()
                b.putInt("cameraselector", 2)
                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
            }

            buttonHome3.setOnClickListener{
                val b = Bundle()
                b.putInt("cameraselector", 3)
                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
            }
            buttonHome4.setOnClickListener{
                val b = Bundle()
                b.putInt("cameraselector", 4)
                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
            }
        }
        return binding?.root
    }

    private fun loadImage(filePath: String) {
//        val file = File(filePath)
        Glide.with(requireView())
            .load(filePath)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(binding?.imageHome!!)
    }
}