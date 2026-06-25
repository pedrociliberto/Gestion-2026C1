package com.grupo4.hangout.ui.unirseJuntadaPantalla

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun UnirseJuntadaPantalla(
    userId: Int,
    onBack: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // espacio automático entre elementos
    ) {
        Text(
            text = "Unirse a una Juntada",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código de la juntada") },
            placeholder = { Text("Ej: ABCD") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(color = PrimaryColor)
        } else {
            Button(
                onClick = {
                    if (codigo.length != 4) {
                        Toast.makeText(context, "El código debe tener 4 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button // sale del onClick sin hacer nada más
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val respuesta = unirseAJuntada(codigo, userId)
                        isLoading = false

                        Toast.makeText(context, respuesta.second, Toast.LENGTH_LONG).show()

                        if (respuesta.first) {
                            onBack()
                        }
                    }
                },
                enabled = codigo.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Unirse", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

suspend fun unirseAJuntada(codigo: String, userId: Int): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("codigo", codigo)
            put("id_usuario", userId)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/juntada/unirse") // el endpoint que hiciste en el backend
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val mensaje = JSONObject(responseBody).optString("mensaje", "Te uniste a la juntada")
                    Pair(true, mensaje)
                } else {
                    val error = JSONObject(responseBody).optString("error", "Error al unirse")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión con el servidor")
        }
    }
}