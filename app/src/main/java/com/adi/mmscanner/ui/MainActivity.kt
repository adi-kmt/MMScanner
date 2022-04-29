package com.adi.mmscanner.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.adi.mmscanner.viewmodel.BarcodeRecieveViewModel
import com.adi.mmscanner.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    lateinit var viewModel: BarcodeRecieveViewModel


    val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permission ->
        var count =0
        permission.forEach { s, b ->
//            Log.e("Permission", s)
            if (!b){
                showToast("Permissions not granted")
            }else{
                count ++
            }
        }
        if (count == 2){
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }
    }

    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MMScanner)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository =BarcodeRepository()


        viewModel= ViewModelProvider(this, ViewModelFactory(repository)).get(BarcodeRecieveViewModel::class.java)

        binding.clickimgBtn.setOnClickListener {
           permissionsLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET))
        }


    binding.Barcoderv.apply {
        layoutManager = LinearLayoutManager(this@MainActivity)
    }
        lifecycleScope.launch {
            viewModel.retieveData().collect { state ->
                when (state) {
                    is StateUtils.Loading -> {
                        binding.apply {
                            Barcoderv.visibility = View.GONE
                            progressbar.visibility = View.VISIBLE
                            EmptyTv.visibility = View.GONE
                        }
                    }
                    is StateUtils.Success -> {
                        binding.apply {
                            progressbar.visibility = View.GONE
                            EmptyTv.visibility = View.GONE

                            state.data?.let {
                                Barcoderv.visibility = View.VISIBLE
                                Barcoderv.adapter = BarcodeAdapter(it)
                            }
                        }
                    }
                    is StateUtils.Failiure -> {
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