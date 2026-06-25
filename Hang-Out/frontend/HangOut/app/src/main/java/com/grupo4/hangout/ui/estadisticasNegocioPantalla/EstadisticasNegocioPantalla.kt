package com.grupo4.hangout.ui.estadisticasNegocioPantalla

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Aparicion
import com.grupo4.hangout.model.EstadisticasNegocio
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.model.PropuestasGanadoras
import com.grupo4.hangout.ui.modificarNegocioPantalla.fetchNegocio
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutBottomNavigation
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutTopBar
import com.grupo4.hangout.ui.resenias.fetchReseniasNegocio
import com.grupo4.hangout.ui.resenias.ReseniaItem
import com.grupo4.hangout.model.Resenia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class EstadoDiaCount(
    var confirmadas: Int = 0,
    var enVotacion: Int = 0,
    var rechazadas: Int = 0
) {
    val total get() = confirmadas + enVotacion + rechazadas
}

enum class OrdenResenias(val descripcion: String) {
    MAYOR_MENOR("Mayor a menor puntaje"),
    MENOR_MAYOR("Menor a mayor puntaje")
}

@Composable
fun EstadisticasNegocioPantalla(
    userId: Int,
    token: String,
    onNavigateToPerfil: () -> Unit,
    onNavigateToPlanificador: () -> Unit,
    onNavigateToDescuentos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNavigateToModificarNegocio: () -> Unit
) {
    var stats by remember { mutableStateOf<EstadisticasNegocio?>(null) }
    var negocio by remember { mutableStateOf<Negocio?>(null) }
    var listaResenias by remember { mutableStateOf<List<Resenia>>(emptyList()) }

    var isLoadingStats by remember { mutableStateOf(true) }
    var isLoadingResenias by remember { mutableStateOf(true) }

    var fechaDesde by remember { mutableStateOf<Date?>(null) }
    var fechaHasta by remember { mutableStateOf<Date?>(null) }
    var criterioOrden by remember { mutableStateOf(OrdenResenias.MAYOR_MENOR) }
    var menuOrdenExpandido by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val fetchedNegocio = fetchNegocio(userId, token)
            negocio = fetchedNegocio

            if (fetchedNegocio != null) {
                listaResenias = fetchReseniasNegocio(fetchedNegocio.id)
            }
            isLoadingResenias = false
        }
        coroutineScope.launch {
            stats = fetchEstadisticas(userId, token)
            isLoadingStats = false
        }
    }

    val aparicionesFiltradas = remember(stats, fechaDesde, fechaHasta) {
        stats?.apariciones?.filter { aparicion ->
            val fecha = parseIsoDate(aparicion.fecha_hora_inicio)
            val desdeOk = fechaDesde == null || (fecha != null && !fecha.before(fechaDesde))
            val hastaOk = fechaHasta == null || (fecha != null && !fecha.after(fechaHasta))
            desdeOk && hastaOk
        } ?: emptyList()
    }

    val reseniasOrdenadas = remember(listaResenias, criterioOrden) {
        if (criterioOrden == OrdenResenias.MAYOR_MENOR) {
            listaResenias.sortedByDescending { it.valoracion }
        } else {
            listaResenias.sortedBy { it.valoracion }
        }
    }

    Scaffold(
        topBar = {
            HangoutTopBar(
                onProfileClick = onNavigateToPerfil,
                onCerrarSesionClick = onCerrarSesion,
                esPersonal = false
            )
        },
        bottomBar = {
            HangoutBottomNavigation(
                onProfileClick = onNavigateToPerfil,
                onNavigateToPlanificador = onNavigateToPlanificador,
                onNavigateToDescuentos = onNavigateToDescuentos,
                onNavigateToJuntadas = {},
                pantallaActual = "Estadísticas",
                esPersonal = false
            )
        }
    ) { padding ->
        if (isLoadingStats || isLoadingResenias) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Estadísticas de mi Negocio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                if (negocio == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToModificarNegocio() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Debes cargar la información de tu negocio para que los usuarios puedan encontrarlo.",
                                fontSize = 14.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Filtros de Fecha para Propuestas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FiltroFecha(
                        label = "Desde",
                        fecha = fechaDesde,
                        onFechaSelected = { fechaDesde = it },
                        modifier = Modifier.weight(1f)
                    )
                    FiltroFecha(
                        label = "Hasta",
                        fecha = fechaHasta,
                        onFechaSelected = { fechaHasta = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (fechaDesde != null || fechaHasta != null) {
                    TextButton(
                        onClick = {
                            fechaDesde = null
                            fechaHasta = null
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Restablecer filtros", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Las estadísticas muestran la cantidad de veces que tu negocio apareció en propuestas de juntadas.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen de Estadísticas de Propuestas
                val ganadoras = aparicionesFiltradas.count { it.es_ganadora }
                val enVotacion = aparicionesFiltradas.count { it.en_votacion }
                val total = aparicionesFiltradas.size
                val rechazadas = total - ganadoras - enVotacion

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatCard("Total", total.toString(), Color(0xFF3B82F6), Modifier.weight(1f))
                    StatCard("Confirmadas", ganadoras.toString(), Color(0xFF10B981), Modifier.weight(1f))
                    StatCard("En Votación", enVotacion.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
                    StatCard("Rechazadas", rechazadas.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estadísticas de Reseñas
                Text(
                    "Actividad vista en calendario",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                BusinessCalendar(aparicionesFiltradas)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Rendimiento de Reseñas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (listaResenias.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                    ) {
                        Text(
                            text = "Tu negocio aún no registra reseñas de los usuarios para procesar estadísticas.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val promedioResenias = listaResenias.map { it.valoracion }.average()
                    val totalResenias = listaResenias.size

                    val positivasCount = listaResenias.count { it.valoracion >= 4 }
                    val porcentajePositivas = (positivasCount.toFloat() / totalResenias) * 100
                    val porcentajeNegativas = 100 - porcentajePositivas

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Promedio General", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format("%.1f", promedioResenias),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Basado en $totalResenias opiniones",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(1.dp)
                                    .background(Color(0xFFE5E7EB))
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .padding(start = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Positivas (4-5)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10B981))
                                        Text(String.format("%.0f%%", porcentajePositivas), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    }
                                    LinearProgressIndicator(
                                        progress = { porcentajePositivas / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE5E7EB), CircleShape),
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Críticas (1-3)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEF4444))
                                        Text(String.format("%.0f%%", porcentajeNegativas), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    }
                                    LinearProgressIndicator(
                                        progress = { porcentajeNegativas / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE5E7EB), CircleShape),
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Listado de opiniones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .clickable { menuOrdenExpandido = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = criterioOrden.descripcion,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }

                            DropdownMenu(
                                expanded = menuOrdenExpandido,
                                onDismissRequest = { menuOrdenExpandido = false }
                            ) {
                                OrdenResenias.values().forEach { orden ->
                                    DropdownMenuItem(
                                        text = { Text(orden.descripcion, fontSize = 13.sp) },
                                        onClick = {
                                            criterioOrden = orden
                                            menuOrdenExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    reseniasOrdenadas.forEach { resenia ->
                        ReseniaItem(resenia = resenia)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FiltroFecha(label: String, fecha: Date?, onFechaSelected: (Date?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    OutlinedCard(
        onClick = {
            val calendar = Calendar.getInstance()
            fecha?.let { calendar.time = it }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time
                    onFechaSelected(selected)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(if (fecha != null) sdf.format(fecha) else "Seleccionar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryColor)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BusinessCalendar(apariciones: List<Aparicion>) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }

    val currentMonthYear = remember(calendarMonth) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(calendarMonth.time).replaceFirstChar { it.uppercase() }
    }

    val daysInMonth = remember(calendarMonth) {
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<Int?>()
        repeat(firstDayOfWeek) { days.add(null) }
        for (i in 1..maxDays) { days.add(i) }
        days
    }

    val countsByDay = remember(apariciones, calendarMonth) {
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val counts = mutableMapOf<String, EstadoDiaCount>()
        apariciones.forEach {
            val date = parseIsoDate(it.fecha_hora_inicio)
            if (date != null) {
                val key = sdfDay.format(date)
                val dayData = counts.getOrPut(key) { EstadoDiaCount() }

                when {
                    it.es_ganadora -> dayData.confirmadas++
                    it.en_votacion -> dayData.enVotacion++
                    else -> dayData.rechazadas++
                }
            }
        }
        counts
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = calendarMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                calendarMonth = newCal
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
            }

            Text(currentMonthYear, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            IconButton(onClick = {
                val newCal = calendarMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                calendarMonth = newCal
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weekDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = daysInMonth.chunked(7)
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().height(55.dp)) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val cal = calendarMonth.clone() as Calendar
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            val dayData = countsByDay[dateStr]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Normal
                                )

                                if (dayData != null && dayData.total > 0) {
                                    Box(
                                        modifier = Modifier.size(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val coloresActivos = mutableListOf<Color>()
                                            if (dayData.confirmadas > 0) coloresActivos.add(Color(0xFF10B981))
                                            if (dayData.enVotacion > 0) coloresActivos.add(Color(0xFFF59E0B))
                                            if (dayData.rechazadas > 0) coloresActivos.add(Color(0xFFEF4444))

                                            val cantidadColores = coloresActivos.size
                                            val arcoGrados = 360f / cantidadColores
                                            var startAngle = -90f

                                            coloresActivos.forEach { color ->
                                                drawArc(
                                                    color = color,
                                                    startAngle = startAngle,
                                                    sweepAngle = arcoGrados,
                                                    useCenter = true
                                                )
                                                startAngle += arcoGrados
                                            }
                                        }

                                        Text(
                                            text = dayData.total.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 11.sp
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(22.dp))
                                }
                            }
                        }
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirmadas", fontSize = 11.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("En Votación", fontSize = 11.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rechazadas", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

private fun parseIsoDate(iso: String): Date? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.parse(iso)
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchEstadisticas(userId: Int, token: String): EstadisticasNegocio? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/apariciones/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)

                val aparicionesArr = json.getJSONArray("apariciones")
                val apariciones = mutableListOf<Aparicion>()
                for (i in 0 until aparicionesArr.length()) {
                    val obj = aparicionesArr.getJSONObject(i)
                    apariciones.add(Aparicion(
                        en_votacion = obj.getBoolean("en_votacion"),
                        es_ganadora = obj.getBoolean("es_ganadora"),
                        fecha_hora_fin = if (obj.isNull("fecha_hora_fin")) null else obj.getString("fecha_hora_fin"),
                        fecha_hora_inicio = obj.getString("fecha_hora_inicio"),
                        id_juntada = obj.getInt("id_juntada"),
                        id_propuesta = obj.getInt("id_propuesta")
                    ))
                }

                val progJson = json.getJSONObject("propuestas_ganadoras")
                val futurasArr = progJson.getJSONArray("futuras")
                val pasadasArr = progJson.getJSONArray("pasadas")

                fun parseList(arr: org.json.JSONArray): List<Aparicion> {
                    val list = mutableListOf<Aparicion>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(Aparicion(
                            en_votacion = obj.getBoolean("en_votacion"),
                            es_ganadora = obj.getBoolean("es_ganadora"),
                            fecha_hora_fin = if (obj.isNull("fecha_hora_fin")) null else obj.getString("fecha_hora_fin"),
                            fecha_hora_inicio = obj.getString("fecha_hora_inicio"),
                            id_juntada = obj.getInt("id_juntada"),
                            id_propuesta = obj.getInt("id_propuesta")
                        ))
                    }
                    return list
                }

                EstadisticasNegocio(
                    apariciones = apariciones,
                    cantidad_apariciones_en_votacion = json.getInt("cantidad_apariciones_en_votacion"),
                    cantidad_apariciones_ganadoras = json.getInt("cantidad_apariciones_ganadoras"),
                    cantidad_apariciones_no_ganadoras = json.getInt("cantidad_apariciones_no_ganadoras"),
                    cantidad_apariciones_total = json.getInt("cantidad_apariciones_total"),
                    id_negocio = json.getInt("id_negocio"),
                    propuestas_ganadoras = PropuestasGanadoras(
                        cantidad_futuras = progJson.getInt("cantidad_futuras"),
                        cantidad_pasadas = progJson.getInt("cantidad_pasadas"),
                        cantidad_total = progJson.getInt("cantidad_total"),
                        futuras = parseList(futurasArr),
                        pasadas = parseList(pasadasArr)
                    )
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}