package com.adi.mmscanner.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.adi.mmscanner.Barcodedata
import com.adi.mmscanner.databinding.ActivityCaptureBinding
import com.adi.mmscanner.repository.BarcodeRepository
import com.adi.mmscanner.showToast
import com.adi.mmscanner.utils.StateUtils
import com.adi.mmscanner.viewmodel.BarcodeSendViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureActivity : AppCompatActivity() {

    /*
    -Maybe use image analyzer + dialog box on confirm, pass intent to previous activity
    -Find a way to pass directly the data
     */


    private lateinit var binding:ActivityCaptureBinding

    val REQUEST_CODE = 10

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var viewModel: BarcodeSendViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCamera()
        binding.captureBtn.setOnClickListener {
            clickImage()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        val repository = BarcodeRepository()

        viewModel= ViewModelProvider(this, ViewModelFactoryS(repository)).get(BarcodeSendViewModel::class.java)

        viewModel.BarcodeLiveData.observe(this, Observer {data ->

    val Builder = AlertDialog.Builder(this)
        .setTitle("Barcode scanned is")
        .setMessage(data.main)
        .setPositiveButton("Confirm"){_, _ ->
            showToast("Confirm clicked")

            //Call corutine using lfcycle scope
            lifecycleScope.launch {
                viewModel.sendData(data).collect { state ->
                    when (state) {
                        is StateUtils.Loading -> {
                            showToast("Losding")
                        }
                        is StateUtils.Success -> {
                            showToast("Sent")
                        }
                        is StateUtils.Failiure -> {
                            showToast("Try Again")
                        }
                    }
                }
            }
        }
        .setNegativeButton("Cancel"){_, _ ->
            showToast("Barcode not saved")
        }

    val dialog = Builder.create()
    //dialog.show()


        })

    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    private fun clickImage(){
       val imageCapture = imageCapture?:return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    showToast("Image captured")
                    BarcodedataScanner(image,image.imageInfo.rotationDegrees)
//                    showToast("Back here")
//                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    showToast("Image Error")
                    exception.message?.let { showToast(it) }
                }
                }
        )

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider:ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider) } //why also is used

            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            imageCapture= ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector,
                    preview,
                    imageCapture
                )
            }catch (e:Exception){
                Log.e("Camera error", "${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun BarcodedataScanner(Image: ImageProxy, rotationDegrees:Int?) {
        showToast("Entered barcode scanner")

        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()

        val scanner = BarcodeScanning.getClient(options)

        val bitmap = imageProxyToBitmap(Image)

        bitmap?.let { abitmap ->
//            val image = InputImage.fromBitmap(abitmap, rotationDegrees?:0)

            Image.image?.let {
                val inputImage = InputImage.fromMediaImage(
                    it,
                    Image.imageInfo.rotationDegrees
                )

                showToast("Bitmap not null")

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        readBarcodes(barcodes) as List<Barcode>
                    }
                    .addOnFailureListener {
                        it.message?.let { it1 -> showToast(it1) }
                    }
//                    .addOnCompleteListener {
//                        Image.close()
//                    }
            }
        }
    }

    private fun readBarcodes(barcodes: List<Barcode>): Unit {
        for (barcode in barcodes){
            val rect = barcode.boundingBox
            when(barcode.valueType){
                Barcode.TYPE_URL ->{
                    val title = barcode.url?.title
                    val url = barcode.url?.url
                    val data = Barcodedata(url, rect, System.currentTimeMillis())
                    showToast("Barcode values are " + data.main + data.date + data.rect, false)
//                    createDialog(data)
                }
                Barcode.TYPE_PHONE -> {
                    val num = barcode.phone?.number
                    val data = Barcodedata(num, rect, System.currentTimeMillis())
//                    data.main?.let { Log.e("Barcode data", it) }
                    showToast("Barcode values are " + data.main + data.date + data.rect, false)
                    viewModel.validateData(data)
//                    createDialog(data)
//                    dialog.show()
//                    setResult(10, Intent().putExtra("main",data.main).putExtra("Date", data.date))
                }
            }
        }
    }
}
