package com.grupo4.hangout.ui.crearReseniaPantalla

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CampoResenia(
    comentario: String,
    onComentarioChange: (String) -> Unit,
    colorEnfoque: Color,
    maxCaracteres: Int = 500
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = comentario,
            onValueChange = { texto ->
                if (texto.length <= maxCaracteres) {
                    onComentarioChange(texto)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text(text = "Escribe tu reseña aquí (Opcional)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorEnfoque,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = colorEnfoque
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            text = "${comentario.length} / $maxCaracteres",
            style = MaterialTheme.typography.bodySmall,
            color = if (comentario.length == maxCaracteres) Color.Red else Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp),
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CampoReseniaPreview(){
    CampoResenia("Un buen lugar para ir con amigos, ...", {texto ->}, Color.Gray)
}