package com.grupo4.hangout.ui.descuentosPantalla

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Descuento
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutBottomNavigation
import com.grupo4.hangout.ui.panelJuntadasPantalla.HangoutTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun DescuentosPantalla(
    userId: Int,
    token: String,
    onNavigateToEstadisticas: () -> Unit,
    onNavigateToPerfilNegocio: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var porcentaje by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var codigoCustom by remember { mutableStateOf("") }

    var listaDescuentos by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun cargarLista() {
        isLoading = true
        coroutineScope.launch {
            listaDescuentos = obtenerDescuentosRequest(userId, token)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarLista()
    }

    Scaffold(
        topBar = {
            HangoutTopBar(
                onProfileClick = onNavigateToPerfilNegocio,
                onCerrarSesionClick = onCerrarSesionClick,
                esPersonal = false,
            )
        },
        bottomBar = {
            HangoutBottomNavigation(
                onProfileClick = onNavigateToPerfilNegocio,
                onNavigateToPlanificador = {},
                onNavigateToDescuentos = {},
                onNavigateToJuntadas = onNavigateToEstadisticas,
                pantallaActual = "Descuentos",
                esPersonal = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sección superior: Formulario de Creación
            if (listaDescuentos.size < 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nuevo Descuento",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = PrimaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${listaDescuentos.size}/3 Activos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción del beneficio") },
                            placeholder = { Text("Ej: 2x1 en hamburguesas") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                focusedLabelColor = PrimaryColor,
                                cursorColor = PrimaryColor
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = porcentaje,
                                onValueChange = { porcentaje = it },
                                label = { Text("Porcentaje", fontSize = 15.sp) },
                                placeholder = { Text("Ej: 20", fontSize = 15.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Percent,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                    )
                                },
                                suffix = { Text("%", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    cursorColor = PrimaryColor
                                )
                            )

                            OutlinedTextField(
                                value = monto,
                                onValueChange = { monto = it },
                                label = { Text("Monto fijo", fontSize = 15.sp) },
                                placeholder = { Text("Ej: 1500", fontSize = 15.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                    )
                                },
                                suffix = { Text("$", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    cursorColor = PrimaryColor
                                )
                            )
                        }

                        OutlinedTextField(
                            value = codigoCustom,
                            onValueChange = { codigoCustom = it },
                            label = { Text("Código de cupón (Opcional)") },
                            placeholder = { Text("Ej: Abc123") },
                            leadingIcon = { Icon(Icons.Default.LocalActivity, contentDescription = null, tint = PrimaryColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                focusedLabelColor = PrimaryColor,
                                cursorColor = PrimaryColor
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    val respuesta = crearDescuentoRequest(descripcion, porcentaje, monto, codigoCustom, userId, token)
                                    isLoading = false

                                    Toast.makeText(context, respuesta.second, Toast.LENGTH_LONG).show()

                                    if (respuesta.first) {
                                        descripcion = ""
                                        porcentaje = ""
                                        monto = ""
                                        codigoCustom = ""
                                        cargarLista()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Confirmar y Crear", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = "Alcanzaste el límite máximo de beneficios activos (3/3). Elimina uno de la lista inferior para poder habilitar la creación de uno nuevo.",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sección inferior: Listado de Descuentos Activos
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tus Descuentos Activos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else if (listaDescuentos.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Aún no has creado ningún descuento promocional.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(modifier = Modifier.heightIn(max = 350.dp)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(listaDescuentos) { desc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = desc.descripcion,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text("Código: ", fontSize = 13.sp, color = Color.Gray)
                                                Text(
                                                    text = desc.codigo,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = PrimaryColor,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        val badgeTexto = when {
                                            desc.porcentaje != null -> "${desc.porcentaje}% OFF"
                                            desc.monto != null -> "$${desc.monto.toInt()}"
                                            else -> "Promo"
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFF4CAF50),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = badgeTexto,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }

                                            IconButton(onClick = {
                                                isLoading = true
                                                coroutineScope.launch {
                                                    val exito = eliminarDescuentoRequest(desc.id, userId, token)
                                                    isLoading = false
                                                    if (exito) {
                                                        Toast.makeText(context, "Se eliminó el descuento.", Toast.LENGTH_SHORT).show()
                                                        cargarLista()
                                                    } else {
                                                        Toast.makeText(context, "Error al eliminar el descuento.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Borrar",
                                                    tint = Color(0xFFD32F2F)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Peticiones de Red utilizando OkHttpClient
// ==========================================

suspend fun obtenerDescuentosRequest(userId: Int, token: String): List<Descuento> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/$userId/descuentos")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val array = json.getJSONArray("descuentos")
                val list = mutableListOf<Descuento>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Descuento(
                            id = obj.getInt("id"),
                            idNegocio = obj.getInt("id_negocio"),
                            descripcion = obj.getString("descripcion"),
                            porcentaje = if (obj.isNull("porcentaje")) null else obj.getInt("porcentaje"),
                            monto = if (obj.isNull("monto")) null else obj.getDouble("monto"),
                            codigo = obj.getString("codigo")
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

suspend fun crearDescuentoRequest(
    titulo: String,
    porcentaje: String,
    monto: String,
    codigo: String,
    userId: Int,
    token: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("descripcion", titulo)
            put("id_negocio", userId)
            if (porcentaje.isNotBlank()) put("porcentaje", porcentaje.toInt())
            if (monto.isNotBlank()) put("monto", monto.toDouble())
            if (codigo.isNotBlank()) put("codigo", codigo)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/$userId/descuentos")
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val respJson = JSONObject(responseBody)
                if (response.isSuccessful) {
                    val descObj = respJson.getJSONObject("descuento")
                    val codigoFinal = descObj.getString("codigo")
                    Pair(true, "¡Descuento creado correctamente!\nCódigo del cupón: $codigoFinal")
                } else {
                    val error = respJson.optString("error", "Error al procesar la solicitud")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión con el servidor")
        }
    }
}

suspend fun eliminarDescuentoRequest(descuentoId: Int, userId: Int, token: String): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/$userId/descuentos/$descuentoId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}