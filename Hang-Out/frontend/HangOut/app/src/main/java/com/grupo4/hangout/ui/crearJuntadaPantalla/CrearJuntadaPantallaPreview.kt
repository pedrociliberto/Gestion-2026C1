package com.grupo4.hangout.ui.crearJuntadaPantalla

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/*
Esto esta bueno para poder ver cada pantalla por separado
sin compilar ni nada. Cuando tiene este @Preview, arriba a la derecha
(en android studio) se puede elegir la vista que tiene codigo y pantalla
a la derecha para poder ir viendo.
*/
@Preview(showBackground = true)
@Composable
fun PantallaCrearJuntadaPreview(){
    CrearJuntadaPantalla(
        userId = 1,
        onBack = {}
    )
}