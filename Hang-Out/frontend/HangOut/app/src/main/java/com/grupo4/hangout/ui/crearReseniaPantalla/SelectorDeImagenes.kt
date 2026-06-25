package com.grupo4.hangout.ui.crearReseniaPantalla

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SelectorDeImagenes(color: Color, onImageSelected: (Uri?) -> Unit){
    val coroutineScope = rememberCoroutineScope()
    val disparadorPickearImagen = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), onImageSelected)

    Button(
        onClick = {disparadorPickearImagen.launch(PickVisualMediaRequest())},
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = "Agregar imagenes a la reseña")
    }
}

@Preview
@Composable
fun SelectorDeImagenesPreview(){
    SelectorDeImagenes(Color(0xFF2B7FFF), {uri -> Pair(true, "ABC")})
}