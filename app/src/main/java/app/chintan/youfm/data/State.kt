package app.chintan.youfm.data

import java.lang.Exception

sealed class State<T> {
    class Loading<T> : State<T>()

    data class Success<T>(val data: T) : State<T>()

    data class Failure<T>(val exception: Exception) : State<T>()

    companion object {

        fun <T> loading() = Loading<T>()

        fun <T> success(data: T) = Success(data)

        fun <T> failure(exception: Exception) = Failure<T>(exception)
    }

}
