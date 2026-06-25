package com.grupo4.hangout.ui.notificacionesPantalla

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
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
import com.grupo4.hangout.model.Notificacion
import com.grupo4.hangout.ui.detalleJuntadaPantalla.formatearFechaHoraEspaniol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisNotificacionesNegocioPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit
) {
    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarNotificaciones() {
        isLoading = true
        coroutineScope.launch {
            notificaciones = fetchNotificacionesEnviadas(userId, token)
            isLoading = false
        }
    }

    LaunchedEffect(userId) {
        cargarNotificaciones()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryColor,
                    navigationIconContentColor = PrimaryColor
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (notificaciones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Aún no has enviado notificaciones", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notificaciones) { notificacion ->
                    NotificacionNegocioCard(
                        notificacion = notificacion,
                        onDeleteClick = { showDeleteConfirm = notificacion.id }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("¿Eliminar notificación?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción eliminará la notificación para todos los usuarios. No se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val notiId = showDeleteConfirm!!
                        showDeleteConfirm = null
                        coroutineScope.launch {
                            val result = eliminarNotificacion(notiId, token)
                            Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                            if (result.first) {
                                cargarNotificaciones()
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun NotificacionNegocioCard(
    notificacion: Notificacion,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notificacion.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ENVIADA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }

            }
        }
    }
}

suspend fun fetchNotificacionesEnviadas(userId: Int, token: String): List<Notificacion> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/notificaciones/creadas/$userId")
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

suspend fun eliminarNotificacion(idNotificacion: Int, token: String): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/notificaciones/eliminar/$idNotificacion")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val json = if (body.isNotEmpty()) JSONObject(body) else JSONObject()
                if (response.isSuccessful) {
                    Pair(true, json.optString("mensaje", "Notificación eliminada"))
                } else {
                    Pair(false, json.optString("error", "No se pudo eliminar la notificación"))
                }
            }
        } catch (_: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}
