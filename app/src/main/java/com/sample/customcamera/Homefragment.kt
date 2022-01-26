package com.sample.customcamera

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.sample.customcamera.databinding.FragmentHomefragmentBinding
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener


class Homefragment : Fragment(R.layout.fragment_homefragment) {

    private var binding: FragmentHomefragmentBinding? = null
    private val PICK_IMAGE_ID = 1009 // the number doesn't matter
    private var filePath : String? = null
    private var imagequality : Int? = null
    private var imagename : String? =null
    private var edittext : String? = null
    private var filesiz : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("requestKey", this,
            { requestKey: String?, bundle: Bundle ->
                val IMAGE_REQ_CODE = bundle.getInt("IMAGE_REQ_CODE")

                if (IMAGE_REQ_CODE == PICK_IMAGE_ID) {
                    try {
                        filePath = bundle.getString("filepath")
                        imagename = bundle.getString("imgname")
                        filesiz = bundle.getString("filesize")
                        val kb = "KB"
                        binding?.path?.text = filePath
                        binding?.imagnam?.text = imagename
                        binding?.filesize?.text = "$filesiz$kb"
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
//            buttonHome0.setOnClickListener{
//                val b = Bundle()
//                b.putInt("cameraselector", 0)
//                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
//                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
//            }
//
            buttonHome1.setOnClickListener{
                edittext = filetext.text.toString().trim()
                if (edittext.isNullOrEmpty() || imagequality!! < 2){
                    Toast.makeText(requireContext(), "Filename cannot be blank or Image quality cannot be less then 1", Toast.LENGTH_SHORT).show()
                }else{
                    val b = Bundle()
                    b.putString("imagename", edittext)
                    b.putInt("cameraselector", 3)
                    b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                    b.putInt("quality", imagequality!!)
                    Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
                }
            }
//
//            buttonHome2.setOnClickListener{
//                val b = Bundle()
//                b.putInt("cameraselector", 2)
//                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
//                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
//            }
//
//            buttonHome3.setOnClickListener{
//                val b = Bundle()
//                b.putInt("cameraselector", 3)
//                b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
//                Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
//            }
            percentBar.max = 100
            percentBar.progress = 0

            percentBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    imagequality = progress
                    percentText.text = imagequality.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            buttonHome4.setOnClickListener{
                edittext = filetext.text.toString().trim()
                if (edittext.isNullOrEmpty() || imagequality!! < 2){
                    Toast.makeText(requireContext(), "Filename cannot be blank or Image quality cannot be less then 1", Toast.LENGTH_SHORT).show()
                }else{
                    val b = Bundle()
                    b.putString("imagename", edittext)
                    b.putInt("cameraselector", 4)
                    b.putInt("quality", imagequality!!)
                    b.putInt("IMAGE_REQ_CODE", PICK_IMAGE_ID)
                    Navigation.findNavController(requireView()).navigate(R.id.action_homefragment_to_cameraXFragmnets, b)
                }
            }
        }
        return binding?.root
    }

    private fun loadImage(filePath: String) {
//        val file = File(filePath)
        Log.d("LogTag", filePath.toString())
        Glide.with(requireView())
            .load(filePath)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(binding?.imageHome!!)
    }
}