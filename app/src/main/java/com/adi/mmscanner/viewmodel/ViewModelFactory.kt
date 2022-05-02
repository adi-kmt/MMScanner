package com.adi.mmscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adi.mmscanner.repository.BarcodeRepository

class ViewModelFactory(private val repository: BarcodeRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when (modelClass){
            BarcodeRecieveViewModel::class.java -> return BarcodeRecieveViewModel(repository) as T

            BarcodeSendViewModel::class.java -> return  BarcodeSendViewModel(repository) as T

            else -> throw IllegalArgumentException()
        }
    }
}

