package com.grupo4.hangout.ui.loginPantalla

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPantalla(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (JSONObject) -> Unit, // Pasamos el JSON para guardar datos del usuario
) {
    var identifier by remember { mutableStateOf("") } // Usuario o Email
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState),
    ) {
        // Cabecera idéntica al Registro para consistencia de marca
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Hangout",
                modifier = Modifier.size(140.dp)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Hola de nuevo!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                modifier = Modifier.padding(top = 26.dp, bottom = 10.dp)
            )

            Text(
                text = "Ingresá a tu cuenta para organizar tu próxima juntada",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Campo Usuario o Email
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Usuario o correo electrónico") },
                placeholder = { Text("Ej. max123 o max@mail.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryColor)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            val result = loginUser(identifier, password)
                            isLoading = false
                            when (result) {
                                is ApiResult.Success -> {
                                    Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(result.data)
                                }
                                is ApiResult.Error -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Iniciar sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("¿No tenés una cuenta?", color = Color.Gray)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Registrate", color = PrimaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Realiza login de usuario contra el backend.
 *
 * Envía una petición POST con username/email y password,
 * ejecuta la llamada en IO y devuelve un [ApiResult]
 * con éxito o error.
 *
 * @param identifier username o email del usuario
 * @param pass contraseña del usuario
 * @return [ApiResult.Success] con datos del usuario o
 *         [ApiResult.Error] con mensaje de error
 */
suspend fun loginUser(
    identifier: String,
    pass: String
): ApiResult {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("usuario", identifier) // El backend busca por este campo tanto user como email
            put("password", pass)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/login")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)

                if (response.code == 200) {
                    ApiResult.Success(jsonResponse)
                } else {
                    val errorMessage = jsonResponse.optString("error", "Error desconocido")
                    ApiResult.Error(errorMessage)
                }
            }
        } catch (_: Exception) {
            ApiResult.Error("Falló la conexión con el servidor")
        }
    }
}
