package com.adi.mmscanner

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun Context.showToast(message:String, isShort:Boolean=true){
    Toast.makeText(this, message, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}
//
////val Builder = AlertDialog.Builder(this)
//    .setTitle("Barcode scanned is")
////    .setMessage(data.main)
//    .setPositiveButton("Confirm"){_, _ ->
////        showToast("Confirm clicked")
//    }
//    .setNegativeButton("Cancel"){_, _ ->
////        showToast("Barcode not saved")
//    }
//
//val dialog = Builder.create()
////dialog.show()
