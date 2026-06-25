package com.grupo4.hangout.ui.editarImagenesNegocioPantalla

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.grupo4.hangout.Config
import com.grupo4.hangout.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class ImagenNegocio(
    val id: Int,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarImagenesNegocioPantalla(
    userId: Int,
    token: String,
    onBack: () -> Unit
) {
    var imagenes by remember { mutableStateOf<List<ImagenNegocio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isUploading = true
                val result = uploadImagen(context, userId, token, uri)
                isUploading = false
                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                if (result.first) {
                    imagenes = fetchImagenes(userId, token)
                }
            }
        }
    }

    LaunchedEffect(userId) {
        imagenes = fetchImagenes(userId, token)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Imágenes del Negocio", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                containerColor = PrimaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Imagen")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryColor)
            } else if (imagenes.isEmpty()) {
                Text(
                    "No hay imágenes cargadas.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(imagenes) { imagen ->
                        ImagenCard(
                            imagen = imagen,
                            onDelete = {
                                coroutineScope.launch {
                                    val result = deleteImagen(userId, token, imagen.id)
                                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                    if (result.first) {
                                        imagenes = fetchImagenes(userId, token)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (isUploading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ImagenCard(imagen: ImagenNegocio, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isImageLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val imageUrl = remember(imagen.url) {
        if (imagen.url.startsWith("http")) {
            imagen.url
        } else {
            val path = if (imagen.url.startsWith("/")) imagen.url else "/${imagen.url}"
            "${Config.BASE_URL}$path"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            isImageLoading = true
                            hasError = false
                        }
                        is AsyncImagePainter.State.Success -> {
                            isImageLoading = false
                            hasError = false
                        }
                        is AsyncImagePainter.State.Error -> {
                            isImageLoading = false
                            hasError = true
                            Log.e("ImagenCard", "Error cargando imagen: $imageUrl", state.result.throwable)
                        }
                        else -> {}
                    }
                }
            )

            if (isImageLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = PrimaryColor,
                    strokeWidth = 2.dp
                )
            }

            if (hasError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ImageNotSupported,
                        contentDescription = "Error",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Text("Error", color = Color.Gray, fontSize = 10.sp)
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Imagen") },
            text = { Text("¿Estás seguro de que querés eliminar esta imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

suspend fun fetchImagenes(userId: Int, token: String): List<ImagenNegocio> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/imagenes/$userId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val arr = json.getJSONArray("imagenes")
                val list = mutableListOf<ImagenNegocio>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(ImagenNegocio(obj.getInt("id"), obj.getString("url")))
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

suspend fun uploadImagen(context: android.content.Context, userId: Int, token: String, uri: Uri): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val file = uriToFile(context, uri) ?: return@withContext Pair(false, "No se pudo procesar la imagen")
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("imagen", file.name, file.asRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/imagenes/$userId")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                if (response.isSuccessful) {
                    Pair(true, json.optString("mensaje", "Imagen subida con éxito"))
                } else {
                    Pair(false, json.optString("error", "Error al subir la imagen"))
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}

suspend fun deleteImagen(userId: Int, token: String, imagenId: Int): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply { put("id_imagen", imagenId) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toRequestBody().contentType())
        
        // Corregido para usar la extensión de okhttp3 adecuadamente si es necesario, 
        // pero mantendremos la lógica compatible con la versión anterior.
        val request = Request.Builder()
            .url("${Config.BASE_URL}/negocios/imagenes/$userId")
            .addHeader("Authorization", "Bearer $token")
            .delete(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val jsonRes = JSONObject(responseBody)
                if (response.isSuccessful) {
                    Pair(true, jsonRes.optString("mensaje", "Imagen eliminada"))
                } else {
                    Pair(false, jsonRes.optString("error", "Error al eliminar la imagen"))
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error de conexión")
        }
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}
