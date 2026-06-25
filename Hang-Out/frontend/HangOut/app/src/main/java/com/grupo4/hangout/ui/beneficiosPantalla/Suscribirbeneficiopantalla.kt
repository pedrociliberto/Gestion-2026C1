package com.grupo4.hangout.ui.beneficiosPantalla

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuscribirBeneficioPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit,
    onSuscripcionExitosa: () -> Unit
) {
    var disponibles by remember { mutableStateOf<List<Beneficio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var beneficioSeleccionado by remember { mutableStateOf<Beneficio?>(null) }
    var mostrarFormularioPago by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        coroutineScope.launch {
            disponibles = fetchBeneficiosDisponibles(userId, token)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Beneficios disponibles",
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
        } else if (disponibles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Ya tenés todos los beneficios disponibles.",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Seleccioná el beneficio que querés contratar:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(disponibles) { beneficio ->
                        val seleccionado = beneficioSeleccionado?.id == beneficio.id
                        OutlinedCard(
                            onClick = { beneficioSeleccionado = beneficio },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (seleccionado) 2.dp else 1.dp,
                                color = if (seleccionado) PrimaryColor else Color.LightGray
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (seleccionado) Color(0xFFE8F5E9) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado,
                                    onClick = { beneficioSeleccionado = beneficio },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(beneficio.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(beneficio.descripcion, fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (beneficioSeleccionado == null) {
                            Toast.makeText(context, "Seleccioná un beneficio primero", Toast.LENGTH_SHORT).show()
                        } else {
                            mostrarFormularioPago = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar al pago", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Volver", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (mostrarFormularioPago && beneficioSeleccionado != null) {
        FormularioPagoDialog(
            beneficio = beneficioSeleccionado!!,
            userId = userId,
            token = token,
            onDismiss = { mostrarFormularioPago = false },
            onSuscripcionExitosa = { mensajeExito ->
                mostrarFormularioPago = false
                Toast.makeText(context, mensajeExito, Toast.LENGTH_LONG).show()
                onSuscripcionExitosa()
            }
        )
    }
}

@Composable
fun FormularioPagoDialog(
    beneficio: Beneficio,
    userId: Int,
    token: String,
    onDismiss: () -> Unit,
    onSuscripcionExitosa: (String) -> Unit
) {
    var numeroTarjeta by remember { mutableStateOf("") }
    var mesVencimiento by remember { mutableStateOf("") }
    var anioVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var nombreTitular by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isProcessingPago by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = { if (!isProcessingPago) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = PrimaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Datos de pago", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryColor)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Beneficio: ${beneficio.nombre}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Número de tarjeta
                OutlinedTextField(
                    value = numeroTarjeta,
                    onValueChange = { nuevo ->
                        val soloDigitos = nuevo.filter { it.isDigit() }.take(16)
                        numeroTarjeta = soloDigitos
                    },
                    enabled = !isProcessingPago,
                    label = { Text("Número de tarjeta") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = TarjetaVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre del titular
                OutlinedTextField(
                    value = nombreTitular,
                    onValueChange = { nombreTitular = it },
                    label = { Text("Nombre del titular") },
                    placeholder = { Text("Como figura en la tarjeta") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vencimiento y CVV en la misma fila
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mesVencimiento,
                        onValueChange = { if (it.length <= 2) mesVencimiento = it.filter { c -> c.isDigit() } },
                        label = { Text("Mes") },
                        placeholder = { Text("MM") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                    OutlinedTextField(
                        value = anioVencimiento,
                        onValueChange = { if (it.length <= 4) anioVencimiento = it.filter { c -> c.isDigit() } },
                        label = { Text("Año") },
                        placeholder = { Text("AAAA") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it.filter { c -> c.isDigit() } },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                }

                // Error de validación local o de validaciones
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg!!, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isProcessingPago) {
                        CircularProgressIndicator(
                            color = PrimaryColor,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mesInt = mesVencimiento.toIntOrNull() ?: 0
                            val anioInt = anioVencimiento.toIntOrNull() ?: 0
                            errorMsg = null
                            isProcessingPago = true
                            coroutineScope.launch {
                                val resultado = suscribirBeneficio(
                                    userId, token, beneficio.id,
                                    numeroTarjeta, mesInt, anioInt,
                                    cvv, nombreTitular
                                )
                                isProcessingPago = false
                                if (resultado.first) {
                                    onSuscripcionExitosa(resultado.second)
                                } else {
                                    errorMsg = resultado.second
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pagar")
                    }
                }
            }
        }
    }
}

suspend fun fetchBeneficiosDisponibles(userId: Int, token: String): List<Beneficio> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/beneficios/$userId/disponibles")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.optJSONArray("disponibles") ?: return@withContext emptyList()
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

suspend fun suscribirBeneficio(
    userId: Int,
    token: String,
    idBeneficio: Int,
    numero: String,
    mes: Int,
    anio: Int,
    cvv: String,
    nombre: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val body = JSONObject().apply {
            put("id_beneficio", idBeneficio)
            put("numero_tarjeta", numero)
            put("mes_vencimiento", mes)
            put("anio_vencimiento", anio)
            put("cvv", cvv)
            put("nombre_titular", nombre)
        }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("${Config.BASE_URL}/beneficios/$userId/suscribir")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val json = if (responseBody.isNotBlank()) JSONObject(responseBody) else JSONObject()
                if (response.isSuccessful) {
                    Pair(true, json.optString("mensaje", "¡Beneficio activado exitosamente!"))
                } else {
                    Pair(false, json.optString("error", "El pago falló. Revisá los datos e intentá nuevamente."))
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}

class TarjetaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(16)
        // Insertar espacio cada 4 dígitos
        val formatted = trimmed.chunked(4).joinToString(" ")

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Por cada 4 dígitos, hay un espacio extra
                val espacios = offset / 4
                return (offset + espacios).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Restar los espacios que hay antes de esta posición
                val espacios = offset / 5
                return (offset - espacios).coerceAtMost(trimmed.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}