package com.grupo4.hangout.ui.crearPropuestaPantalla

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
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
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.ui.planificadorPantalla.fetchLugares
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPropuestaPantalla(
    userId: Int,
    juntadaId: Int,
    onBack: () -> Unit
) {
    var fechaInicio by remember { mutableStateOf<LocalDateTime?>(null) }
    var fechaFin by remember { mutableStateOf<LocalDateTime?>(null) }
    var lugarTexto by remember { mutableStateOf("") }
    var selectedNegocioId by remember { mutableStateOf<Int?>(null) }
    var sugerencias by remember { mutableStateOf<List<Negocio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scrollState = rememberScrollState()

    val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
    val token = sharedPrefs.getString("token", "") ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Propuesta") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Detalles de la propuesta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )

            // Lugar (Autocomplete)
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = lugarTexto,
                    onValueChange = {
                        lugarTexto = it
                        selectedNegocioId = null // Si el usuario escribe manualmente, reseteamos el id de negocio
                        searchJob?.cancel()
                        if (it.length >= 3) {
                            searchJob = coroutineScope.launch {
                                delay(500)
                                isSearching = true
                                sugerencias = fetchLugares(it, userId)
                                isSearching = false
                            }
                        } else {
                            sugerencias = emptyList()
                        }
                    },
                    label = { Text("Lugar (Obligatorio)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedLeadingIconColor = PrimaryColor,
                        unfocusedLeadingIconColor = PrimaryColor
                    ),
                    trailingIcon = {
                        if (isSearching) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                )

                if (sugerencias.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column {
                            sugerencias.forEach { negocio ->
                                Text(
                                    text = negocio.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            lugarTexto = negocio.nombre
                                            selectedNegocioId = negocio.id
                                            sugerencias = emptySet<Negocio>().toList() // emptyList()
                                            sugerencias = emptyList()
                                        }
                                        .padding(12.dp)
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // Fecha Inicio
            DateTimePickerField(
                label = "Fecha y Hora de Inicio (Obligatorio)",
                selectedDateTime = fechaInicio,
                onDateTimeSelected = { fechaInicio = it }
            )

            // Fecha Fin
            DateTimePickerField(
                label = "Fecha y Hora de Finalización (Opcional)",
                selectedDateTime = fechaFin,
                onDateTimeSelected = { fechaFin = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (lugarTexto.isBlank() || fechaInicio == null) {
                            Toast.makeText(context, "Lugar y Fecha de Inicio son obligatorios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (token.isBlank()) {
                            Toast.makeText(context, "Sesión inválida. Por favor, vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            val success = postularPropuesta(
                                token = token,
                                userId = userId,
                                juntadaId = juntadaId,
                                idNegocio = selectedNegocioId,
                                lugarPersonalizado = if (selectedNegocioId == null) lugarTexto else null,
                                fechaInicio = fechaInicio!!,
                                fechaFin = fechaFin
                            )
                            isLoading = false
                            if (success.first) {
                                Toast.makeText(context, success.second, Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Error: ${success.second}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Enviar Propuesta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DateTimePickerField(
    label: String,
    selectedDateTime: LocalDateTime?,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    OutlinedTextField(
        value = selectedDateTime?.format(formatter) ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDateTimePicker(context, selectedDateTime) {
                    onDateTimeSelected(it)
                }
            },
        enabled = false, // Hacemos que no sea editable por teclado
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = PrimaryColor,
            disabledLabelColor = PrimaryColor,
            disabledLeadingIconColor = PrimaryColor
        ),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
    )
}

fun showDateTimePicker(
    context: Context,
    current: LocalDateTime?,
    onSelected: (LocalDateTime) -> Unit
) {
    val calendar = Calendar.getInstance()
    current?.let {
        calendar.set(it.year, it.monthValue - 1, it.dayOfMonth, it.hour, it.minute)
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    onSelected(LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

suspend fun postularPropuesta(
    token: String,
    userId: Int,
    juntadaId: Int,
    idNegocio: Int?,
    lugarPersonalizado: String?,
    fechaInicio: LocalDateTime,
    fechaFin: LocalDateTime?
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val isoFormatter = DateTimeFormatter.ISO_DATE_TIME

        val json = JSONObject().apply {
            if (idNegocio != null) {
                put("id_negocio", idNegocio)
            } else {
                put("lugar_personalizado", lugarPersonalizado)
            }
            put("fecha_hora_inicio", fechaInicio.format(isoFormatter))
            if (fechaFin != null) {
                put("fecha_hora_fin", fechaFin.format(isoFormatter))
            }
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/postular/$userId/$juntadaId")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val respJson = JSONObject(responseBody)
                if (response.isSuccessful) {
                    Pair(true, respJson.optString("message", "Propuesta enviada con éxito"))
                } else {
                    Pair(false, respJson.optString("error", "Error desconocido"))
                }
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Error de conexión")
        }
    }
}