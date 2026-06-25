package com.grupo4.hangout.ui.planificadorPantalla

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectorHora(
    label: String,
    hora: String?,
    onHoraSeleccionada: (String) -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val abrirTimePicker = {
        val horaInicial = hora?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
        val minInicial  = hora?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, horaElegida, minElegido ->
                onHoraSeleccionada(String.format("%02d:%02d", horaElegida, minElegido))
            },
            horaInicial,
            minInicial,
            true
        ).show()
    }

    Surface(
        modifier = modifier.clickable { abrirTimePicker() },
        shape = RoundedCornerShape(10.dp),
        color = if (hora != null) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (hora != null) Color(0xFF1E40AF) else Color(0xFFCBD5E1)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (hora != null) Color(0xFF1E40AF) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (hora != null) Color(0xFF1E40AF) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = hora ?: "Elegir hora",
                    fontSize = 15.sp,
                    fontWeight = if (hora != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hora != null) Color(0xFF0F172A) else Color(0xFF64748B)
                )
            }
            if (hora != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpiar",
                    tint = Color(0xFF1E40AF),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onLimpiar)
                )
            }
        }
    }
}