package com.grupo4.hangout.ui.planificadorPantalla

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(showBackground = true)
fun PlanificadorPantallaPreview(){
    PlanificadorPantalla(
        1,
        onNegocioClick = { id -> },
        modifier = Modifier,
        onProfileClick = {},
        onCerrarSesionClick = {},
        onNavigateToJuntadas = {}
    )
}