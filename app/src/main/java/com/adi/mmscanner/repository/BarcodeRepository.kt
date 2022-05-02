package com.adi.mmscanner.repository

import androidx.lifecycle.Lifecycle
import com.adi.mmscanner.Barcodedata
import com.adi.mmscanner.utils.StateUtils
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class BarcodeRepository {

    private val collection = FirebaseFirestore.getInstance().collection("Barcodes")

    fun repoSaveData(data: Barcodedata) = flow<StateUtils<DocumentReference>> {
        emit(StateUtils.loading())

        val dataref = collection.add(data).await()

        emit(StateUtils.success(dataref))
    }.catch {
        emit(StateUtils.failiure(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    fun repogetData() = flow<StateUtils<List<Barcodedata>>> {
        emit(StateUtils.loading())

        val data = collection.get().await()
        val barcodedata = data.toObjects(Barcodedata::class.java)

        emit(StateUtils.success(data = barcodedata))
    }.catch {
        emit(StateUtils.failiure(it.message.toString()))
    }.flowOn(Dispatchers.IO)
}