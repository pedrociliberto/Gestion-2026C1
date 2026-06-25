package com.grupo4.hangout.ui.crearReseniaPantalla

import android.net.Uri
import com.grupo4.hangout.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun enviarResenia(
    valoracion: Int,
    textoResenia: String,
    juntadaId: Int,
    negocioId: Int,
    userId: Int,
    token: String,
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO){
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("valoracion", valoracion)
            put("texto_resenia", textoResenia)
            put("juntadaId", juntadaId)
            put("negocioId", negocioId)
            put("userId", userId)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/resenia")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respJson = JSONObject(responseBody)
                    val mensaje = respJson.optString("mensaje", "Reseña enviada.")
                    Pair(true, mensaje)
                } else {
                    val error = JSONObject(responseBody).optString("error", "Error al crear la reseña")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}

suspend fun borrarResenia(negocioId: Int, juntadaId: Int, userId: Int, token: String): Unit{
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/resenia/$negocioId/$juntadaId/$userId")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()

        client.newCall(request).execute()
    }
}
