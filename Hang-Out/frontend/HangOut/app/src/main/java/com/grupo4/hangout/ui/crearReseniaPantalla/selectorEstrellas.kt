package com.grupo4.hangout.ui.crearReseniaPantalla

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SelectorEstrellas(
    calificacion: Int,
    onCalificacionChange: (Int) -> Unit,
    colorActivo: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Calificación $i",
                tint = if (i <= calificacion) colorActivo else Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onCalificacionChange(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectorEstrellasPreview(){
    SelectorEstrellas(
        3, { numero ->}, Color.Yellow
    )
}