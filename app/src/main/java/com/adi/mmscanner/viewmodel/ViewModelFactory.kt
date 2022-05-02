package com.adi.mmscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adi.mmscanner.repository.BarcodeRepository

class ViewModelFactory(private val repository: BarcodeRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass){
            MainActivityVM::class.java -> MainActivityVM(repository) as T

            CaptureActivityVM::class.java -> CaptureActivityVM(repository) as T

            else -> throw IllegalArgumentException()
        }
    }
}

