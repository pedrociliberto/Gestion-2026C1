package com.grupo4.hangout.ui.panelJuntadasPantalla

import android.content.Context
import com.grupo4.hangout.model.Juntada

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.R
import com.grupo4.hangout.ui.detalleJuntadaPantalla.formatearFechaHoraEspaniol
import com.grupo4.hangout.ui.notificacionesPantalla.fetchNotificacionesRecibidas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

@Composable
fun PanelJuntadasPantalla(
    userId: Int,
    esPersonal: Boolean,
    onNavigateToCrearJuntada: () -> Unit,
    onNavigateToDetalleJuntada: (Int) -> Unit,
    onNavigateToUnirseJuntada: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToPlanificador: () -> Unit,
    onNavigateToDescuentos: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var juntadas by remember { mutableStateOf<List<Juntada>>(emptyList()) }
    var unreadCount by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(1) } // Empezamos en "Pendientes" por defecto
    val tabs = listOf("Pasadas", "Pendientes", "Próximas" )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("token", "") ?: ""
            juntadas = fetchJuntadas(userId, token)
            
            // Fetch notifications to get unread count
            val notis = fetchNotificacionesRecibidas(userId, token)
            unreadCount = notis.count { !it.leida }
        }
    }

    val juntadasFiltradas = remember(juntadas, selectedTab) {
        when (selectedTab) {
            0 -> juntadas.filter { it.estado == "PASADA" }
            1 -> juntadas.filter { it.estado == "PENDIENTE" }
            2 -> juntadas.filter { it.estado == "CONFIRMADA" }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            HangoutTopBar(
                onProfileClick = onNavigateToPerfil,
                onNotificationsClick = onNavigateToNotificaciones,
                onCerrarSesionClick = onCerrarSesion,
                unreadCount = unreadCount,
                esPersonal = esPersonal
            )
        },
        bottomBar = { HangoutBottomNavigation(onProfileClick = onNavigateToPerfil, onNavigateToPlanificador, onNavigateToDescuentos, esPersonal = esPersonal) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCalendario,
                containerColor = Color(0xFF172554),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendario")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = onNavigateToCrearJuntada,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Crear nueva Juntada", fontSize = 16.sp)
            }

            // Boton para unirse a una juntada
            Button(
                onClick = onNavigateToUnirseJuntada,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)) // verde
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Unirse a una Juntada", fontSize = 16.sp)
            }

            Text(
                text = "Mis Juntadas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = if (isSelected) {
                            Modifier
                                .padding(4.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
                        } else Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (juntadasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val mensajeVacio = when (selectedTab) {
                        0 -> "Aún no tenés un historial de juntadas."
                        1 -> "No tenés juntadas pendientes.\n¡Tocá el botón 'Crear nueva Juntada' para crear una!"
                        else -> "No tenés próximas juntadas ya confirmadas.\n¡Organizá una con tus amigos!"

                    }
                    Text(
                        text = mensajeVacio,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(juntadasFiltradas) { juntada ->
                        JuntadaCard(juntada, onClick = { onNavigateToDetalleJuntada(juntada.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun HangoutTopBar(
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onCerrarSesionClick: () -> Unit,
    unreadCount: Int = 0,
    esPersonal: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_horizontal),
            contentDescription = "Logo de Hangout",
            modifier = Modifier.height(40.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (esPersonal) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                Text(text = if (unreadCount > 9) "9+" else unreadCount.toString())
                            }
                        }
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    IconButton(onClick = onNotificationsClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = PrimaryColor
                        )
                    }
                }
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color.White),
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver mi Perfil") },
                        onClick = {
                            menuExpanded = false
                            onProfileClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(
                            text = "Cerrar Sesión",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) },
                        onClick = {
                            menuExpanded = false
                            showConfirmDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    )
                }
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(text = "Cerrar Sesión", fontWeight = FontWeight.Bold) },
                text = { Text(text = "¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            onCerrarSesionClick()
                        }
                    ) {
                        Text("Aceptar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun JuntadaCard(juntada: Juntada, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = juntada.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(
                    color = if (juntada.rol == "Creador") Color(0xFFDBEAFE) else Color(0xFFD1FAE5),
                    contentColor = if (juntada.rol == "Creador") Color(0xFF1E40AF) else Color(0xFF065F46),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = juntada.rol,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val badgeColor = when(juntada.estado) {
                "CONFIRMADA" -> Color(0xFF10B981)
                "PASADA" -> Color(0xFF6B7280)
                else -> Color(0xFF1F2937)
            }

            val badgeTexto = when(juntada.estado) {
                "PASADA" -> "Pasada"
                "PENDIENTE" -> "Pendiente"
                "CONFIRMADA" -> "Confirmada"
                else -> "Estado desconocido"
            }

            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = badgeTexto,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!juntada.fechaHoraInicio.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha y hora",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val inicioBonito = formatearFechaHoraEspaniol(juntada.fechaHoraInicio)
                    val textoFinal = if (!juntada.fechaHoraFin.isNullOrEmpty()) {
                        val finBonito = formatearFechaHoraEspaniol(juntada.fechaHoraFin)
                        "Desde: $inicioBonito\nHasta: $finBonito"
                    } else {
                        inicioBonito
                    }

                    Text(
                        text = textoFinal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Código: ${juntada.codigo}", color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Código de Juntada", juntada.codigo)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(context, "Código copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6))
            ) {
                Text("Compartir Código")
            }
        }
    }
}

@Composable
fun HangoutBottomNavigation(
    onProfileClick: () -> Unit,
    onNavigateToPlanificador: () -> Unit,
    onNavigateToDescuentos: () -> Unit,
    onNavigateToJuntadas: () -> Unit = {},
    pantallaActual: String = "Juntadas",
    esPersonal: Boolean = true
) {
    val selectedBlue = Color(0xFF3B82F6)
    val lightBlue = selectedBlue.copy(alpha = 0.15f)

    NavigationBar(containerColor = Color.White) {
        val label1 = if (esPersonal) "Juntadas" else "Estadísticas"
        val label2 = if (esPersonal) "Lugares" else "Descuentos"
        val icon = if (esPersonal) Icons.Default.Home else Icons.Default.Analytics
        
        NavigationBarItem(
            icon = { Icon(icon, label1) },
            label = { Text(label1) },
            selected = pantallaActual == "Juntadas" || pantallaActual == "Estadísticas",
            onClick = onNavigateToJuntadas,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedBlue,
                selectedTextColor = selectedBlue,
                indicatorColor = lightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, label2) },
            label = { Text(label2) },
            selected = pantallaActual == "Planificador" || pantallaActual == "Descuentos",
            onClick = {
                if (label2 == "Lugares") {
                    onNavigateToPlanificador()
                } else {
                    onNavigateToDescuentos()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedBlue,
                selectedTextColor = selectedBlue,
                indicatorColor = lightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") },
            selected = pantallaActual == "Perfil",
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedBlue,
                selectedTextColor = selectedBlue,
                indicatorColor = lightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

suspend fun fetchJuntadas(userId: Int, token: String): List<Juntada> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/juntadas/$userId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()

                val bodyString = response.body?.string() ?: ""
                val jsonArray = JSONArray(bodyString)
                val lista = mutableListOf<Juntada>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val idOrganizador = obj.getInt("id_organizador")
                    val rolCalculado = if (idOrganizador == userId) "Creador" else "Invitado"
                    lista.add(Juntada(
                        id = obj.getInt("id"),
                        titulo = obj.getString("titulo"),
                        codigo = obj.getString("codigo"),
                        idOrganizador = idOrganizador,
                        rol = rolCalculado,
                        organizador = obj.optString("organizador", ""),
                        participantes = emptyList(),
                        estado = obj.optString("estado", "PENDIENTE"),
                        idPropuestaGanadora = if (obj.isNull("id_propuesta_ganadora")) null else obj.getInt("id_propuesta_ganadora"),
                        fechaHoraInicio = if (obj.isNull("fecha_hora_inicio")) null else obj.getString("fecha_hora_inicio"),
                        fechaHoraFin = if (obj.isNull("fecha_hora_fin")) null else obj.getString("fecha_hora_fin")
                    ))
                }
                lista
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
