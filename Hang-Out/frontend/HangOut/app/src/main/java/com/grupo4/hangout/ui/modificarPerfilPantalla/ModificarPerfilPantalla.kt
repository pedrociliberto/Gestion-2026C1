package com.grupo4.hangout.ui.modificarPerfilPantalla

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarPerfilPantalla(
    userId: Int,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Copias para poder revertir si el usuario cancela y comparar qué cambió exactamente
    var originalNombre by remember { mutableStateOf("") }
    var originalUsuario by remember { mutableStateOf("") }
    var originalEmail by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Recuperamos el token de las SharedPreferences
    val sharedPrefs = remember { context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE) }
    val token = remember { sharedPrefs.getString("token", "") ?: "" }

    // Función para deshacer cambios locales
    fun cancelarEdicion() {
        nombre = originalNombre
        usuario = originalUsuario
        email = originalEmail
        isEditing = false
    }

    // Función para refrescar datos reales del servidor
    suspend fun refrescarDatos() {
        val userData = fetchUserData(userId, token)
        if (userData != null) {
            val data = userData.optJSONObject("data") ?: userData
            nombre = data.optString("nombre_completo", "")
            usuario = data.optString("usuario", "")
            email = data.optString("email", "")

            // Actualizamos la copia original para que coincida con lo nuevo del servidor
            originalNombre = nombre
            originalUsuario = usuario
            originalEmail = email
        }
    }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            refrescarDatos()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditing) "Modificar Mis Datos" else "Mis Datos Personales", 
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = if (isEditing) { { cancelarEdicion() } } else onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                if (!isEditing) {
                    // --- MODO VISTA ---
                    InfoItem(label = "Nombre completo", value = nombre, icon = Icons.Default.Person)
                    InfoItem(label = "Usuario", value = usuario, icon = Icons.Default.AccountCircle)
                    InfoItem(label = "Correo electrónico", value = email, icon = Icons.Default.Email)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Modificar Datos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // --- MODO EDICIÓN (FORMULARIO) ---
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text("Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = PrimaryColor) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryColor) }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (isSaving) {
                        CircularProgressIndicator(color = PrimaryColor)
                    } else {
                        Button(
                            onClick = {
                                val sinCambios = nombre == originalNombre && usuario == originalUsuario && email == originalEmail
                                if (sinCambios) {
                                    Toast.makeText(context, "No se detectaron cambios para guardar", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSaving = true
                                coroutineScope.launch {
                                    val result = updateUserData(
                                        userId = userId,
                                        token = token,
                                        nombre = nombre, originalNombre = originalNombre,
                                        usuario = usuario, originalUsuario = originalUsuario,
                                        email = email, originalEmail = originalEmail
                                    )
                                    if (result.first) {
                                        // Refrescamos los datos reales del servidor antes de volver a modo vista
                                        refrescarDatos()
                                        isEditing = false
                                    }
                                    isSaving = false
                                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Guardar Cambios", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(onClick = { cancelarEdicion() }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
    }
}

suspend fun fetchUserData(userId: Int, token: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/perfil/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext null
                }
                JSONObject(body)
            }
        } catch (_: Exception) {
            null
        }
    }
}

suspend fun updateUserData(
    userId: Int,
    token: String,
    nombre: String,
    originalNombre: String,
    usuario: String,
    originalUsuario: String,
    email: String,
    originalEmail: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            if (nombre != originalNombre) {
                put("nombre_completo", nombre)
            }
            if (usuario != originalUsuario) {
                put("usuario", usuario)
            }
            if (email != originalEmail) {
                put("email", email)
            }
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/perfil/modificar/$userId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""

                val jsonResp = if (respBody.isNotBlank()) JSONObject(respBody) else JSONObject()
                if (response.isSuccessful) Pair(true, jsonResp.optString("mensaje", "Datos actualizados"))
                else Pair(false, jsonResp.optString("error", "Error al actualizar"))
            }
        } catch (_: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}
