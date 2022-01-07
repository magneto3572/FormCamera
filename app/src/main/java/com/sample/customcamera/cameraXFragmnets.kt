package com.sample.customcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaActionSound
import android.opengl.Visibility
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.common.util.concurrent.ListenableFuture
import com.sample.customcamera.databinding.FragmentCameraXFragmnetsBinding
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class cameraXFragmnets : DialogFragment() {

    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var formattedDate: String
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var binding: FragmentCameraXFragmnetsBinding? = null
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var bitmap: Bitmap? = null
    private var address: String? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraselectorfront: Int = 0
    private var cameraSelector : CameraSelector? = null

    private var IMAGE_REQ_CODE: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IMAGE_REQ_CODE = arguments?.getInt("IMAGE_REQ_CODE") ?: 0
        address = arguments?.getString("address")
        cameraselectorfront = arguments?.getInt("cameraselector")?:0
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()
        setupCameraProvider()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraXFragmnetsBinding.inflate(inflater)

        if (cameraselectorfront ==1 ){
            binding?.apply{
                layoutcut.visibility = View.VISIBLE
                aligntext.visibility = View.VISIBLE
            }
        }else if(cameraselectorfront == 2){
            binding?.apply{
                layoutcut.visibility = View.INVISIBLE
                aligntext.visibility = View.GONE
            }
        }else{
            binding?.apply{
                layoutcut.visibility = View.INVISIBLE
                aligntext.visibility = View.GONE
            }
        }

        if (allPermissionsGranted()) {
//            startCamera()
            if (TextUtils.isEmpty(address)) {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                getLastLocation()
            }else{
                binding?.location?.text = address
            }

            binding?.cameraCaptureButton?.isEnabled = true

            try {
                setupCameraProvider()
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )

        }

        binding?.cameraCaptureButton?.setOnClickListener {
            capturePhoto()
            binding?.cameraCaptureButton?.isEnabled = false
        }
        binding?.fabFlash?.setOnClickListener {
//            toggleFlash()
            changeFlashMode()
        }
        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
        formattedDate = df.format(c)
        binding?.date?.text = formattedDate
//        outputDirectory = getOutputDirectory(requireContext());
        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding!!.root
    }

    private fun changeFlashMode() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            imageCapture?.flashMode = flashMode
            updateFlashModeButton()
        }
    }

    private fun setupCameraProvider() {
        if (cameraProvider == null) {
            ProcessCameraProvider.getInstance(requireActivity()).also { provider ->
                provider.addListener({
                    cameraProvider = provider.get()
                    if (cameraProvider != null) {
                        bindCamera()
                    }

                }, ContextCompat.getMainExecutor(requireActivity()))
            }
        }

    }

    private fun createPreview(screenSize: Size): Preview = Preview.Builder()
        .setTargetResolution(screenSize)
        .build()

    private fun createCameraSelectorback(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    private fun createCameraSelectorfront(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        .build()

    private fun createCameraCapture(screenAspectRatio: Rational): ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setTargetAspectRatio(screenAspectRatio.toInt())
        .setFlashMode(flashMode)
        .build()


    private fun bindCamera() {
        try {
            if (imageCapture == null)
            {
                val metrics = DisplayMetrics().also { binding?.viewFinder?.display?.getRealMetrics(it) }
                val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
                val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

                val preview: Preview = createPreview(screenSize)
                cameraSelector = when (cameraselectorfront) {
                    1 -> {
                        binding?.annotationLayout?.visibility = View.VISIBLE
                        createCameraSelectorfront()
                    }
                    2 -> {
                        binding?.annotationLayout?.visibility = View.VISIBLE
                        createCameraSelectorfront()
                    }
                    3 -> {
                        binding?.annotationLayout?.visibility = View.GONE
                        createCameraSelectorfront()
                    }
                    4 -> {
                        binding?.annotationLayout?.visibility = View.GONE
                        createCameraSelectorback()
                    }
                    else -> {
                        binding?.annotationLayout?.visibility = View.VISIBLE
                        createCameraSelectorback()
                    }
                }

                imageCapture = createCameraCapture(screenAspectRatio)
                camera =
                    cameraProvider?.bindToLifecycle(this, cameraSelector!!, preview, imageCapture)
                camera?.let { camera ->
                    preview.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                    setupCameraSetting(camera)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindCamera: Error : ", e)
        }catch (e: NullPointerException) {
            e.printStackTrace()
            Log.e(TAG, "bindCamera: Error : ", e)
        }
    }

    private fun setupCameraSetting(camera: Camera) {
        updateFlashAvailable(camera.cameraInfo.hasFlashUnit())
    }

    private fun updateFlashAvailable(isEnabled: Boolean) {
        binding?.fabFlash?.isEnabled = isEnabled
        updateFlashModeButton()
    }

    private fun updateFlashModeButton() {
        val drawable = ContextCompat.getDrawable(
            requireContext(),
            when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_baseline_flash_on_24
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_baseline_flash_auto_24
                else -> R.drawable.ic_baseline_flash_off_24
            }
        )
        binding?.fabFlash?.setImageDrawable(drawable)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        if (cameraExecutor != null) {
            cameraExecutor?.shutdown()
        }

        dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
//                startCamera()
                setupCameraProvider()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                Navigation.findNavController(requireView()).navigateUp();
            }
        }
    }

    private fun capturePhoto() {
//        val imageCapture = imageCapture ?: return
//       imageCapture?.targetRotation = Surface.ROTATION_180

        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)

        imageCapture?.takePicture(cameraExecutor!!, object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                //get bitmap from image
                try {
                    Log.d("LogTag", image.imageInfo.rotationDegrees.toString())
                    val matrix = Matrix()
                    if (image.imageInfo.rotationDegrees == 90) {
                        matrix.postRotate(90F)
                    }else{
                        if(cameraselectorfront == 1){
                            matrix.postRotate(-90f)
                            matrix.preScale(1.0f, -1.0f)
                        }
                        else if(cameraselectorfront == 2){
                            matrix.postRotate(-90f)
                            matrix.preScale(1.0f, -1.0f)
                        }
                    }

                    bitmap = imageProxyToBitmap(image)
                    image.close()

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, bitmap!!.width, bitmap!!.height, true)
                    val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                    Log.d("LogBitmap", image.toString())
                    val bundle = Bundle()

                    when (cameraselectorfront) {
                        1 -> {
                            bundle.putParcelable("bitmap", rotatedBitmap.drawText(address!!, formattedDate, 60F, Color.parseColor("#39FF14")))
                        }
                        2 -> {
                            bundle.putParcelable("bitmap", rotatedBitmap.drawText(address!!, formattedDate, 60F, Color.parseColor("#39FF14")))
                        }
                        3 -> {
                            bundle.putParcelable("bitmap", rotatedBitmap)
                        }
                        4 -> {
                            bundle.putParcelable("bitmap", rotatedBitmap)
                        }
                        else -> {
                            bundle.putParcelable("bitmap", rotatedBitmap.drawText(address!!, formattedDate, 70F, Color.parseColor("#39FF14")))
                        }
                    }
                    bundle.putInt("IMAGE_REQ_CODE", IMAGE_REQ_CODE)

                    activity?.runOnUiThread {
                        binding?.cameraCaptureButton?.isEnabled = true
                        cameraProvider?.unbindAll()
                        Navigation.findNavController(requireParentFragment().requireView())
                            .navigate(R.id.action_cameraXFragmnets_to_imagePreviewFragment2, bundle)
//                        dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(TAG, "onCaptureSuccess: Error : " + e.message)
                }

                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        })
    }

    fun Bitmap.drawText(text: String, text2: String, textSize: Float, color: Int): Bitmap? {
        val bitmap = copy(config, true)
        val canvas = Canvas(bitmap)

        Paint().apply {
//            val half1 = address.substring(0, address.length / 2)
//            val half2 = address.substring(address.length / 2)
            flags = Paint.ANTI_ALIAS_FLAG
            this.color = color
            this.textSize = textSize
            setShadowLayer(20f, 0f, 2f, Color.parseColor("#673695"))
            if(cameraselectorfront == 1){
                typeface = Typeface.DEFAULT
                canvas.drawText(text2, 60f, height - 180f, this)
                canvas.drawText(text, 60f, height - 80f, this)
            }else{
                typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(text2, 60f, height - 240f, this)
                canvas.drawText(text, 60f, height - 80f, this)
            }
        }
        return bitmap
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location?> { task ->
                val location = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    latitude = location.latitude.toDouble()
                    longitude = location.longitude.toDouble()

                    if (latitude != null) {
                        //                    binding?.location.text = "$latitude , $longitude"
                        val addresses: List<Address>
                        val geocoder: Geocoder = Geocoder(requireActivity(), Locale.getDefault())

                        try {
                            addresses = geocoder.getFromLocation(
                                latitude!!,
                                longitude!!,
                                1
                            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                    Log.d(TAG, "Address: $addresses[0] , sublocality : ${addresses[0].subLocality}, locality : ${addresses[0].locality}, adminArea : ${addresses[0].adminArea}" +
//                            ", subAdminArea : ${addresses[0].subAdminArea}, pincode : ${addresses[0].postalCode}")
                            address =
                                addresses[0].subLocality + " , " + addresses[0].locality + " , " + addresses[0].postalCode // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            binding?.location?.text = address
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(),"please set location setting to high accuracy!",Toast.LENGTH_SHORT).show()
                        }catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(),"please set location setting to high accuracy!",Toast.LENGTH_SHORT).show()
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(),"please set location setting to high accuracy!",Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            })
        } else {
            Toast.makeText(context, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                .show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        }
    }

    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().application)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        binding = null
        camera = null
        imageCapture = null
        cameraExecutor = null
        bitmap = null
        address = null
        latitude = null
        longitude = null
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}