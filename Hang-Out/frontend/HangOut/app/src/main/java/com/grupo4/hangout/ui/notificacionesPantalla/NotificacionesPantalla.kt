package com.grupo4.hangout.ui.notificacionesPantalla

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesPantalla(
    userId: Int,
    onBack: () -> Unit
) {
    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("token", "") ?: ""
            notificaciones = fetchNotificacionesRecibidas(userId, token)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryColor,
                    navigationIconContentColor = PrimaryColor
                )
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (notificaciones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFBBBBBB)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No tenés notificaciones", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    Text("Cuando lleguen, aparecerán acá", fontSize = 13.sp, color = Color(0xFFAAAAAA))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "${notificaciones.size} notificación${if (notificaciones.size != 1) "es" else ""}",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(notificaciones, key = { it.id }) { notificacion ->
                    NotificacionRecibidaItem(
                        notificacion = notificacion,
                        onTap = {
                            coroutineScope.launch {
                                val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
                                val token = sharedPrefs.getString("token", "") ?: ""
                                borrarNotificacion(notificacion.id, token)
                                notificaciones = notificaciones.filter { it.id != notificacion.id }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificacionRecibidaItem(
    notificacion: Notificacion,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryColor.copy(alpha = 0.15f), PrimaryColor.copy(alpha = 0.30f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notificacion.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    notificacion.descripcion,
                    fontSize = 13.sp,
                    color = Color(0xFF777777),
                    lineHeight = 18.sp
                )
            }

            // Cruz para borrar
            IconButton(onClick = onTap) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Borrar notificación",
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- HTTP helpers ---

suspend fun fetchNotificacionesRecibidas(userId: Int, token: String): List<Notificacion> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/perfil/notificaciones/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val arr = json.getJSONArray("notificaciones")
                val lista = mutableListOf<Notificacion>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    lista.add(Notificacion(
                        id = obj.getInt("id"),
                        titulo = obj.getString("titulo"),
                        descripcion = obj.getString("descripcion"),
                        leida = obj.optBoolean("leida", false)
                    ))
                }
                lista
            }
        } catch (_: Exception) { emptyList() }
    }
}

suspend fun borrarNotificacion(notificacionId: Int, token: String) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/perfil/notificaciones/$notificacionId")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()
        try {
            client.newCall(request).execute().use { }
        } catch (_: Exception) { }
    }
}