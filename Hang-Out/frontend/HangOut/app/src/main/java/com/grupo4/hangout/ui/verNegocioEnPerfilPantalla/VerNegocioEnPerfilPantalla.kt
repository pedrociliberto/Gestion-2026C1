package com.grupo4.hangout.ui.verNegocioEnPerfilPantalla

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.ui.modificarNegocioPantalla.diasSemana
import com.grupo4.hangout.ui.modificarNegocioPantalla.fetchNegocio
import com.grupo4.hangout.ui.modificarNegocioPantalla.parseHorarios
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Star

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VerNegocioEnPerfilPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit,
    onNavigateToModificar: () -> Unit,
    onNavigateToBeneficios: () -> Unit
) {
    var negocio by remember { mutableStateOf<Negocio?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            negocio = fetchNegocio(userId, token)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Negocio",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToModificar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(
                            "Modificar información",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onNavigateToBeneficios,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gestionar beneficios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (negocio == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Todavía no hay información cargada. " +
                                    "Tocá \"Modificar información\" para completar los datos de tu negocio.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        "Información actual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryColor
                    )
                }

                SeccionVer(titulo = "Nombre") {
                    val valor = negocio?.nombre?.takeIf { it.isNotBlank() }
                    if (valor != null) {
                        Text(valor, fontSize = 16.sp)
                    } else {
                        TextoVacio("Sin nombre")
                    }
                }

                SeccionVer(titulo = "Descripción") {
                    val valor = negocio?.descripcion?.takeIf { it.isNotBlank() }
                    if (valor != null) {
                        Text(valor, fontSize = 16.sp)
                    } else {
                        TextoVacio("Sin descripción")
                    }
                }

                SeccionVer(titulo = "Ubicación") {
                    val valor = negocio?.ubicacion?.takeIf { it.isNotBlank() }
                    if (valor != null) {
                        Text(valor, fontSize = 16.sp)
                    } else {
                        TextoVacio("Sin ubicación")
                    }
                }

                SeccionVer(titulo = "URL Ubicación") {
                    val valor = negocio?.URLUbicacion?.takeIf { it.isNotBlank() }
                    if (valor != null) {
                        Text(valor, fontSize = 16.sp)
                    } else {
                        TextoVacio("Sin URL de ubicación")
                    }
                }

                SeccionVer(titulo = "Horarios") {
                    val horarios = negocio?.horarios?.let { parseHorarios(it) } ?: emptyList()
                    if (horarios.isEmpty()) {
                        TextoVacio("Sin horarios cargados")
                    } else {
                        horarios.forEach { h ->
                            val dia = diasSemana.find { it.first == h.dia }?.second ?: h.dia
                            val desde = String.format("%02d:%02d", h.desdeHora, h.desdeMin)
                            val hasta = String.format("%02d:%02d", h.hastaHora, h.hastaMin)
                            Text("$dia: $desde a $hasta", fontSize = 15.sp)
                        }
                    }
                }

                SeccionVer(titulo = "Sitio web / Redes sociales") {
                    val valor = negocio?.sitioWeb?.takeIf { it.isNotBlank() }
                    if (valor != null) {
                        Text(valor, fontSize = 16.sp, color = Color(0xFF3B82F6))
                    } else {
                        TextoVacio("Sin sitio web")
                    }
                }

                SeccionVer(titulo = "Categorías") {
                    val filtros = negocio?.filtros ?: emptyList()
                    if (filtros.isEmpty()) {
                        TextoVacio("Sin categorías seleccionadas")
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            filtros.forEach { filtro ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(filtro.nombre) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        labelColor = PrimaryColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
@Composable
private fun TextoVacio(texto: String) {
    Text(
        text = texto,
        fontSize = 15.sp,
        color = Color(0xFFADB5BD),
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
    )
}

@Composable
fun SeccionVer(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            contenido()
        }
    }
}