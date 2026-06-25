package com.grupo4.hangout.ui.modificarNegocioPantalla

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Filtro
import com.grupo4.hangout.model.Negocio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class HorarioDia(
    val dia: String,
    val desdeHora: Int,
    val desdeMin: Int,
    val hastaHora: Int,
    val hastaMin: Int
)

val diasSemana = listOf(
    "LU" to "Lunes",
    "MA" to "Martes",
    "MI" to "Miércoles",
    "JU" to "Jueves",
    "VI" to "Viernes",
    "SA" to "Sábado",
    "DO" to "Domingo",
    "FE" to "Feriados"
)

fun parseHorarios(horariosStr: String): List<HorarioDia> {
    if (horariosStr.isBlank()) return emptyList()
    return try {
        horariosStr.split(",").mapNotNull { part ->
            val cleaned = part.trim()
            if (cleaned.length < 3) return@mapNotNull null

            // Los primeros 2 caracteres son el día
            val dia = cleaned.take(2).uppercase()
            val resto = cleaned.drop(2).replace("-", " ").trim().replace(" ", "-")
            val horas = resto.split("-")
            if (horas.size == 2) {
                fun parseTime(s: String): Pair<Int, Int>? {
                    val t = s.trim()
                    return if (t.contains(":")) {
                        val pts = t.split(":")
                        val h = pts[0].toIntOrNull() ?: return null
                        val m = if (pts.size > 1) pts[1].toIntOrNull() ?: 0 else 0
                        h to m
                    } else {
                        val h = t.toIntOrNull() ?: return null
                        h to 0
                    }
                }
                val desde = parseTime(horas[0])
                val hasta = parseTime(horas[1])
                if (desde != null && hasta != null) {
                    HorarioDia(dia, desde.first, desde.second, hasta.first, hasta.second)
                } else null
            } else null
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun formatHorarios(horarios: List<HorarioDia>): String {
    return horarios.joinToString(",") { h ->
        val desde = String.format("%02d:%02d", h.desdeHora, h.desdeMin)
        val hasta = String.format("%02d:%02d", h.hastaHora, h.hastaMin)
        "${h.dia}$desde-$hasta"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModificarNegocioPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit,
    onNavigateToEditarImagenes: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var horariosList by remember { mutableStateOf<List<HorarioDia>>(emptyList()) }
    var sitioWeb by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var URLUbicacion by remember{ mutableStateOf("") }
    var selectedFiltrosIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var allFiltros by remember { mutableStateOf<List<Filtro>>(emptyList()) }

    var esNegocioNuevo by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showFiltrosDialog by remember { mutableStateOf(false) }
    var showHorarioDialog by remember { mutableStateOf(false) }
    var showCancelarConfirmDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val negocioData = fetchNegocio(userId, token)
            val filtrosData = fetchAllFiltros()

            if (negocioData != null) {
                nombre = negocioData.nombre
                descripcion = negocioData.descripcion
                horariosList = parseHorarios(negocioData.horarios)
                ubicacion = negocioData.ubicacion
                sitioWeb = negocioData.sitioWeb
                selectedFiltrosIds = negocioData.filtros.map { f -> f.id }.toSet()
                URLUbicacion = negocioData.URLUbicacion
                esNegocioNuevo = false
            } else {
                esNegocioNuevo = true
            }
            allFiltros = filtrosData
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (esNegocioNuevo) "Completar datos del Negocio" else "Editar Negocio",
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                // Banner informativo solo la primera vez
                if (esNegocioNuevo) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Es la primera vez que cargás tu negocio. Completá nombre, descripción, ubicación y al menos un horario para continuar. Una vez creado, podrás gestionar las imágenes de tu negocio.",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Negocio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    placeholder = { Text("Ej: Av. Corrientes 1234, CABA") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorUbicacion(ubicacion = URLUbicacion) {
                    URLUbicacion = it
                }

                Text("Horarios de Atención:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    horariosList.forEach { horario ->
                        val diaNombre = diasSemana.find { it.first == horario.dia }?.second ?: horario.dia
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val desde = String.format("%02d:%02d", horario.desdeHora, horario.desdeMin)
                                val hasta = String.format("%02d:%02d", horario.hastaHora, horario.hastaMin)
                                Text("$diaNombre: $desde a $hasta", modifier = Modifier.weight(1f), fontSize = 14.sp)
                                IconButton(onClick = { horariosList = horariosList - horario }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { showHorarioDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar Horario")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sitioWeb,
                    onValueChange = { sitioWeb = it },
                    label = { Text("Sitio Web") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Filtros:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                FlowRow(
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allFiltros.filter { it.id in selectedFiltrosIds }.forEach { filtro ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(filtro.nombre) },
                            colors = SuggestionChipDefaults.suggestionChipColors(labelColor = PrimaryColor)
                        )
                    }
                    if (allFiltros.none { it.id in selectedFiltrosIds }) {
                        Text("Ningún filtro seleccionado", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { showFiltrosDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB), contentColor = Color.Black)
                ) {
                    Text("Seleccionar Filtros")
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onNavigateToEditarImagenes,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !esNegocioNuevo,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (!esNegocioNuevo) PrimaryColor else Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gestionar Imágenes")
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isSaving) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else {
                    Button(
                        onClick = {
                            // Validaciones siempre requeridas
                            if (nombre.isBlank()) {
                                Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // Primera vez: exigir todos los campos obligatorios
                            if (esNegocioNuevo) {
                                if (descripcion.isBlank()) {
                                    Toast.makeText(context, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (ubicacion.isBlank()) {
                                    Toast.makeText(context, "La ubicación es obligatoria", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (horariosList.isEmpty()) {
                                    Toast.makeText(context, "Agregá al menos un horario", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            isSaving = true
                            coroutineScope.launch {
                                val horariosStr = formatHorarios(horariosList)
                                val result = saveNegocio(userId, token, nombre, descripcion, horariosStr, ubicacion, sitioWeb, selectedFiltrosIds.toList(), URLUbicacion)
                                isSaving = false
                                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                if (result.first) {
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(
                            if (esNegocioNuevo) "Guardar información" else "Guardar Cambios",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { showCancelarConfirmDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .wrapContentWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "Cancelar Cambios",
                        color = Color.Red,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showHorarioDialog) {
        DialogHorario(
            diasYaOcupados = horariosList.map { it.dia }.toSet(),
            onDismiss = { showHorarioDialog = false },
            onConfirm = { nuevoHorario ->
                horariosList = (horariosList.filter { it.dia != nuevoHorario.dia } + nuevoHorario).sortedBy { h ->
                    diasSemana.indexOfFirst { it.first == h.dia }
                }
                showHorarioDialog = false
            }
        )
    }

    if (showFiltrosDialog) {
        Dialog(onDismissRequest = { showFiltrosDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).heightIn(max = 500.dp)) {
                    Text("Seleccionar Filtros", style = MaterialTheme.typography.titleLarge, color = PrimaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(allFiltros) { filtro ->
                            val isChecked = filtro.id in selectedFiltrosIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFiltrosIds = if (isChecked) {
                                            selectedFiltrosIds - filtro.id
                                        } else {
                                            selectedFiltrosIds + filtro.id
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedFiltrosIds = if (checked) {
                                            selectedFiltrosIds + filtro.id
                                        } else {
                                            selectedFiltrosIds - filtro.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                                )
                                Text(filtro.nombre, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFiltrosDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }

    if (showCancelarConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelarConfirmDialog = false },
            title = {
                Text(
                    text = "¿Descartar cambios?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Si cancelás, vas a perder todas las modificaciones que no hayas guardado.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelarConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sí, cancelar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelarConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryColor)
                ) {
                    Text("Continuar editando")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun DialogHorario(diasYaOcupados: Set<String>, onDismiss: () -> Unit, onConfirm: (HorarioDia) -> Unit) {
    val diasDisponibles = diasSemana.filter { it.first !in diasYaOcupados }
    var selectedDia by remember { mutableStateOf(if (diasDisponibles.isNotEmpty()) diasDisponibles[0].first else "") }
    var desdeHora by remember { mutableIntStateOf(8) }
    var desdeMin by remember { mutableIntStateOf(0) }
    var hastaHora by remember { mutableIntStateOf(20) }
    var hastaMin by remember { mutableIntStateOf(0) }
    var expandedDia by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Agregar Horario", style = MaterialTheme.typography.titleLarge, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))

                if (diasDisponibles.isEmpty()) {
                    Text("Todos los días ya tienen un horario configurado.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Cerrar") }
                } else {
                    Text("Día", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedDia = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                        ) {
                            Text(diasSemana.find { it.first == selectedDia }?.second ?: "Seleccionar")
                        }
                        DropdownMenu(expanded = expandedDia, onDismissRequest = { expandedDia = false }) {
                            diasDisponibles.forEach { (code, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = {
                                    selectedDia = code
                                    expandedDia = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Desde", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            NumberPicker(value = desdeHora, onValueChange = { desdeHora = it }, range = 0..23)
                        }
                        Text(" : ", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            NumberPicker(value = desdeMin, onValueChange = { desdeMin = it }, range = (0..55 step 5).toList())
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Hasta", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            NumberPicker(value = hastaHora, onValueChange = { hastaHora = it }, range = 0..23)
                        }
                        Text(" : ", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            NumberPicker(value = hastaMin, onValueChange = { hastaMin = it }, range = (0..55 step 5).toList())
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = PrimaryColor)) { Text("Cancelar") }
                        Button(
                            onClick = {
                                val totalDesde = desdeHora * 60 + desdeMin
                                val totalHasta = hastaHora * 60 + hastaMin
                                if (totalDesde < totalHasta && selectedDia.isNotEmpty()) {
                                    onConfirm(HorarioDia(selectedDia, desdeHora, desdeMin, hastaHora, hastaMin))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(value: Int, onValueChange: (Int) -> Unit, range: Iterable<Int>) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text(String.format("%02d", value), fontSize = 16.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            range.forEach { num ->
                DropdownMenuItem(text = { Text(String.format("%02d", num)) }, onClick = {
                    onValueChange(num)
                    expanded = false
                })
            }
        }
    }
}

suspend fun fetchNegocio(userId: Int, token: String): Negocio? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                if (!json.getBoolean("existe")) return@withContext null

                val negJson = json.getJSONObject("negocio")
                val filtrosArr = negJson.getJSONArray("filtros")
                val filtrosList = mutableListOf<Filtro>()
                for (i in 0 until filtrosArr.length()) {
                    val f = filtrosArr.getJSONObject(i)
                    filtrosList.add(Filtro(f.getInt("id"), f.getString("nombre")))
                }

                Negocio(
                    id = negJson.getInt("id"),
                    nombre = negJson.optString("nombre", ""),
                    descripcion = negJson.optString("descripcion", ""),
                    horarios = negJson.optString("horarios", ""),
                    ubicacion = negJson.optString("ubicacion", ""),
                    sitioWeb = negJson.optString("sitio_web", ""),
                    URLUbicacion = negJson.optString("url_ubicacion", ""),
                    filtros = filtrosList
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

suspend fun fetchAllFiltros(): List<Filtro> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/filtros/listar")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val filtrosArr = json.getJSONArray("filtros")
                val list = mutableListOf<Filtro>()
                for (i in 0 until filtrosArr.length()) {
                    val f = filtrosArr.getJSONObject(i)
                    list.add(Filtro(f.getInt("id"), f.getString("nombre")))
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

suspend fun saveNegocio(
    userId: Int,
    token: String,
    nombre: String,
    descripcion: String,
    horarios: String,
    ubicacion: String,
    sitioWeb: String,
    filtros: List<Int>,
    URLUbicacion: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("nombre", nombre)
            put("descripcion", descripcion)
            put("horarios", horarios)
            put("ubicacion", ubicacion)
            put("sitio_web", sitioWeb)
            put("filtros", JSONArray(filtros))
            put("url_ubicacion", URLUbicacion)
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/$userId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = if (responseBody.isNotBlank()) JSONObject(responseBody) else JSONObject()
                if (response.isSuccessful) {
                    val msg = jsonResponse.optString("mensaje", "Información guardada con éxito")
                    Pair(true, msg)
                } else {
                    val err = jsonResponse.optString("error", "Error al guardar los cambios")
                    Pair(false, err)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}
