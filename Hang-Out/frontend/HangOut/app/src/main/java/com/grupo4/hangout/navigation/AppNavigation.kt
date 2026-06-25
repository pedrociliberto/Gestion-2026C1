package com.grupo4.hangout.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grupo4.hangout.ui.loginPantalla.LoginPantalla
import com.grupo4.hangout.ui.panelJuntadasPantalla.PanelJuntadasPantalla
import com.grupo4.hangout.ui.crearJuntadaPantalla.CrearJuntadaPantalla
import com.grupo4.hangout.ui.registroPantalla.RegistroPantalla
import com.grupo4.hangout.ui.unirseJuntadaPantalla.UnirseJuntadaPantalla
import com.grupo4.hangout.ui.detalleJuntadaPantalla.DetalleJuntadaPantalla
import com.grupo4.hangout.ui.planificadorPantalla.PlanificadorPantalla
import com.grupo4.hangout.ui.perfilPantalla.PerfilPantalla
import com.grupo4.hangout.ui.notificacionesPantalla.NotificacionesPantalla
import com.grupo4.hangout.ui.notificacionesPantalla.MisNotificacionesNegocioPantalla
import com.grupo4.hangout.ui.crearNotificacionPantalla.CrearNotificacionPantalla
import com.grupo4.hangout.ui.verNegocioEnPerfilPantalla.VerNegocioEnPerfilPantalla
import com.grupo4.hangout.ui.modificarNegocioPantalla.ModificarNegocioPantalla
import com.grupo4.hangout.ui.editarImagenesNegocioPantalla.EditarImagenesNegocioPantalla
import com.grupo4.hangout.ui.modificarPerfilPantalla.ModificarPerfilPantalla
import androidx.core.content.edit
import com.grupo4.hangout.model.Negocio
import com.grupo4.hangout.ui.verNegocioBuscadoPantalla.VerNegocioBuscadoPantalla
import com.grupo4.hangout.ui.crearPropuestaPantalla.CrearPropuestaPantalla
import com.grupo4.hangout.ui.crearReseniaPantalla.CrearReseniaPantalla
import com.grupo4.hangout.ui.estadisticasNegocioPantalla.EstadisticasNegocioPantalla
import com.grupo4.hangout.ui.beneficiosPantalla.BeneficiosPantalla
import com.grupo4.hangout.ui.beneficiosPantalla.SuscribirBeneficioPantalla
import com.grupo4.hangout.ui.vistaCalendario.VistaCalendario
import androidx.compose.material.icons.filled.Star
import com.grupo4.hangout.ui.descuentosPantalla.DescuentosPantalla

@Preview
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("hangout_session", Context.MODE_PRIVATE)
    }
    val negocioSeleccionadoState = remember { mutableStateOf<Negocio?>(null) }

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            LaunchedEffect(Unit) {
                val guardadoUserId = sharedPreferences.getInt("user_id", -1)
                val guardadoEsPersonal = sharedPreferences.getBoolean("es_personal", true)
                val guardadoToken = sharedPreferences.getString("token", "") ?: ""
                if (guardadoUserId != -1 && guardadoToken.isNotEmpty()) {
                    navController.navigate("panelScreen/$guardadoUserId/$guardadoEsPersonal") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }

        composable("login") {
            LoginPantalla(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { jsonCompleto ->
                    val token = jsonCompleto.getString("token")
                    val userData = jsonCompleto.getJSONObject("data")
                    val userId = userData.getInt("id")
                    val esPersonal = userData.getBoolean("es_cuenta_personal")
                    sharedPreferences.edit {
                        putInt("user_id", userId)
                        putBoolean("es_personal", esPersonal)
                        putString("token", token)
                    }
                    navController.navigate("panelScreen/$userId/$esPersonal") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegistroPantalla(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "panelScreen/{userId}/{esPersonal}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("esPersonal") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val esPersonal = backStackEntry.arguments?.getBoolean("esPersonal") ?: true
            if (esPersonal) {
                PanelJuntadasPantalla(
                    userId = userId,
                    esPersonal = esPersonal,
                    onNavigateToCrearJuntada = {
                        navController.navigate("crearJuntada/$userId")
                    },
                    onNavigateToDetalleJuntada = { juntadaId ->
                        navController.navigate("juntadaDetallada/$userId/$juntadaId")
                    },
                    onNavigateToUnirseJuntada = {
                        navController.navigate("unirseJuntada/$userId")
                    },
                    onNavigateToPerfil = {
                        navController.navigate("perfil/$userId/$esPersonal")
                    },
                    onNavigateToPlanificador = {
                        navController.navigate("planificador/$userId/$esPersonal")
                    },
                    onNavigateToDescuentos = {
                        navController.navigate("descuentos/$userId")
                    },
                    onNavigateToNotificaciones = {
                        navController.navigate("notificaciones/$userId")
                    },
                    onNavigateToCalendario = {
                        navController.navigate("calendario/$userId")
                    },
                    onCerrarSesion = {
                        sharedPreferences.edit { clear() }
                        navController.navigate("login") {
                            popUpTo("panelScreen/$userId/$esPersonal") { inclusive = true }
                        }
                    }
                )
            } else {
                val token = sharedPreferences.getString("token", "") ?: ""
                EstadisticasNegocioPantalla(
                    userId = userId,
                    token = token,
                    onNavigateToPerfil = {
                        navController.navigate("perfil/$userId/$esPersonal")
                    },
                    onNavigateToPlanificador = {
                        navController.navigate("planificador/$userId/$esPersonal")
                    },
                    onNavigateToDescuentos = {
                        navController.navigate("descuentos/$userId")
                    },
                    onCerrarSesion = {
                        sharedPreferences.edit { clear() }
                        navController.navigate("login") {
                            popUpTo("panelScreen/$userId/$esPersonal") { inclusive = true }
                        }
                    },
                    onNavigateToModificarNegocio = {
                        navController.navigate("modificarNegocio/$userId")
                    }
                )
            }
        }

        composable(
            route = "crearJuntada/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            CrearJuntadaPantalla(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "unirseJuntada/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UnirseJuntadaPantalla(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "juntadaDetallada/{userId}/{juntadaId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("juntadaId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val juntadaId = backStackEntry.arguments?.getInt("juntadaId") ?: 0
            DetalleJuntadaPantalla(
                juntadaId = juntadaId,
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToCrearPropuesta = { uid, jid ->
                    navController.navigate("crearPropuesta/$uid/$jid")
                },
                onNavigateToCrearResenia = { juntadaId, negocioId, nombreNegocio ->
                    navController.navigate("crearResenia/$juntadaId/$negocioId/$nombreNegocio")
                }
            )
        }

        composable(
            route = "crearPropuesta/{userId}/{juntadaId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("juntadaId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val juntadaId = backStackEntry.arguments?.getInt("juntadaId") ?: 0
            CrearPropuestaPantalla(
                userId = userId,
                juntadaId = juntadaId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "perfil/{userId}/{esPersonal}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("esPersonal") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val esPersonal = backStackEntry.arguments?.getBoolean("esPersonal") ?: true
            val token = sharedPreferences.getString("token", "") ?: ""
            PerfilPantalla(
                userId = userId,
                esPersonal = esPersonal,
                token = token,
                onNavigateToVerNegocio = {
                    navController.navigate("verNegocio/$userId")
                },
                onNavigateToModificarPerfil = {
                    navController.navigate("modificarPerfil/$userId")
                },
                onCerrarSesion = {
                    sharedPreferences.edit { clear() }
                    navController.navigate("login") {
                        popUpTo("perfil/$userId/$esPersonal") { inclusive = true }
                    }
                },
                onNavigateToJuntadas = {
                    navController.navigate("panelScreen/$userId/$esPersonal")
                },
                onNavigateToPlanificador = {
                    navController.navigate("planificador/$userId/$esPersonal")
                },
                onNavigateToDescuentos = {
                    navController.navigate("descuentos/$userId")
                },
                onNavigateToCrearNotificacion = {
                    navController.navigate("crearNotificacion/$userId")
                },
                onNavigateToMisNotificacionesEnviadas = {
                    navController.navigate("misNotificacionesNegocio/$userId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "crearNotificacion/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            CrearNotificacionPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "modificarPerfil/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            ModificarPerfilPantalla(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "misNotificacionesNegocio/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            MisNotificacionesNegocioPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "verNegocio/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            VerNegocioEnPerfilPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() },
                onNavigateToModificar = {
                    navController.navigate("modificarNegocio/$userId")
                },
                onNavigateToBeneficios = {
                    navController.navigate("beneficios/$userId")
                }
            )
        }

        composable(route = "verNegocioBuscado") {
            negocioSeleccionadoState.value?.let { negocio ->
                VerNegocioBuscadoPantalla(
                    negocio = negocio,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = "planificador/{userId}/{esPersonal}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("esPersonal") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val esPersonal = backStackEntry.arguments?.getBoolean("esPersonal") ?: true
            PlanificadorPantalla(
                userId = userId,
                esPersonal = esPersonal,
                onNegocioClick = { negocio ->
                    negocioSeleccionadoState.value = negocio
                    navController.navigate("verNegocioBuscado")
                },
                onNavigateToJuntadas = {navController.navigate("panelScreen/$userId/$esPersonal")},
                onCerrarSesionClick = {
                    sharedPreferences.edit { clear() }
                    navController.navigate("login") {
                        popUpTo("planificador/$userId/$esPersonal") { inclusive = true }
                    }
                },
                onProfileClick = {navController.navigate("perfil/$userId/$esPersonal")},
                onNotificationsClick = { navController.navigate("notificaciones/$userId") }
            )
        }

        composable(
            route = "modificarNegocio/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            ModificarNegocioPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() },
                onNavigateToEditarImagenes = {
                    navController.navigate("editarImagenesNegocio/$userId")
                }
            )
        }

        composable(
            route = "editarImagenesNegocio/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            EditarImagenesNegocioPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "crearResenia/{juntadaId}/{negocioId}/{nombreNegocio}",
            arguments = listOf(
                navArgument("juntadaId") { type = NavType.IntType },
                navArgument("negocioId") { type = NavType.IntType },
                navArgument("nombreNegocio") {type = NavType.StringType}
            )
        ) {backStackEntry ->
            val userId = sharedPreferences.getInt("user_id", 0) ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            val juntadaId = backStackEntry.arguments?.getInt("juntadaId") ?: 0
            val negocioId = backStackEntry.arguments?.getInt("negocioId") ?: 0
            val nombreNegocio = backStackEntry.arguments?.getString("nombreNegocio") ?: ""

            CrearReseniaPantalla(nombreNegocio, juntadaId, negocioId, userId, token, onBack = {navController.popBackStack()})
        }

        composable(
            route = "beneficios/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            BeneficiosPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() },
                onNavigateToSuscribir = { navController.navigate("suscribirBeneficio/$userId") }
            )
        }

        composable(
            route = "suscribirBeneficio/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""
            SuscribirBeneficioPantalla(
                userId = userId,
                token = token,
                onBack = { navController.popBackStack() },
                onSuscripcionExitosa = {
                    navController.navigate("beneficios/$userId") {
                        popUpTo("suscribirBeneficio/$userId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "calendario/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            VistaCalendario(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToDetalleJuntada = { juntadaId ->
                    navController.navigate("juntadaDetallada/$userId/$juntadaId")
                }
            )
        }

        composable(
            route = "descuentos/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = sharedPreferences.getString("token", "") ?: ""

            DescuentosPantalla(
                userId = userId,
                token = token,
                onNavigateToEstadisticas = {
                    navController.navigate("panelScreen/$userId/false") {
                        popUpTo("descuentos/$userId") { inclusive = true }
                    }
                },
                onNavigateToPerfilNegocio = {
                    navController.navigate("perfil/$userId/false")
                },
                onCerrarSesionClick = {
                    sharedPreferences.edit { clear() }
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "notificaciones/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            NotificacionesPantalla(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

    }
}
