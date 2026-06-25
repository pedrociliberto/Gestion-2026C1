package com.grupo4.hangout.ui.loginPantalla
import org.json.JSONObject

sealed class ApiResult {
    data class Success(val data: JSONObject) : ApiResult()
    data class Error(val message: String) : ApiResult()
}