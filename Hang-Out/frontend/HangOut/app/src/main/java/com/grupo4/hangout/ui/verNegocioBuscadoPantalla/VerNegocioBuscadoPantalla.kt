package com.grupo4.hangout.ui.verNegocioBuscadoPantalla

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.R
import com.grupo4.hangout.model.Descuento
import com.grupo4.hangout.model.Negocio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.grupo4.hangout.ui.resenias.*
import com.grupo4.hangout.model.Resenia
import com.grupo4.hangout.ui.descuentosPantalla.obtenerDescuentosRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerNegocioBuscadoPantalla(
    negocio: Negocio,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var listaImagenes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingImages by remember { mutableStateOf(true) }
    var listaResenias by remember { mutableStateOf<List<Resenia>>(emptyList()) }
    var isLoadingResenias by remember { mutableStateOf(true) }
    var descuentosNegocio by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(negocio.id) {
        coroutineScope.launch {
            listaImagenes = fetchImagenesNegocio(negocio.id)
            isLoadingImages = false
        }
        coroutineScope.launch {
            listaResenias = fetchReseniasNegocio(negocio.id)
            isLoadingResenias = false
        }
        coroutineScope.launch {
            descuentosNegocio = obtenerDescuentosRequest(negocio.id, "")
        }
    }

    // Modal para ver imagen ampliada con carrusel y zoom funcional
    if (selectedImageIndex != null) {
        FullScreenImageModal(
            images = listaImagenes,
            initialIndex = selectedImageIndex!!,
            onDismiss = { selectedImageIndex = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Lugar") },
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
                    .height(250.dp)
            ) {
                if (!isLoadingImages && listaImagenes.isEmpty()) {
                    // Fondo por defecto si no hay imágenes
                    Image(
                        painter = painterResource(id = R.drawable.fondo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!isLoadingImages) {
                    // Carrusel de imágenes asociadas
                    val pagerState = rememberPagerState(pageCount = { listaImagenes.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = listaImagenes[page],
                            contentDescription = "Imagen de ${negocio.nombre}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedImageIndex = page },
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Indicador de páginas (dots)
                    if (listaImagenes.size > 1) {
                        Row(
                            Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(listaImagenes.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Placeholder mientras carga
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }

                // Gradiente sobre la imagen para mejorar legibilidad del título
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = negocio.nombre.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (!isLoadingResenias && listaResenias.isNotEmpty()) {
                        val promedio = listaResenias.map { it.valoracion }.average()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            repeat(5) { index ->
                                val active = index < promedio.toInt()
                                Icon(
                                    imageVector = if (active) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (active) Color(0xFFFFB800) else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%.1f", promedio),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Cuerpo de la pantalla
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Información General",
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val ubicacionTexto = negocio.ubicacion.ifBlank { "Sin ubicación registrada" }
                InfoRowNegocio(icon = Icons.Default.LocationOn, text = ubicacionTexto)

                if (negocio.URLUbicacion.isNotBlank() && negocio.URLUbicacion != null) {
                    BotonAbrirMapa(url = negocio.URLUbicacion)
                } else{
                    val uriAscii = Uri.encode(negocio.ubicacion)
                    BotonAbrirMapa(url = "https://www.google.com/maps/search/?api=1&query=${uriAscii}")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "El dueño de este negocio no especifico la ubicación exacta de su negocio. Puede que la ubicación mostrada no sea correcta",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                if (negocio.sitioWeb.isNotBlank()) {
                    InfoRowNegocio(
                        icon = Icons.Default.Info,
                        text = negocio.sitioWeb,
                        textColor = Color(0xFF1D4ED8)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                Text(
                    text = "Sobre el lugar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = negocio.descripcion.ifBlank { "Sin descripción disponible." },
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                Text(
                    text = "Horarios de Atención",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val horariosFormateados = formatearHorariosBackend(negocio.horarios)
                Text(
                    text = horariosFormateados,
                    fontSize = 15.sp,
                    color = Color(0xFF334155),
                    lineHeight = 26.sp
                )

                if (negocio.filtros.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                    Text(
                        text = "Características",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FiltrosFlow(filtros = negocio.filtros.map { it.nombre })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                if (descuentosNegocio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Descuentos Disponibles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    descuentosNegocio.forEach { desc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Fondo verde muy claro y sutil
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = desc.descripcion,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2E7D32)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Código: ",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = desc.codigo,
                                            color = PrimaryColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                val valorBeneficio = when {
                                    desc.porcentaje != null -> "${desc.porcentaje}% OFF"
                                    desc.monto != null -> "$${desc.monto.toInt()}"
                                    else -> "Promo"
                                }

                                Surface(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = valorBeneficio,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "El negocio no tiene descuentos disponibles en este momento.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reseñas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isLoadingResenias && listaResenias.isNotEmpty()) {
                        val promedio = listaResenias.map { it.valoracion }.average()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", promedio),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = " (${listaResenias.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingResenias) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else if (listaResenias.isEmpty()) {
                    Text(
                        text = "Aún no hay reseñas para este lugar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    listaResenias.forEach { resenia ->
                        ReseniaItem(resenia = resenia)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageModal(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                ZoomableImage(imageUrl = images[page], onDismiss = onDismiss)
            }
            
            // Botón de cierre
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }

            // Indicador de página en modal
            if (images.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        // Si estamos con zoom mayor a 1, consumimos todos los gestos para manejar paneo y zoom local
                        if (scale > 1f) {
                            event.changes.forEach { if (it.positionChanged()) it.consume() }

                            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                            scale = newScale
                            offset += panChange

                            // Si el usuario aleja hasta volver al tamaño original, reseteamos el offset
                            if (scale <= 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        } else {
                            // Si estamos en escala normal (1f), solo consumimos si se inicia un pinch (zoom)
                            // Si es un simple deslizamiento lateral (panChange.x != 0), NO lo consumimos
                            // para permitir que el HorizontalPager cambie de página.
                            if (zoomChange != 1f) {
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                                scale = (scale * zoomChange).coerceIn(1f, 5f)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    },
                    onTap = { onDismiss() }
                )
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun InfoRowNegocio(icon: ImageVector, text: String, textColor: Color = Color.Unspecified) {
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
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosFlow(filtros: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        maxItemsInEachRow = 3
    ) {
        filtros.forEach { nombreFiltro ->
            FiltroChipCustom(nombre = nombreFiltro)
        }
    }
}

@Composable
fun FiltroChipCustom(nombre: String) {
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
                    text = nombre.first().toString().uppercase(),
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

private fun formatearHorariosBackend(horariosRaw: String): String {
    if (horariosRaw.isBlank()) return "Sin horarios disponibles."

    return horariosRaw.split(",")
        .mapNotNull { bloque ->
            val item = bloque.trim()
            if (item.length >= 2) {
                val siglaDia = item.substring(0, 2)
                val rangoHoras = item.substring(2)

                val nombreDia = when (siglaDia) {
                    "LU" -> "Lunes"
                    "MA" -> "Martes"
                    "MI" -> "Miércoles"
                    "JU" -> "Jueves"
                    "VI" -> "Viernes"
                    "SA" -> "Sábado"
                    "DO" -> "Domingo"
                    "FE" -> "Feriados"
                    else -> siglaDia
                }

                if (rangoHoras.contains("-")) {
                    val horas = rangoHoras.split("-")
                    val desde = horas.getOrNull(0) ?: ""
                    val hasta = horas.getOrNull(1) ?: ""
                    "• $nombreDia: $desde-$hasta"
                } else {
                    "• $nombreDia: $rangoHoras"
                }
            } else null
        }
        .joinToString("\n")
}

private suspend fun fetchImagenesNegocio(negocioId: Int): List<String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/imagenes/$negocioId")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.getJSONArray("imagenes")
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rawUrl = obj.getString("url")
                    val fullUrl = if (rawUrl.startsWith("http")) rawUrl
                    else "${Config.BASE_URL}${if (rawUrl.startsWith("/")) "" else "/"}$rawUrl"
                    list.add(fullUrl)
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}


