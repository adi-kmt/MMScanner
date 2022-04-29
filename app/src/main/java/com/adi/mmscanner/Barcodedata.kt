package com.adi.mmscanner

import android.graphics.Rect
import java.util.*

data class Barcodedata(
    val main:String?,
    val rect: Rect?,
    val date: Long
    )