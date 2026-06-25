package com.grupo4.hangout.ui.modificarNegocioPantalla

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo4.hangout.PrimaryColor

@Composable
fun SelectorUbicacion(ubicacion: String, onValueChange: (String) -> Unit){
    OutlinedTextField(
        value = ubicacion,
        onValueChange = onValueChange,
        label = { Text("URL Ubicación (opcional)") },
        placeholder = { Text("Puedes ingresar la URL de tu ubicación en Google Maps.") },
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
}