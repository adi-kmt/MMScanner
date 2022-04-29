package com.adi.mmscanner.utils

sealed class StateUtils<T> {

    class Loading<T> : StateUtils<T>()

    data class Success<T>(val data: T): StateUtils<T>()

    data class Failiure<T>(val message:String): StateUtils<T>()


    companion object{
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> failiure(message: String) = Failiure<T>(message)
    }
}