package com.adi.mmscanner.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.adi.mmscanner.R
import com.adi.mmscanner.databinding.ActivityMainBinding
import com.adi.mmscanner.repository.BarcodeRepository
import com.adi.mmscanner.showToast
import com.adi.mmscanner.utils.StateUtils
import com.adi.mmscanner.viewmodel.MainActivityVM
import com.adi.mmscanner.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    lateinit var viewModel: MainActivityVM


    val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {permission ->
        if (permission){
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }else{
            showToast("Permissions not granted")
        }
    }


    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MMScanner)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository =BarcodeRepository()


        viewModel= ViewModelProvider(this, ViewModelFactory(repository)).get(MainActivityVM::class.java)

        binding.clickimgBtn.setOnClickListener {
           permissionsLauncher.launch(android.Manifest.permission.CAMERA)
        }


    binding.Barcoderv.apply {
        layoutManager = LinearLayoutManager(this@MainActivity)
    }
        lifecycleScope.launch {
            viewModel.retieveData().collect { state ->
                when (state) {
                    is StateUtils.Loading -> {
                        Log.e("RV", "Loading")
                        binding.apply {
                            Barcoderv.visibility = View.GONE
                            progressbar.visibility = View.VISIBLE
                            EmptyTv.visibility = View.GONE
                        }
                    }
                    is StateUtils.Success -> {
                        Log.e("RV", "Success")
                        binding.apply {
                            progressbar.visibility = View.GONE
                            EmptyTv.visibility = View.GONE

                            state.data.let {
                                Barcoderv.visibility = View.VISIBLE
                                Barcoderv.adapter = BarcodeAdapter(it.toTypedArray())
                            }
                        }
                    }
                    is StateUtils.Failiure -> {
                        Log.e("RV", state.message)
                        binding.apply {
                            Barcoderv.visibility = View.GONE
                            progressbar.visibility = View.GONE
                            EmptyTv.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

    }
}