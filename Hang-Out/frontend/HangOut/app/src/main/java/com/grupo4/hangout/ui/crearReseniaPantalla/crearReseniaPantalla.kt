package com.grupo4.hangout.ui.crearReseniaPantalla

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.hangout.PrimaryColor
import com.grupo4.hangout.model.Juntada
import com.grupo4.hangout.model.Negocio
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CrearReseniaPantalla(
    nombreNegocio: String,
    juntadaId: Int,
    negocioId: Int,
    userId: Int,
    token: String,
    onBack: () -> Unit
) {
    val azulCustom = Color(0xFF2B7FFF)

    var calificacion by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var imagenes by remember {mutableStateOf(mutableListOf<Uri>())}

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿Como estuvo ${nombreNegocio}?",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SelectorEstrellas(
                calificacion = calificacion,
                onCalificacionChange = { calificacion = it },
                colorActivo = azulCustom
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoResenia(
                comentario = comentario,
                onComentarioChange = { comentario = it },
                colorEnfoque = azulCustom
            )

            Text(text="Imagenes de la reseña")

            GrillaImagenesSeleccionadas(imagenes) { uri ->
                run {
                    val indiceBorrada = imagenes.indexOf(uri)

                    if (indiceBorrada != -1) {
                        val imagenesNuevas = mutableListOf<Uri>()
                        imagenesNuevas.addAll(imagenes)
                        imagenesNuevas.removeAt(indiceBorrada)
                        imagenes = imagenesNuevas
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SelectorDeImagenes(azulCustom) {uri ->
                if(uri != null){
                    val imagenesNuevas = mutableListOf<Uri>()
                    imagenesNuevas.addAll(imagenes)
                    imagenesNuevas.add(uri)
                    imagenes = imagenesNuevas
                }
            }
        }

        if(isLoading){
            CircularProgressIndicator(color = PrimaryColor)
        } else{
            BotonEnviarResenia(
                onClick = {
                    isLoading = true

                    coroutineScope.launch {
                        val respuesta = enviarResenia(
                            calificacion,
                            comentario,
                            juntadaId,
                            negocioId,
                            userId,
                            token
                        )
                        val seCreoResenia = respuesta.first

                        if (!seCreoResenia){
                            val mensajeRta = respuesta.second
                            Toast.makeText(context, mensajeRta, Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        val respuestaImagenes = enviarImagenes(context, imagenes, negocioId, juntadaId, userId, token)
                        val seSubieronImagenes = respuestaImagenes.first
                        if(!seSubieronImagenes){
                            val mensajeRta = respuestaImagenes.second
                            Toast.makeText(context, mensajeRta, Toast.LENGTH_LONG).show()
                            borrarImagenesJuntada(negocioId, juntadaId, userId, token)
                            borrarResenia(negocioId, juntadaId, userId, token)
                        } else{
                            Toast.makeText(context, "Reseña enviada", Toast.LENGTH_LONG).show()
                            onBack()
                        }

                        isLoading = false
                    }
                },
                habilitado = calificacion > 0,
                colorFondo = azulCustom
            )
        }
    }
}

@Preview
@Composable
fun CrearReseniaPantallaPreview(){
    val juntada = Juntada(1, "Cafecito", "ABCD", "organizador", idOrganizador = 1, "Juan", mutableListOf(), "PENDIENTE")
    val negocio = Negocio(1, "Cafe Martinez", "cafe", "LU 08:00-10:00", "CABA", "cafe.com", mutableListOf())
    CrearReseniaPantalla("Cafe Martinez", 1, 1, 1, "AbC", {})
}