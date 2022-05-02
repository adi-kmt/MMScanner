package com.adi.mmscanner.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.Surface.ROTATION_0
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
import com.adi.mmscanner.viewmodel.CaptureActivityVM
import com.adi.mmscanner.viewmodel.ViewModelFactory
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

    private lateinit var binding:ActivityCaptureBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var dataVM: CaptureActivityVM




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

        dataVM= ViewModelProvider(this, ViewModelFactory(repository)).get(CaptureActivityVM::class.java)

        dataVM.BarcodeLiveData.observe(this, Observer { data ->
            data.main?.let { showToast(it) }

    val Builder = AlertDialog.Builder(this)
        .setTitle("Barcode scanned is")
        .setMessage(data.main)
        .setPositiveButton("Confirm"){_, _ ->
            showToast("Confirm clicked")

            startActivity(Intent(this, MainActivity::class.java))

            //Call coroutine using lifecycle scope
            lifecycleScope.launch {
                dataVM.sendData(data).collect { state ->
                    showToast(
                        when(state){
                            is StateUtils.Loading -> {
                                "Loading"
                            }
                            is StateUtils.Success -> {
                                "Sent"
                            }
                            is StateUtils.Failiure -> {
                                "Try again"
                            }
                        }
                    )
                    if (state is StateUtils.Success){
                        startActivity(Intent(this@CaptureActivity, MainActivity::class.java))
                    }
                }
            }
        }
        .setNegativeButton("Cancel"){_, _ ->
            showToast("Barcode not saved")
        }

    val dialog = Builder.create()
    dialog.show()
        })
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
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            imageCapture= ImageCapture.Builder().build()

            val viewPort = ViewPort.Builder(Rational(350, 350), ROTATION_0).build()

            val useCase = imageCapture?.let {capture ->
                UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(capture)
                .setViewPort(viewPort)
                .build() }


            try {
                cameraProvider.unbindAll()
                useCase?.let {usecase ->
                    cameraProvider.bindToLifecycle(this, cameraSelector,
                        usecase
                    )
                }
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


            Image.image?.let {
                val inputImage = InputImage.fromMediaImage(
                    it,
                    Image.imageInfo.rotationDegrees
                )

                showToast("Bitmap not null")

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        readBarcodes(barcodes) //as List<Barcode>
                    }
                    .addOnFailureListener {exception ->
                        exception.message?.let { exmessage -> showToast(exmessage) }
                    }.addOnCompleteListener {
                            Image.close()
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
                    dataVM.validateData(data)
                }
                Barcode.TYPE_PHONE -> {
                    val num = barcode.phone?.number
                    val data = Barcodedata(num, rect, System.currentTimeMillis())
//                    data.main?.let { Log.e("Barcode data", it) }
                    showToast("Barcode values are " + data.main + data.date + data.rect, false)
                    dataVM.validateData(data)
                }
            }
        }
    }
}
