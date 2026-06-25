package com.grupo4.hangout.model

data class Juntada(
    val id: Int,
    val titulo: String,
    val codigo: String,
    val rol: String,
    val idOrganizador: Int,
    val organizador: String,
    val participantes: List<String>,
    val estado: String = "PENDIENTE", // "PENDIENTE", "CONFIRMADA", "PASADA"
    val idPropuestaGanadora: Int? = null,
    val fechaHoraInicio: String? = null,
    val fechaHoraFin: String? = null
)

data class Filtro(
    val id: Int,
    val nombre: String
)

data class Negocio(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val horarios: String,
    val ubicacion : String,
    val sitioWeb: String,
    val filtros: List<Filtro>,
    val imagenes: List<String> = emptyList(),
    val URLUbicacion: String = "",
    val tienePosicionamiento: Boolean = false
)

data class NegocioResponse(
    val existe: Boolean,
    val mensaje: String? = null,
    val negocio: Negocio
)

data class FiltrosResponse(
    val filtros: List<Filtro>
)

data class Propuesta(
    val id: Int,
    val idUsuario: Int,
    val idNegocio: Int?,
    val nombreNegocio: String?,
    val nombreUsuario: String,
    val lugarPersonalizado: String?,
    val fechaHoraInicio: String,
    val fechaHoraFin: String?,
    var cantidadVotos: Int,
    var yoVote: Boolean
)

data class Aparicion(
    val en_votacion: Boolean,
    val es_ganadora: Boolean,
    val fecha_hora_fin: String?,
    val fecha_hora_inicio: String,
    val id_juntada: Int,
    val id_propuesta: Int
)

data class PropuestasGanadoras(
    val cantidad_futuras: Int,
    val cantidad_pasadas: Int,
    val cantidad_total: Int,
    val futuras: List<Aparicion>,
    val pasadas: List<Aparicion>
)

data class EstadisticasNegocio(
    val apariciones: List<Aparicion>,
    val cantidad_apariciones_en_votacion: Int,
    val cantidad_apariciones_ganadoras: Int,
    val cantidad_apariciones_no_ganadoras: Int,
    val cantidad_apariciones_total: Int,
    val id_negocio: Int,
    val propuestas_ganadoras: PropuestasGanadoras
)

data class Beneficio(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class Resenia(
    val nombreUsuario: String,
    val valoracion: Int,
    val textoResenia: String,
    val fecha: String,
    var imagenes: List<String>
)

data class Descuento(
    val id: Int,
    val idNegocio: Int,
    val descripcion: String,
    val porcentaje: Int?,
    val monto: Double?,
    val codigo: String
)

data class Notificacion(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val leida: Boolean,
)
