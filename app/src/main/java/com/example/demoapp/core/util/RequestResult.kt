package com.example.demoapp.core.util

sealed class RequestResult {
    data object Loading : RequestResult()

    data class Success(val message: String) : RequestResult()

    data class Failure(val errorMessage: String) : RequestResult()
}
