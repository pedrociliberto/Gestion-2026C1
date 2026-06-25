package com.grupo4.hangout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.grupo4.hangout.navigation.AppNavigation

val PrimaryColor = Color(0xFF1976D2)
val SecondaryColor = Color(0xFF64B5F6)
val GradientEnd = Color(0xFFBBDEFB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Surface provides the full screen background color
                Surface(modifier = Modifier.fillMaxSize()) {
                    // To manage navigation across screens:
                    AppNavigation()
                }
            }
        }
    }
}