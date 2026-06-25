package com.grupo4.hangout.ui.crearReseniaPantalla

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.grupo4.hangout.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

suspend fun enviarImagenes(context: android.content.Context, imagenes: List<Uri>, negocioId: Int, juntadaId: Int, userId: Int, token: String): Pair<Boolean, String>{

    return withContext(Dispatchers.IO){
        var seSubieronTodas = true
        var errorImagenes = ""
        imagenes.forEach { uri ->
            val resultado = enviarImagen(context, uri, negocioId, juntadaId, userId, token)
            val seSubioImagen = resultado.first
            errorImagenes = resultado.second
            seSubieronTodas = seSubieronTodas && seSubioImagen
        }
        if(!seSubieronTodas){
            Pair(false, errorImagenes)
        } else{
            Pair(true, "Imagenes subidas con exito")
        }
    }
}

suspend fun enviarImagen(context: android.content.Context, uri: Uri, negocioId: Int, juntadaId: Int, userId: Int, token: String): Pair<Boolean, String>{
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val file = uriToFile(context, uri) ?: return@withContext Pair(false, "No se pudo procesar la imagen")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("imagen", file.name, file.asRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/resenia/$negocioId/$juntadaId/$userId/imagenes")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                if (response.isSuccessful) {
                    Pair(true, json.optString("mensaje", "Imagen subida con éxito"))
                } else {
                    Pair(false, json.optString("error", "Error al subir la imagen"))
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}

suspend fun borrarImagenesJuntada(negocioId: Int, juntadaId: Int, userId: Int, token: String): Unit{
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/resenia/$negocioId/$juntadaId/$userId/imagenes")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()

        client.newCall(request).execute()
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}