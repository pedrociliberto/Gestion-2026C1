package com.grupo4.hangout.ui.crearJuntadaPantalla

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.withContext

@Composable
fun CrearJuntadaPantalla(
    userId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tituloJuntada by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Crear Juntada",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Input: Título
        OutlinedTextField(
            value = tituloJuntada,
            onValueChange = { tituloJuntada = it },
            label = { Text("Título de la Juntada") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            )
        )

        // Input: Código
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código (Opcional)") },
            placeholder = { Text("Ej: A123") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = PrimaryColor)
        } else {
            Button(
                onClick = {
                    if (tituloJuntada.isBlank()) {
                        Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val respuesta = escucharClickEnCrear(tituloJuntada, codigo, userId)
                        isLoading = false

                        Toast.makeText(context, respuesta.second, Toast.LENGTH_LONG).show()

                        if (respuesta.first) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(text = "Confirmar y Crear", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

suspend fun escucharClickEnCrear(titulo: String, codigo: String, userId: Int): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("titulo", titulo)
            put("codigo", codigo)
            put("id_organizador", userId)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/juntada")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respJson = JSONObject(responseBody)
                    val mensaje = respJson.optString("mensaje", "Juntada creada")
                    Pair(true, mensaje)
                } else {
                    val error = JSONObject(responseBody).optString("error", "Error al crear")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión:")
        }
    }
}
