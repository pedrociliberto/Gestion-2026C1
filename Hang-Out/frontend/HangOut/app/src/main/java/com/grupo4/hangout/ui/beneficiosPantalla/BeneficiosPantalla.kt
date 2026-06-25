package com.grupo4.hangout.ui.beneficiosPantalla

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Beneficio
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
fun BeneficiosPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit,
    onNavigateToSuscribir: () -> Unit
) {
    var beneficiosActivos by remember { mutableStateOf<List<Beneficio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isDesactivating by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var beneficioAEliminar by remember { mutableStateOf<Beneficio?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val resultado = fetchBeneficiosActivos(userId, token)
            beneficiosActivos = resultado
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis Beneficios",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading || isDesactivating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (beneficiosActivos.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No tenés ningún beneficio activo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Suscribite a un beneficio para mejorar la visibilidad de tu negocio.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Text(
                        "Beneficios activos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(beneficiosActivos) { beneficio ->
                            BeneficioActivoCard(
                                beneficio = beneficio,
                                onDesactivarClick = {
                                    beneficioAEliminar = beneficio
                                    mostrarDialogoConfirmacion = true
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToSuscribir,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Ver beneficios disponibles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Volver", color = Color.Gray)
                }
            }
        }
    }

    if (mostrarDialogoConfirmacion && beneficioAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoConfirmacion = false
                beneficioAEliminar = null
            },
            title = {
                Text(text = "Desactivar beneficio", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "¿Estás seguro de que querés dar de baja el beneficio \"${beneficioAEliminar?.nombre}\"? Vas a perder sus ventajas inmediatamente.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)), // Botón rojo de acción destructiva
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        val beneficioId = beneficioAEliminar?.id ?: return@Button
                        beneficioAEliminar = null

                        isDesactivating = true
                        coroutineScope.launch {
                            val exito = desactivarBeneficio(userId, token, beneficioId)
                            if (exito) {
                                Toast.makeText(context, "Beneficio dado de baja", Toast.LENGTH_SHORT).show()
                                beneficiosActivos = fetchBeneficiosActivos(userId, token)
                            } else {
                                Toast.makeText(context, "Error al desactivar", Toast.LENGTH_SHORT).show()
                            }
                            isDesactivating = false
                        }
                    }
                ) {
                    Text("Sí, desactivar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        beneficioAEliminar = null
                    }
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun BeneficioActivoCard(beneficio: Beneficio, onDesactivarClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(beneficio.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B5E20))
                Text(beneficio.descripcion, fontSize = 13.sp, color = Color(0xFF388E3C))
            }
            IconButton(onClick = onDesactivarClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Desactivar beneficio",
                    tint = Color(0xFFC62828)
                )
            }
        }
    }
}

suspend fun fetchBeneficiosActivos(userId: Int, token: String): List<Beneficio> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/beneficios/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.optJSONArray("activos") ?: return@withContext emptyList()
                val lista = mutableListOf<Beneficio>()
                for (i in 0 until arr.length()) {
                    val b = arr.getJSONObject(i)
                    lista.add(Beneficio(b.getInt("id"), b.getString("nombre"), b.getString("descripcion")))
                }
                lista
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

suspend fun desactivarBeneficio(userId: Int, token: String, idBeneficio: Int): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val jsonBody = JSONObject().apply {
            put("id_beneficio", idBeneficio)
        }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("${Config.BASE_URL}/beneficios/$userId/desactivar")
            .addHeader("Authorization", "Bearer $token")
            .post(jsonBody)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}