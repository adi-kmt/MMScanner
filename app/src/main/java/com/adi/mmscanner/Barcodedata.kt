package com.adi.mmscanner

import android.graphics.Rect
import java.util.*

data class Barcodedata(
    val main:String?="",
    val rect: Rect?=Rect(0, 0, 0, 0),
    val date: Long?=0
)