package com.sample.customcamera


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagePreviewBinding.inflate(inflater)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.btnCancel?.setOnClickListener { dismiss() }

        try {
            val bundle = arguments
            IMAGE_REQ_CODE = arguments?.getInt("IMAGE_REQ_CODE")!!
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

    @Throws(IOException::class)
    private fun createFilePath(bitmap1: Bitmap): String? {
        var filePath: String? = null
        val f = File(requireContext().cacheDir, UUID.randomUUID().toString() + ".jpeg")
        f.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(f)
            filePath = f.path
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos!!.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return filePath

//        String root = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES).toString();
//        File myDir = new File(root + "/Taskmo_saved_images");
//        myDir.mkdirs();
//        Random generator = new Random();
//        int n = 10000;
//        n = generator.nextInt(n);
//        String fname = "Image-" + n + ".jpg";
//        File file = new File(myDir, fname);
//        if (file.exists()) file.delete();
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, out);
//            filePath = file.getPath();
//            out.flush();
//            out.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}