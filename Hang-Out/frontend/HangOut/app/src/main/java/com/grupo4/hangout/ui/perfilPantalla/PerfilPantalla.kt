package com.grupo4.hangout.ui.perfilPantalla

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutBottomNavigation
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class Filtro(val id: Int, val nombre: String)

@Composable
fun PerfilPantalla(
    userId: Int,
    esPersonal: Boolean,
    token: String = "",
    onNavigateToVerNegocio: () -> Unit,
    onNavigateToModificarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNavigateToJuntadas: () -> Unit,
    onNavigateToPlanificador: () -> Unit,
    onNavigateToDescuentos: () -> Unit,
    onNavigateToCrearNotificacion: () -> Unit = {},
    onNavigateToMisNotificacionesEnviadas: () -> Unit = {},
    onBack: () -> Unit
) {
    var allFiltros by remember { mutableStateOf<List<Filtro>>(emptyList()) }
    var selectedFiltrosIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var notificacionesRestantes by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        val filtrosList = fetchAllFiltros() ?: emptyList()
        val userFiltros = if (esPersonal) fetchUserFiltros(userId) ?: emptyList() else emptyList()

        if (!esPersonal && token.isNotEmpty()) {
            notificacionesRestantes = fetchNotificacionesRestantes(userId, token)
        }

        allFiltros = filtrosList
        selectedFiltrosIds = userFiltros.map { it.id }.toSet()
        isLoading = false
    }

    Scaffold(
        topBar = {
            HangoutTopBar(
                onProfileClick = { /* Ya estamos en perfil */ },
                onCerrarSesionClick = onCerrarSesion,
                esPersonal = esPersonal
            )
        },
        bottomBar = {
            HangoutBottomNavigation(
                onProfileClick = { },
                onNavigateToPlanificador = onNavigateToPlanificador,
                onNavigateToDescuentos = onNavigateToDescuentos,
                onNavigateToJuntadas = onNavigateToJuntadas,
                pantallaActual = "Perfil",
                esPersonal = esPersonal
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mi Perfil",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                // Botón común para todos: Ver datos personales
                OutlinedButton(
                    onClick = onNavigateToModificarPerfil,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver mis datos personales", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!esPersonal) {
                    // Diseño para cuenta de EMPRESA
                    Button(
                        onClick = onNavigateToVerNegocio,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Ver información de mi negocio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (notificacionesRestantes > 0) {
                        Button(
                            onClick = onNavigateToCrearNotificacion,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear notificación Pop-Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Tenés $notificacionesRestantes notificaciones disponibles!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (notificacionesRestantes > 0) {
                        OutlinedButton(
                            onClick = onNavigateToMisNotificacionesEnviadas,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ver notificaciones enviadas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    // Diseño para cuenta PERSONAL (Preferencias)
                    Text(
                        text = "Mis Preferencias",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Seleccioná las categorías que más te interesan para tus juntadas.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allFiltros) { filtro ->
                            val isChecked = filtro.id in selectedFiltrosIds
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFiltrosIds = if (isChecked) selectedFiltrosIds - filtro.id else selectedFiltrosIds + filtro.id
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChecked) PrimaryColor.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
                                border = if (isChecked) BorderStroke(1.dp, PrimaryColor) else null
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                                    )
                                    Text(
                                        text = filtro.nombre,
                                        fontSize = 16.sp,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSaving) {
                        CircularProgressIndicator(color = PrimaryColor)
                    } else {
                        Button(
                            onClick = {
                                isSaving = true
                                coroutineScope.launch {
                                    val result = saveUserFiltros(userId, selectedFiltrosIds.toList())
                                    isSaving = false
                                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)) // Verde para guardar
                        ) {
                            Text("Guardar Preferencias", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

suspend fun fetchAllFiltros(): List<Filtro>? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url("${Config.BASE_URL}/filtros/listar").build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: ""
                val obj = JSONObject(bodyString)
                val jsonArray = obj.optJSONArray("filtros") ?: return@withContext emptyList()
                val listaFiltros = mutableListOf<Filtro>()
                for (i in 0 until jsonArray.length()) {
                    val filtroJson = jsonArray.getJSONObject(i)
                    listaFiltros.add(Filtro(filtroJson.getInt("id"), filtroJson.getString("nombre")))
                }
                return@withContext listaFiltros
            }
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}

suspend fun fetchUserFiltros(usuarioId: Int): List<Filtro>? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url("${Config.BASE_URL}/filtros/listar/$usuarioId").build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: ""
                val obj = JSONObject(bodyString)
                val jsonArray = obj.optJSONArray("filtros") ?: return@withContext emptyList()
                val listaFiltros = mutableListOf<Filtro>()
                for (i in 0 until jsonArray.length()) {
                    val filtroJson = jsonArray.getJSONObject(i)
                    listaFiltros.add(Filtro(filtroJson.getInt("id"), filtroJson.getString("nombre")))
                }
                return@withContext listaFiltros
            }
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}

suspend fun saveUserFiltros(usuarioId: Int, filtrosSeleccionadosIds: List<Int>): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply { put("filtros", JSONArray(filtrosSeleccionadosIds)) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url("${Config.BASE_URL}/filtros/agregar/$usuarioId").put(body).build()
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Pair(true, JSONObject(responseBody).optString("mensaje", "Filtros actualizados"))
                } else {
                    Pair(false, JSONObject(responseBody).optString("mensaje", "Error al guardar"))
                }
            }
        } catch (e: Exception) { e.printStackTrace(); Pair(false, "Error de conexión") }
    }
}

suspend fun fetchNotificacionesRestantes(userId: Int, token: String): Int {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/notificaciones")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext 0
                val body = response.body?.string() ?: return@withContext 0
                val json = JSONObject(body)
                return@withContext json.optInt("notificaciones_restantes", 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
