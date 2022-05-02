package com.adi.mmscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adi.mmscanner.repository.BarcodeRepository

class ViewModelFactory(private val repository: BarcodeRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass){
            GetDataVM::class.java -> GetDataVM(repository) as T

            SendDataVM::class.java -> SendDataVM(repository) as T

            else -> throw IllegalArgumentException()
        }
    }
}

