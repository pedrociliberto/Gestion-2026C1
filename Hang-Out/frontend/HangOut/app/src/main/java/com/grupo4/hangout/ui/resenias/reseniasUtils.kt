package com.grupo4.hangout.ui.resenias

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo4.hangout.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import com.grupo4.hangout.model.Resenia

@Composable
fun ReseniaItem(resenia: Resenia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ... (encabezado con nombre y fecha)

            // Estrellas
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < resenia.valoracion) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < resenia.valoracion) Color(0xFFFFB800) else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Texto de la reseña
            Text(
                text = resenia.textoResenia,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            // Imágenes de la reseña
            if (resenia.imagenes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    resenia.imagenes.forEach { imageUrl ->
                        val fullUrl = remember(imageUrl) {
                            if (imageUrl.startsWith("http")) imageUrl
                            else "${Config.BASE_URL}/${imageUrl.removePrefix("/")}"
                        }
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}


suspend fun fetchReseniasNegocio(negocioId: Int): List<Resenia> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/resenia/$negocioId")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.getJSONArray("resenias")
                val list = mutableListOf<Resenia>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val fecha = obj.getString("fecha_publicacion")
                    val fechaFormateada = formatearFechaGmtAEspanol(fecha)
                    val listaImagenes = obj.getJSONArray("imagenes")
                    val imagenes = mutableListOf<String>()
                    for (j in 0 until listaImagenes.length()) {
                        val imagen = listaImagenes.getString(j)
                        imagenes.add(imagen)
                    }
                    list.add(
                        Resenia(
                            nombreUsuario = obj.getString("usuario"),
                            valoracion = obj.getInt("puntaje"),
                            textoResenia = obj.getString("resenia"),
                            fecha = fechaFormateada,
                            imagenes = imagenes
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

private fun formatearFechaGmtAEspanol(fechaGmt: String): String {
    if (fechaGmt.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        val date = inputFormat.parse(fechaGmt) ?: return fechaGmt
        val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("es", "ES"))
        outputFormat.format(date)
    } catch (e: Exception) {
        fechaGmt
    }
}