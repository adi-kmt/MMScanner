package com.adi.mmscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adi.mmscanner.repository.BarcodeRepository
import kotlinx.coroutines.launch

class BarcodeRecieveViewModel(
    private val repository: BarcodeRepository
)
    :ViewModel() {

    fun retieveData() = repository.repogetData()
}