package com.adi.mmscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adi.mmscanner.repository.BarcodeRepository
import kotlinx.coroutines.launch

class MainActivityVM(
    private val repository: BarcodeRepository
)
    :ViewModel() {

    /*
    Should convert to LiveData to be observed by UI?
     */

    fun retieveData() = repository.repogetData()
}