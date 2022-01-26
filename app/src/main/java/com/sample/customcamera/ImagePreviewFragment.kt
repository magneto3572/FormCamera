package com.sample.customcamera


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sample.customcamera.databinding.FragmentImagePreviewBinding
import java.io.*
import java.util.*


class ImagePreviewFragment : DialogFragment() {

    companion object{
        var bitmap: Bitmap?= null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private var binding : FragmentImagePreviewBinding? = null
    var IMAGE_REQ_CODE = 0
    var imgquality=0
    var imagetext :String? = null
    private var imagename : String? =null
    private var filesize : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagePreviewBinding.inflate(inflater)

        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.btnCancel?.setOnClickListener { dismiss() }

        try {
            val bundle = arguments
            IMAGE_REQ_CODE = arguments?.getInt("IMAGE_REQ_CODE")!!
            imgquality = arguments?.getInt("imgquality")!!
            imagetext = arguments?.getString("imagetext")!!
            bitmap = bundle?.getParcelable<Bitmap>("bitmap")!!


            Glide.with(requireContext())
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.testimageview)
//            binding?.testimageview?.setImageBitmap(bitmap)

            binding?.btnSave?.setOnClickListener {

                try {
                    val bundle = Bundle()
                    bundle.putInt("IMAGE_REQ_CODE",IMAGE_REQ_CODE)
//                bundle.putParcelable("bitmap", bitmap)
                    bundle.putString("filepath", createFilePath(bitmap!!))
                    bundle.putString("imgname", imagename)
                    bundle.putString("filesize", filesize)
                    parentFragmentManager.setFragmentResult("requestKey", bundle)
                    dismiss()
                }catch (e:Exception){
                    e.printStackTrace()
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }
        }catch (e : IllegalArgumentException){
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createFilePath(bitmap1: Bitmap): String? {
        var filePath: String? = null
        val root =Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        val myDir = File("$root/Custom_saved_image");
        myDir.mkdirs();
        val generator = Random();
        var n = 10000;
        n = generator.nextInt(n);
        val fname = "$imagetext.jpg";
        val file =  File(myDir, fname);
        imagename = fname
        if (file.exists()) file.delete();
        try {
            val out =  FileOutputStream(file);
            bitmap1.compress(Bitmap.CompressFormat.JPEG, imgquality, out);
            filePath = file.path;
            val fileSizeInKB: Long = file.length() / 1024
            filesize = fileSizeInKB.toString()
            out.flush();
            out.close();

        } catch (e: java.lang.Exception) {
            e.printStackTrace();
        }

        return  filePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}