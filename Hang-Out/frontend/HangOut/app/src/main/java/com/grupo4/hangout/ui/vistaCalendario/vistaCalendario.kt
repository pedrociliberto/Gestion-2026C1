package com.grupo4.hangout.ui.vistaCalendario

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.model.Juntada
import com.grupo4.hangout.ui.panelJuntadasPantalla.fetchJuntadas
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

enum class CalendarViewMode { MONTH, WEEK, DAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaCalendario(
    userId: Int,
    onBack: () -> Unit,
    onNavigateToDetalleJuntada: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var juntadas by remember { mutableStateOf<List<Juntada>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("token", "") ?: ""
            juntadas = fetchJuntadas(userId, token)
        }
    }

    val juntadasDelDia = remember(juntadas, selectedDate) {
        juntadas.filter { juntada ->
            juntada.fechaHoraInicio?.let { fechaStr ->
                try {
                    val fechaPart = fechaStr.split("T")[0].split(" ")[0]
                    fechaPart == selectedDate.toString()
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selector de vista
            TabRow(
                selectedTabIndex = viewMode.ordinal,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    if (viewMode.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[viewMode.ordinal]),
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
            ) {
                CalendarViewMode.entries.forEach { mode ->
                    Tab(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        text = {
                            Text(
                                text = when(mode) {
                                    CalendarViewMode.MONTH -> "Mes"
                                    CalendarViewMode.WEEK -> "Semana"
                                    CalendarViewMode.DAY -> "Día"
                                },
                                fontWeight = if (viewMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (viewMode == mode) Color(0xFF3B82F6) else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    when (viewMode) {
                        CalendarViewMode.MONTH -> currentMonth = currentMonth.minusMonths(1)
                        CalendarViewMode.WEEK -> {
                            selectedDate = selectedDate.minusWeeks(1)
                            currentMonth = YearMonth.from(selectedDate)
                        }
                        CalendarViewMode.DAY -> {
                            selectedDate = selectedDate.minusDays(1)
                            currentMonth = YearMonth.from(selectedDate)
                        }
                    }
                }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                val title = when (viewMode) {
                    CalendarViewMode.MONTH -> "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}"
                    CalendarViewMode.WEEK -> {
                        val firstDayOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
                        val lastDayOfWeek = firstDayOfWeek.plusDays(6)
                        if (firstDayOfWeek.month == lastDayOfWeek.month) {
                            "${firstDayOfWeek.dayOfMonth} - ${lastDayOfWeek.dayOfMonth} ${firstDayOfWeek.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }}"
                        } else {
                            "${firstDayOfWeek.dayOfMonth} ${firstDayOfWeek.month.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))} - ${lastDayOfWeek.dayOfMonth} ${lastDayOfWeek.month.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))}"
                        }
                    }
                    CalendarViewMode.DAY -> selectedDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES")))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    when (viewMode) {
                        CalendarViewMode.MONTH -> currentMonth = currentMonth.plusMonths(1)
                        CalendarViewMode.WEEK -> {
                            selectedDate = selectedDate.plusWeeks(1)
                            currentMonth = YearMonth.from(selectedDate)
                        }
                        CalendarViewMode.DAY -> {
                            selectedDate = selectedDate.plusDays(1)
                            currentMonth = YearMonth.from(selectedDate)
                        }
                    }
                }) {
                    Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewMode != CalendarViewMode.DAY) {
                // Días de la semana
                Row(modifier = Modifier.fillMaxWidth()) {
                    val diasSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    diasSemana.forEach { dia ->
                        Text(
                            text = dia,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (viewMode) {
                CalendarViewMode.MONTH -> {
                    // Cuadrícula del calendario mensual
                    val firstDayOfMonth = currentMonth.atDay(1)
                    val dayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
                    var dateIterator = firstDayOfMonth.minusDays((dayOfWeek - 1).toLong())

                    for (week in 0 until 6) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (day in 0 until 7) {
                                CalendarDayItem(
                                    date = dateIterator,
                                    isSelected = dateIterator == selectedDate,
                                    isToday = dateIterator == today,
                                    isCurrentMonth = YearMonth.from(dateIterator) == currentMonth,
                                    hasJuntadas = juntadas.any { j -> j.fechaHoraInicio?.startsWith(dateIterator.toString()) == true },
                                    onClick = { selectedDate = it }
                                )
                                dateIterator = dateIterator.plusDays(1)
                            }
                        }
                        if (YearMonth.from(dateIterator) > currentMonth && week >= 4) break
                    }
                }
                CalendarViewMode.WEEK -> {
                    val firstDayOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
                    var dateIterator = firstDayOfWeek
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (day in 0 until 7) {
                            CalendarDayItem(
                                date = dateIterator,
                                isSelected = dateIterator == selectedDate,
                                isToday = dateIterator == today,
                                isCurrentMonth = true,
                                hasJuntadas = juntadas.any { j -> j.fechaHoraInicio?.startsWith(dateIterator.toString()) == true },
                                onClick = { selectedDate = it }
                            )
                            dateIterator = dateIterator.plusDays(1)
                        }
                    }
                }
                CalendarViewMode.DAY -> {
                    // No grid for day view, just show the selected date large
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val subtitle = if (viewMode == CalendarViewMode.DAY) "Horarios para hoy" else "Juntadas para el ${selectedDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES")))}"
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (juntadasDelDia.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay juntadas para este día.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(juntadasDelDia) { juntada ->
                        if (viewMode == CalendarViewMode.DAY) {
                            JuntadaCardConHorario(juntada, onClick = { onNavigateToDetalleJuntada(juntada.id) })
                        } else {
                            JuntadaCardLocal(juntada, onClick = { onNavigateToDetalleJuntada(juntada.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    hasJuntadas: Boolean,
    onClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF3B82F6)
                else if (hasJuntadas && isCurrentMonth) Color(0xFFDBEAFE)
                else if (isToday) Color(0xFFF3F4F6)
                else Color.Transparent
            )
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isSelected) Color.White
                else if (isToday) Color(0xFF3B82F6)
                else if (isCurrentMonth) Color.Black
                else Color.LightGray,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasJuntadas && !isSelected && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
            }
        }
    }
}

@Composable
fun JuntadaCardConHorario(juntada: Juntada, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna de Horario
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = extraerHora(juntada.fechaHoraInicio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
                if (!juntada.fechaHoraFin.isNullOrEmpty()) {
                    Box(modifier = Modifier.height(10.dp).width(2.dp).background(Color.LightGray))
                    Text(
                        text = extraerHora(juntada.fechaHoraFin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info de la Juntada
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = juntada.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (juntada.estado == "CONFIRMADA" && !juntada.fechaHoraInicio.isNullOrEmpty()) {
                        val context = LocalContext.current
                        IconButton(
                            onClick = { exportJuntadaToCalendar(context, juntada) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Exportar al calendario",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Rol: ${juntada.rol}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Surface(
                    color = when(juntada.estado) {
                        "CONFIRMADA" -> Color(0xFF10B981)
                        "PASADA" -> Color(0xFF6B7280)
                        else -> Color(0xFFF59E0B)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = juntada.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun extraerHora(fechaStr: String?): String {
    if (fechaStr == null) return "--:--"
    return try {
        val parts = fechaStr.split("T")
        if (parts.size > 1) {
            parts[1].substring(0, 5) // "HH:mm"
        } else {
            val partsSpace = fechaStr.split(" ")
            if (partsSpace.size > 1) {
                partsSpace[1].substring(0, 5)
            } else "--:--"
        }
    } catch (e: Exception) {
        "--:--"
    }
}

@Composable
fun JuntadaCardLocal(juntada: Juntada, onClick: () -> Unit) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = juntada.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (juntada.estado == "CONFIRMADA" && !juntada.fechaHoraInicio.isNullOrEmpty()) {
                    val context = LocalContext.current
                    IconButton(onClick = { exportJuntadaToCalendar(context, juntada) }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Exportar al calendario",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                }

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

                    val inicioBonito = com.grupo4.hangout.ui.detalleJuntadaPantalla.formatearFechaHoraEspaniol(juntada.fechaHoraInicio!!)
                    val textoFinal = if (!juntada.fechaHoraFin.isNullOrEmpty()) {
                        val finBonito = com.grupo4.hangout.ui.detalleJuntadaPantalla.formatearFechaHoraEspaniol(juntada.fechaHoraFin!!)
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
            Button(
                onClick = { /* Código para Compartir Link */ },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6))
            ) {
                Text("Compartir Código")
            }
        }
    }
}

fun exportJuntadaToCalendar(context: Context, juntada: Juntada) {
    val title = juntada.titulo
    val description = "Organizado vía HangOut App\nCódigo: ${juntada.codigo}"
    val location = "Ver detalles en la App HangOut"

    val formatter = DateTimeFormatter.ISO_DATE_TIME
    
    val startTimeMillis = try {
        val dateStr = juntada.fechaHoraInicio?.replace(" ", "T") ?: ""
        val ldt = LocalDateTime.parse(dateStr)
        ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
        
        juntada.fechaHoraFin?.let {
            try {
                val dateStrEnd = it.replace(" ", "T")
                val ldtEnd = LocalDateTime.parse(dateStrEnd)
                val endTimeMillis = ldtEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
            } catch (e: Exception) {}
        }
    }
    context.startActivity(intent)
}

