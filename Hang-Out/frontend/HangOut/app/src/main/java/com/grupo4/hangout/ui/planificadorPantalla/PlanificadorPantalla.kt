package com.grupo4.hangout.ui.planificadorPantalla

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.grupo4.hangout.model.Filtro
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutBottomNavigation
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutTopBar
import com.grupo4.hangout.ui.notificacionesPantalla.fetchNotificacionesRecibidas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

val DIAS_SEMANA = listOf(
    "LU" to "Lunes",
    "MA" to "Martes",
    "MI" to "Miércoles",
    "JU" to "Jueves",
    "VI" to "Viernes",
    "SA" to "Sábado",
    "DO" to "Domingo"
)

private val AZUL       = PrimaryColor
private val AZUL_CLARO = Color(0xFFEFF6FF)
private val GRIS_FONDO = Color(0xFFF1F5F9)
private val GRIS_BORDE = Color(0xFFE2E8F0)
private val TEXTO_DARK = Color(0xFF0F172A)
private val TEXTO_GRIS = Color(0xFF64748B)
private val VERDE      = Color(0xFF16A34A)
private val VERDE_CLARO= Color(0xFFDCFCE7)

private const val PREFS_NAME     = "hangout_filtros_guardados"
private const val KEY_FILTROS    = "filtros_seleccionados"
private const val KEY_DIAS       = "dias_seleccionados"
private const val KEY_HORA_DESDE = "hora_desde"
private const val KEY_HORA_HASTA = "hora_hasta"
private const val KEY_USAR_PREF   = "usar_preferencias_perfil"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanificadorPantalla(
    userId: Int,
    esPersonal: Boolean = true,
    modifier: Modifier = Modifier,
    onNegocioClick: (Negocio) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onCerrarSesionClick: () -> Unit,
    onNavigateToJuntadas: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var resultados by remember { mutableStateOf<List<Negocio>>(emptyList()) }
    var textoBusqueda by remember { mutableStateOf("") }
    var buscado by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableIntStateOf(0) }

    var filtrosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var filtrosSeleccionados by remember {
        mutableStateOf<Set<String>>(prefs.getStringSet(KEY_FILTROS, emptySet()) ?: emptySet())
    }
    var diasSeleccionados by remember {
        mutableStateOf<Set<String>>(prefs.getStringSet(KEY_DIAS, emptySet()) ?: emptySet())
    }
    var horaDesde by remember { mutableStateOf(prefs.getString(KEY_HORA_DESDE, null)) }
    var horaHasta by remember { mutableStateOf(prefs.getString(KEY_HORA_HASTA, null)) }
    var usarFiltrosPersonales by remember {
        mutableStateOf(prefs.getBoolean(KEY_USAR_PREF, false))
    }
    var mostrarFiltros by remember { mutableStateOf(false) }
    var guardadoOk by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        coroutineScope.launch { filtrosDisponibles = fetchFiltrosDisponibles() }
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("token", "") ?: ""
            val notis = fetchNotificacionesRecibidas(userId, token)
            unreadCount = notis.count { !it.leida }
        }
    }

    LaunchedEffect(textoBusqueda, filtrosSeleccionados, diasSeleccionados, horaDesde, horaHasta, usarFiltrosPersonales) {
        if (textoBusqueda.isNotEmpty()) {
            delay(300) // Debounce typing
        }
        buscado = true
        resultados = fetchLugares(
            textoBusqueda, userId,
            filtrosSeleccionados.toList(),
            diasSeleccionados.toList(),
            horaDesde, horaHasta,
            usarFiltrosPersonales
        )
    }

    val cantFiltrosActivos = filtrosSeleccionados.size +
            diasSeleccionados.size +
            (if (horaDesde != null || horaHasta != null) 1 else 0)

    fun guardarFiltros() {
        prefs.edit().apply {
            putStringSet(KEY_FILTROS, filtrosSeleccionados)
            putStringSet(KEY_DIAS, diasSeleccionados)
            if (horaDesde != null) putString(KEY_HORA_DESDE, horaDesde) else remove(KEY_HORA_DESDE)
            if (horaHasta != null) putString(KEY_HORA_HASTA, horaHasta) else remove(KEY_HORA_HASTA)
        }.apply()
        mostrarFiltros = false
        guardadoOk = true
        coroutineScope.launch { delay(2000); guardadoOk = false }
    }

    Scaffold(
        topBar = {
            HangoutTopBar(
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onCerrarSesionClick = onCerrarSesionClick,
                unreadCount = unreadCount,
                esPersonal = esPersonal
            )
        },
        bottomBar = {
            HangoutBottomNavigation(
                onProfileClick = onProfileClick,
                onNavigateToPlanificador = {},
                onNavigateToJuntadas = onNavigateToJuntadas,
                onNavigateToDescuentos = {},
                pantallaActual = "Planificador"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Buscar lugares",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TEXTO_DARK
            )

            // ── BARRA DE BÚSQUEDA ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cafetería, bar, restaurante...", color = TEXTO_GRIS) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GRIS_FONDO,
                        unfocusedContainerColor = GRIS_FONDO,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(color = AZUL, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                buscado = true
                                resultados = fetchLugares(
                                    textoBusqueda, userId,
                                    filtrosSeleccionados.toList(),
                                    diasSeleccionados.toList(),
                                    horaDesde, horaHasta,
                                    usarFiltrosPersonales
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                }
            }

            // ── TOGGLE FILTROS PERSONALES ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Usar mis preferencias de perfil",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TEXTO_DARK
                    )
                    Text(
                        text = "Filtra automáticamente por tus categorías favoritas",
                        fontSize = 12.sp,
                        color = TEXTO_GRIS
                    )
                }
                Switch(
                    checked = usarFiltrosPersonales,
                    onCheckedChange = { 
                        usarFiltrosPersonales = it
                        prefs.edit().putBoolean(KEY_USAR_PREF, it).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AZUL,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = GRIS_BORDE,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            // ── BOTÓN FILTROS ─────────────────────────────────────────
            OutlinedButton(
                onClick = { mostrarFiltros = !mostrarFiltros },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AZUL),
                border = androidx.compose.foundation.BorderStroke(1.dp, AZUL)
            ) {
                Text(
                    text = if (mostrarFiltros) "Ocultar filtros" else "Filtrar búsqueda",
                    fontWeight = FontWeight.SemiBold
                )
                if (cantFiltrosActivos > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(AZUL, shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$cantFiltrosActivos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── PANEL DE FILTROS ──────────────────────────────────────
            if (mostrarFiltros) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        // ── CARACTERÍSTICAS ───────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏷️", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("Características del lugar", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXTO_DARK)
                            }
                            Text("Seleccioná uno o más para filtrar lugares que los cumplan todos.", fontSize = 12.sp, color = TEXTO_GRIS)
                            if (filtrosDisponibles.isEmpty()) {
                                Text("Cargando...", color = TEXTO_GRIS, fontSize = 13.sp)
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filtrosDisponibles.forEach { filtro ->
                                        val seleccionado = filtro in filtrosSeleccionados
                                        FilterChip(
                                            selected = seleccionado,
                                            onClick = {
                                                filtrosSeleccionados = if (seleccionado)
                                                    filtrosSeleccionados - filtro
                                                else
                                                    filtrosSeleccionados + filtro
                                            },
                                            label = { Text(filtro, fontSize = 13.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = GRIS_FONDO,
                                                selectedContainerColor = AZUL,
                                                selectedLabelColor = Color.White,
                                                labelColor = TEXTO_DARK
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = GRIS_BORDE)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🕐", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("Disponibilidad horaria", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXTO_DARK)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Día(s) de la semana", fontSize = 13.sp, color = TEXTO_GRIS)
                                if (diasSeleccionados.size > 1) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("${diasSeleccionados.size} seleccionados", fontSize = 11.sp, color = AZUL, fontWeight = FontWeight.Medium)
                                }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DIAS_SEMANA.forEach { (codigo, nombre) ->
                                    val seleccionado = codigo in diasSeleccionados
                                    FilterChip(
                                        selected = seleccionado,
                                        onClick = {
                                            diasSeleccionados = if (seleccionado)
                                                diasSeleccionados - codigo
                                            else
                                                diasSeleccionados + codigo
                                        },
                                        label = { Text(nombre, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = GRIS_FONDO,
                                            selectedContainerColor = AZUL,
                                            selectedLabelColor = Color.White,
                                            labelColor = TEXTO_DARK
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Text("Rango horario (podés elegir uno o los dos)", fontSize = 13.sp, color = TEXTO_GRIS)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SelectorHora(
                                    label = "Desde",
                                    hora = horaDesde,
                                    onHoraSeleccionada = { horaDesde = it },
                                    onLimpiar = { horaDesde = null },
                                    modifier = Modifier.weight(1f)
                                )
                                SelectorHora(
                                    label = "Hasta",
                                    hora = horaHasta,
                                    onHoraSeleccionada = { horaHasta = it },
                                    onLimpiar = { horaHasta = null },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = GRIS_BORDE)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { guardarFiltros() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (guardadoOk) VERDE else AZUL
                                )
                            ) {
                                Text(
                                    text = if (guardadoOk) "✓ Guardado" else " Guardar filtro",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    filtrosSeleccionados = emptySet()
                                    diasSeleccionados = emptySet()
                                    horaDesde = null
                                    horaHasta = null
                                    prefs.edit().apply {
                                        remove(KEY_FILTROS); remove(KEY_DIAS)
                                        remove(KEY_HORA_DESDE); remove(KEY_HORA_HASTA)
                                    }.apply()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                                enabled = cantFiltrosActivos > 0
                            ) {
                                Text("✕  Limpiar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── RESULTADOS ────────────────────────────────────────────
            if (buscado && resultados.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GRIS_FONDO)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No encontramos lugares con esos criterios.", color = TEXTO_GRIS, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            resultados.forEach { negocio ->
                TarjetaNegocioSimple(negocio = negocio, onClick = { onNegocioClick(negocio) })
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── TARJETA DE NEGOCIO ────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaNegocioSimple(negocio: Negocio, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (negocio.tienePosicionamiento) {
                Surface(
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        "⭐ Publicidad",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B6000),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(text = negocio.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(Modifier.height(4.dp))
            Text(text = negocio.descripcion, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 20.sp)

            if (negocio.ubicacion.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(negocio.ubicacion, fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }

            if (negocio.filtros.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    negocio.filtros.forEach { filtro ->
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = filtro.nombre, fontSize = 12.sp, color = AZUL, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ── LLAMADAS AL BACKEND ───────────────────────────────────────────────

suspend fun fetchFiltrosDisponibles(): List<String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url("${Config.BASE_URL}/filtros/listar").build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = JSONObject(response.body?.string() ?: "")
                val arr = json.getJSONArray("filtros")
                (0 until arr.length()).map { arr.getJSONObject(it).getString("nombre") }
            }
        } catch (_: Exception) { emptyList() }
    }
}

suspend fun fetchLugares(
    textoBusqueda: String,
    userId: Int,
    filtrosSeleccionados: List<String> = emptyList(),
    diasSeleccionados: List<String> = emptyList(),
    horarioDesde: String? = null,
    horarioHasta: String? = null,
    usarFiltrosPersonales: Boolean = false
): List<Negocio> {
    return withContext(Dispatchers.IO) {
        val url = buildString {
            append("${Config.BASE_URL}/negocios?busqueda=$textoBusqueda&id_usuario=$userId")
            if (usarFiltrosPersonales) append("&usar_filtros_usuario=true")
            filtrosSeleccionados.forEach { append("&filtros=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            diasSeleccionados.forEach { append("&horario_dia=$it") }
            if (horarioDesde != null) append("&horario_hora_desde=$horarioDesde")
            if (horarioHasta != null) append("&horario_hora_hasta=$horarioHasta")
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = JSONObject(response.body?.string() ?: "")
                val arr  = json.getJSONArray("resultados")
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    val filtrosArr = obj.getJSONArray("filtros")
                    val filtros = (0 until filtrosArr.length()).map { j ->
                        Filtro(id = 0, nombre = filtrosArr.getString(j))
                    }
                    Negocio(
                        id          = obj.optInt("id", 0),
                        nombre      = obj.getString("nombre"),
                        descripcion = obj.getString("descripcion"),
                        horarios    = obj.getString("horarios"),
                        ubicacion   = obj.optString("ubicacion", ""),
                        sitioWeb   = obj.getString("sitio_web"),
                        URLUbicacion = obj.optString("url_ubicacion", ""),
                        filtros     = filtros,
                        tienePosicionamiento = obj.optBoolean("tiene_posicionamiento", false)
                    )
                }
            }
        } catch (_: Exception) { emptyList() }
    }
}