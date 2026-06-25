package com.grupo4.hangout.ui.detalleJuntadaPantalla

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.R
import com.grupo4.hangout.model.Juntada
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.model.Propuesta
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ResultadoCierreJuntada(
    val exito: Boolean,
    val codigoAlertaHorario: String,
    val mensaje: String,
    val idGanadora: Int?
)

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun DetalleJuntadaPantalla(
    juntadaId: Int,
    userId: Int,
    onBack: () -> Unit,
    onNavigateToCrearPropuesta: (Int, Int) -> Unit,
    onNavigateToCrearResenia: (Int, Int, String) -> Unit
) {

    var juntada by remember { mutableStateOf<Juntada?>(null) }
    var propuestas by remember { mutableStateOf<List<Propuesta>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var estadoJuntadaActual by remember { mutableStateOf("PENDIENTE") }
    var mostrarAlertaHorario by remember { mutableStateOf(false) }
    var mensajeAlertaHorario by remember { mutableStateOf("") }
    var idPropuestaGanadoraActual by remember { mutableStateOf<Int?>(null) }
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
    val token = sharedPrefs.getString("token", "") ?: ""
    val votacionAbierta = estadoJuntadaActual == "PENDIENTE"

    LaunchedEffect(juntadaId) {
        val datosCargados = fetchJuntada(juntadaId, userId)
        val propuestasCargadas = fetchPropuestas(token, userId, juntadaId)
        if (datosCargados != null) {
            juntada = datosCargados
            estadoJuntadaActual = datosCargados.estado
            idPropuestaGanadoraActual = datosCargados.idPropuestaGanadora
        }
        propuestas = propuestasCargadas
        isLoading = false
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Juntada") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryColor,
                    navigationIconContentColor = PrimaryColor,
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (juntada == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontró la juntada")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.fondo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryColor.copy(alpha = 0.4f),
                                        PrimaryColor.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = juntada!!.titulo.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(
                            when (estadoJuntadaActual) {
                                "CONFIRMADA" -> "Confirmada"
                                "PASADA" -> "Pasada"
                                else -> "Pendiente"
                            }
                        )
                    }
                }

                val propuestaGanadora = if (estadoJuntadaActual == "CONFIRMADA" || estadoJuntadaActual == "PASADA") {
                    propuestas.find { it.id == idPropuestaGanadoraActual }
                } else {
                    null
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Información General",
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val textoFecha = if (propuestaGanadora != null) {
                        val inicio = formatearFechaHoraEspaniol(propuestaGanadora.fechaHoraInicio)
                        val fin = propuestaGanadora.fechaHoraFin?.let { formatearFechaHoraEspaniol(it) }
                        if (fin != null) "Desde: $inicio\nHasta: $fin" else inicio
                    } else {
                        "A confirmar (votación abierta)"
                    }

                    val textoLugar = if (propuestaGanadora != null) {
                        propuestaGanadora.nombreNegocio ?: propuestaGanadora.lugarPersonalizado ?: "Lugar seleccionado"
                    } else {
                        "Punto de encuentro a confirmar (votación abierta)"
                    }

                    InfoRow(icon = Icons.Default.DateRange, text = textoFecha)
                    InfoRow(icon = Icons.Default.LocationOn, text = textoLugar)
                    InfoRow(icon = Icons.Default.Person, text = "Organiza: ${juntada!!.organizador}")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                    val invitados = juntada!!.participantes

                    Text(
                        text = "Participantes (${invitados.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ParticipantsFlow(invitados = invitados)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

                    if (estadoJuntadaActual == "PENDIENTE") {
                        Button(
                            onClick = { onNavigateToCrearPropuesta(userId, juntadaId) },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crear nueva Propuesta",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (juntada?.rol == "Creador" && estadoJuntadaActual != "PASADA") {
                        if (estadoJuntadaActual == "PENDIENTE") {
                            Button(
                                onClick = {
                                    juntada?.let { juntadaValida ->
                                        coroutineScope.launch {
                                            val resultadoCierre = cerrarVotacion(juntadaValida.id, token)
                                            if (resultadoCierre.exito) {
                                                estadoJuntadaActual = "CONFIRMADA"
                                                idPropuestaGanadoraActual = resultadoCierre.idGanadora
                                                Toast.makeText(context, "Votación cerrada con éxito", Toast.LENGTH_SHORT).show()
                                                when (resultadoCierre.codigoAlertaHorario) {
                                                    "DIA_CERRADO" -> {
                                                        mensajeAlertaHorario = "¡Atención! La propuesta ganadora corresponde a un negocio que se encuentra cerrado el DÍA de la juntada. Evaluá reabrir la votación."
                                                        mostrarAlertaHorario = true
                                                    }
                                                    "FUERA_HORARIO" -> {
                                                        mensajeAlertaHorario = "¡Atención! La juntada se programó completamente FUERA DEL HORARIO de atención del negocio para ese día. Evaluá reabrir la votación."
                                                        mostrarAlertaHorario = true
                                                    }
                                                    "ARRANCA_ANTES" -> {
                                                        mensajeAlertaHorario = "¡Atención! La juntada ARRANCA ANTES de que el negocio abra sus puertas. Es posible que tengan que esperar afuera. Evaluá reabrir la votación."
                                                        mostrarAlertaHorario = true
                                                    }
                                                    "TERMINA_DESPUES" -> {
                                                        mensajeAlertaHorario = "¡Atención! La juntada TERMINA LUEGO del horario de cierre del negocio. Tengan en cuenta que el lugar cerrará antes de finalizar su reunión. Evaluá reabrir la votación."
                                                        mostrarAlertaHorario = true
                                                    }
                                                    "MUCHOS_DIAS" -> {
                                                        mensajeAlertaHorario = "¡Atención! La juntada quiere realizarse VARIOS DÍAS SEGUIDOS. Tengan en cuenta que el lugar podría cerrar en el medio, o no estar disponible en alguno de esos días. Evaluá reabrir la votación."
                                                        mostrarAlertaHorario = true
                                                    }
                                                    "OK" -> {
                                                        mostrarAlertaHorario = false
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, resultadoCierre.mensaje, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), // Rojo Cierre
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("Cerrar Votación y Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else if (estadoJuntadaActual == "CONFIRMADA") {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        juntada?.let { juntadaValida ->
                                            val (exito, mensaje) = abrirVotacion(juntadaValida.id, token)
                                            if (exito) {
                                                estadoJuntadaActual = "PENDIENTE"
                                                Toast.makeText(context, "La votación ha sido reabierta", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reabrir Votación", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Propuestas de Planes (${propuestas.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (propuestas.isEmpty()) {
                        Text(
                            text = "Aún no hay propuestas de lugares. ¡Creá tu primera propuesta!",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        propuestas.forEach { propuesta ->
                            PropuestaItemCard(
                                propuesta = propuesta,
                                votacionAbierta = votacionAbierta,
                                currentUserId = userId,
                                esGanadora = (propuesta.id == idPropuestaGanadoraActual && estadoJuntadaActual == "CONFIRMADA"),
                                onDeleteClick = { propuestaId ->
                                    isLoading = true
                                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                                        val exito = deletePropuesta(token, userId, propuestaId)
                                        if (exito) {
                                            propuestas = fetchPropuestas(token, userId, juntadaId)
                                        }
                                        isLoading = false
                                    }
                                },
                                onVoteClick = {propuestaId, nuevoEstadoVotacion ->
                                    // Actualizo mi votación en esa propuesta
                                    for (propuesta in propuestas){
                                        if (propuesta.id == propuestaId){
                                            propuesta.yoVote = nuevoEstadoVotacion
                                        }
                                    }

                                    coroutineScope.launch {
                                        val resultadoVotacion = actualizarVotaciones(propuestas,userId, juntadaId, token)
                                        val seActualizo = resultadoVotacion.first
                                        val mensajeRespuesta = resultadoVotacion.second

                                        if(!seActualizo){
                                            Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show()
                                        } else{
                                            propuestas = fetchPropuestas(token, userId, juntadaId)
                                        }
                                    }

                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (estadoJuntadaActual != "PASADA") {
                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedButton(
                            onClick = { mostrarConfirmacionSalir = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Salir de la Juntada",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (juntada?.rol == "Creador" && estadoJuntadaActual != "PASADA") {
                        Button(
                            onClick = { mostrarConfirmacionEliminar = true },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Eliminar Juntada", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (mostrarAlertaHorario) {
                    AlertDialog(
                        onDismissRequest = { mostrarAlertaHorario = false },
                        title = {
                            Text(
                                text = "⚠️ Alerta de Horario",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        },
                        text = { Text(text = mensajeAlertaHorario) },
                        confirmButton = {
                            TextButton(onClick = { mostrarAlertaHorario = false }) {
                                Text("Entendido", fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            }
                        }
                    )
                }

                if (mostrarConfirmacionSalir) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmacionSalir = false },
                        title = { Text("¿Salir de la juntada?", fontWeight = FontWeight.Bold) },
                        text = { Text("¿Estás seguro de que querés salir de esta juntada? Esta acción no se puede deshacer fácilmente.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarConfirmacionSalir = false
                                    coroutineScope.launch {
                                        val (exito, mensaje) = salirDeJuntada(userId, juntadaId, token)
                                        if (exito) {
                                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("Salir", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmacionSalir = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (mostrarConfirmacionEliminar) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmacionEliminar = false },
                        title = { Text("¿Eliminar juntada?", fontWeight = FontWeight.Bold) },
                        text = { Text("¿Estás seguro de que querés eliminar esta juntada? Esta acción borrará todas las propuestas y votos, y no se puede deshacer.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarConfirmacionEliminar = false
                                    coroutineScope.launch {
                                        val (exito, mensaje) = eliminarJuntada(juntadaId, token)
                                        if (exito) {
                                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("Eliminar", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (estadoJuntadaActual == "PASADA" && propuestaGanadora != null && propuestaGanadora.idNegocio != null) {
                    val nombreNegocio = propuestaGanadora.nombreNegocio ?: propuestaGanadora.lugarPersonalizado ?: ""
                    val idNegocio = propuestaGanadora.idNegocio ?: 0
                    Button(
                        onClick = { onNavigateToCrearResenia(juntadaId, idNegocio, nombreNegocio) },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Reseñar lugar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParticipantsFlow(invitados: List<String>) {
    if (invitados.isEmpty()) {
        Text("No hay participantes aún", color = Color.Gray)
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            maxItemsInEachRow = 4
        ) {
            invitados.forEach { nombre ->
                ParticipantChip(nombre = nombre)
            }
        }
    }
}

@Composable
fun ParticipantChip(nombre: String) {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(PrimaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    nombre.first().toString().uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}


@Composable
fun StatusBadge(estado: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = estado,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PropuestaItemCard(
    propuesta: Propuesta,
    votacionAbierta: Boolean,
    currentUserId: Int,
    esGanadora: Boolean,
    onDeleteClick: (Int) -> Unit,
    onVoteClick: (Int, Boolean) -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val esMia = propuesta.idUsuario == currentUserId

    val nombreLugar = propuesta.nombreNegocio ?: propuesta.lugarPersonalizado ?: "Lugar desconocido"
    val nombrePostulante = propuesta.nombreUsuario
    val esNegocio = propuesta.idNegocio != null
    val backgroundColor = if (esGanadora) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esGanadora) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (esNegocio) PrimaryColor else Color(0xFFE65100),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nombreLugar,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f) // Evita que pise al badge si el nombre es largo
                )

                Surface(
                    color = if (esNegocio) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (esNegocio) "Negocio" else "Lugar Personalizado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (esNegocio) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }

                Surface(
                    color = Color(0xFFEBEFF5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (propuesta.cantidadVotos == 1) "1 voto" else "${propuesta.cantidadVotos} votos",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }
            }



            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (esMia) "Propuesto por mí" else "Propuesto por: $nombrePostulante",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            val inicioFormateado = formatearFechaHoraEspaniol(propuesta.fechaHoraInicio)
            val finFormateado = propuesta.fechaHoraFin?.let { formatearFechaHoraEspaniol(it) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp)
                ) {
                    Text(
                        text = "Desde: $inicioFormateado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )

                    if (finFormateado != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hasta: $finFormateado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }

                if (esMia && votacionAbierta) {
                    IconButton(
                        onClick = { showConfirmationDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar propuesta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Surface(
            onClick = {
                val nuevoEstado = !propuesta.yoVote
                onVoteClick(propuesta.id, nuevoEstado)
            },
            enabled = votacionAbierta,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (votacionAbierta) Color(229, 231, 235) else Color(243, 244, 246),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val contenidoTint = if (votacionAbierta) Color(30, 41, 57) else Color(156, 163, 175)
                Icon(
                    imageVector = if (propuesta.yoVote) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = null,
                    tint = contenidoTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (propuesta.yoVote) "Votado" else "Votar",
                    color = contenidoTint,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "¿Eliminar propuesta?")
            },
            text = {
                Text(text = "Esta acción quitará la propuesta para '$nombreLugar' de la lista de propuestas de la juntada. No se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        onDeleteClick(propuesta.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

suspend fun fetchJuntada(juntadaId: Int, idUsuarioAcual: Int): Juntada? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/juntada/$juntadaId")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val bodyString = response.body?.string() ?: ""
                val obj = JSONObject(bodyString)

                println("DEBUG: Cuerpo recibido: $bodyString")

                val jsonArray = obj.optJSONArray("participantes")
                val listaParticipantes = mutableListOf<String>()
                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        listaParticipantes.add(jsonArray.getString(i))
                    }
                }

                val idOrganizador = obj.getInt("id_organizador")
                val rolCalculado = if (idOrganizador == idUsuarioAcual) "Creador" else "Invitado"
                val estadoReal = obj.optString("estado", "PENDIENTE")
                val idPropGanadora = if (obj.isNull("propuesta_ganadora")) null else obj.optInt("propuesta_ganadora")
                Juntada(
                    id = juntadaId,
                    titulo = obj.optString("titulo"),
                    codigo = obj.optString("codigo"),
                    rol = rolCalculado,
                    idOrganizador = idOrganizador,
                    organizador = obj.optString("organizador"),
                    participantes = listaParticipantes,
                    estado = estadoReal,
                    idPropuestaGanadora = idPropGanadora
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun fetchPropuestas(token: String, userId: Int, juntadaId: Int): List<Propuesta> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/postular/$userId/$juntadaId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()

                val bodyString = response.body?.string() ?: ""
                val obj = JSONObject(bodyString)
                val jsonArray = obj.optJSONArray("propuestas") ?: return@withContext emptyList()

                val listaPropuestas = mutableListOf<Propuesta>()
                for (i in 0 until jsonArray.length()) {
                    val pObj = jsonArray.getJSONObject(i)

                    listaPropuestas.add(
                        Propuesta(
                            id = pObj.optInt("id"),
                            idUsuario = pObj.optInt("id_usuario"),
                            idNegocio = if (pObj.isNull("id_negocio")) null else pObj.optInt("id_negocio"),
                            nombreNegocio = if (pObj.isNull("nombre_negocio")) null else pObj.optString("nombre_negocio"),
                            nombreUsuario = pObj.optString("nombre_usuario"),
                            lugarPersonalizado = if (pObj.isNull("lugar_personalizado")) null else pObj.optString("lugar_personalizado"),
                            fechaHoraInicio = pObj.optString("fecha_hora_inicio"),
                            fechaHoraFin = if (pObj.isNull("fecha_hora_fin")) null else pObj.optString("fecha_hora_fin"),
                            cantidadVotos = pObj.getInt("cantidad_votos"),
                            yoVote = pObj.getBoolean("yo_vote")
                        )
                    )
                }
                listaPropuestas
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun deletePropuesta(token: String, userId: Int, propuestaId: Int): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/eliminar/$userId/$propuestaId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * Convierte un formato ISO estándar (ej: "2026-05-19T18:30:00")
 * a un string amigable en español: "19 de mayo de 2026, 18:30"
 */
fun formatearFechaHoraEspaniol(isoString: String): String {
    return try {
        val limpiado = isoString.replace("T", " ")
        val partes = limpiado.split(" ")
        val parteFecha = partes[0] // "2026-05-19"
        val parteHora = partes[1]  // "18:30:00"

        val componentesFecha = parteFecha.split("-")
        val anio = componentesFecha[0]
        val mesNum = componentesFecha[1]
        val dia = componentesFecha[2].toInt().toString()

        val componentesHora = parteHora.split(":")
        val horaMinutos = "${componentesHora[0]}:${componentesHora[1]}"

        val nombreMes = when (mesNum) {
            "01" -> "enero"
            "02" -> "febrero"
            "03" -> "marzo"
            "04" -> "abril"
            "05" -> "mayo"
            "06" -> "junio"
            "07" -> "julio"
            "08" -> "agosto"
            "09" -> "septiembre"
            "10" -> "octubre"
            "11" -> "noviembre"
            "12" -> "diciembre"
            else -> mesNum
        }

        "$dia de $nombreMes de $anio, $horaMinutos"
    } catch (_: Exception) {
        isoString.replace("T", " ").substringBeforeLast(":")
    }
}

suspend fun actualizarVotaciones(
    propuestas: List<Propuesta>,
    idUsuario: Int,
    idJuntada: Int,
    token: String
): Pair<Boolean, String>{
    return withContext(Dispatchers.IO){
        // Creo un JSON con el formato {idPropuesta: yoVote}
        val diccPropuestas = mutableMapOf<String, Boolean>()
        for (propuesta in propuestas){
            diccPropuestas[propuesta.id.toString()] = propuesta.yoVote
        }

        try {
            val jsonPropuestas = JSONObject(diccPropuestas)
            val body = jsonPropuestas.toString().toRequestBody()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${Config.BASE_URL}/votacion/$idUsuario/$idJuntada")
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val respJson = JSONObject(responseBody)

                if (!response.isSuccessful){
                    val mensajeError = respJson.getString("error")
                    Pair(false, mensajeError)
                } else{
                    val mensaje = respJson.getString("mensaje")
                    Pair(true, mensaje)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Ocurrio un error con el servidor")
        }
    }
}

suspend fun cerrarVotacion(
    idJuntada: Int,
    token: String
): ResultadoCierreJuntada {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val body = okhttp3.internal.EMPTY_REQUEST
            val request = Request.Builder()
                .url("${Config.BASE_URL}/juntada/$idJuntada/cerrar")
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (responseBody.isBlank()) return@withContext ResultadoCierreJuntada(
                    exito = false,
                    codigoAlertaHorario = "",
                    mensaje = "Error de respuesta del servidor",
                    idGanadora = null
                )

                val respJson = JSONObject(responseBody)

                if (!response.isSuccessful) {
                    val mensajeError = respJson.optString("error", "Ocurrió un error al cerrar las votaciones de la juntada.")
                    ResultadoCierreJuntada(
                        exito = false,
                        codigoAlertaHorario = "",
                        mensaje = mensajeError,
                        idGanadora = null
                    )
                } else {
                    val alertaNegocioCerrado = respJson.optBoolean("alerta_negocio_cerrado", false)
                    val mensajeJson = respJson.optString("mensaje", "Votación cerrada")
                    val idGanadora = if (respJson.isNull("propuesta_ganadora")) null else respJson.optInt("propuesta_ganadora")
                    val codigoAlerta = respJson.optString("codigo_alerta_horario", "OK")
                    ResultadoCierreJuntada(true, codigoAlerta, mensajeJson, idGanadora)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoCierreJuntada(
                exito = false,
                codigoAlertaHorario = "",
                mensaje = "Ocurrió un error con el servidor",
                idGanadora = null
            )
        }
    }
}

suspend fun abrirVotacion(
    idJuntada: Int,
    token: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val body = okhttp3.internal.EMPTY_REQUEST
            val request = Request.Builder()
                .url("${Config.BASE_URL}/juntada/$idJuntada/abrir")
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val respJson = org.json.JSONObject(responseBody)

                if (!response.isSuccessful) {
                    val mensajeError = respJson.optString("error", "Ocurrió un error al reabrir las votaciones de la juntada.")
                    Pair(false, mensajeError)
                } else {
                    val mensajeJson = respJson.optString("mensaje", "Votación reabierta.")
                    Pair(true, mensajeJson)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Ocurrió un error con el servidor")
        }
    }
}

suspend fun salirDeJuntada(
    idUsuario: Int,
    idJuntada: Int,
    token: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = JSONObject()
            json.put("id_usuario", idUsuario)
            json.put("id_juntada", idJuntada)
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${Config.BASE_URL}/juntada/salir")
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (responseBody.isBlank()) return@withContext Pair(false, "Error de respuesta del servidor")

                val respJson = JSONObject(responseBody)
                if (response.isSuccessful) {
                    val mensaje = respJson.optString("mensaje", "Has salido de la juntada")
                    Pair(true, mensaje)
                } else {
                    val error = respJson.optString("error", "Ocurrió un error al salir de la juntada")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Ocurrió un error con el servidor")
        }
    }
}

suspend fun eliminarJuntada(
    idJuntada: Int,
    token: String
): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${Config.BASE_URL}/juntada/$idJuntada")
                .delete()
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (responseBody.isBlank()) return@withContext Pair(false, "Error de respuesta del servidor")

                val respJson = JSONObject(responseBody)
                if (response.isSuccessful) {
                    val mensaje = respJson.optString("mensaje", "Juntada eliminada con éxito")
                    Pair(true, mensaje)
                } else {
                    val error = respJson.optString("error", "Ocurrió un error al eliminar la juntada")
                    Pair(false, error)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Ocurrió un error con el servidor")
        }
    }
}
