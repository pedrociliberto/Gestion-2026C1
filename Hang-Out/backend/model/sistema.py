from datetime import datetime
from zoneinfo import ZoneInfo

from model.juntada import Juntada
from model.excepciones import ExcepcionSistema
from model.buscador import Buscador
from model.resenia import Resenia
from model.imagen import Imagen
from repositorios.repositorio_propuestas import RepositorioPropuestas
from repositorios.repositorio_votacion import RepositorioVotacion

# No lo meto en Sistema porque sino tengo que cambiar medio programa:
repositorio_propuestas = RepositorioPropuestas()
repositorio_votacion = RepositorioVotacion()

class Sistema:
    def __init__(
            self, 
            repositorio_juntadas, 
            repositorio_usuarios, 
            repositorio_participantes,
            repositorio_negocio,
            repositorio_filtros,
            repositorio_resenia,
            repositorio_propuestas,
            proveedor_codigos,
        ):
        self.repositorio_juntadas = repositorio_juntadas
        self.repositorio_usuarios = repositorio_usuarios
        self.repositorio_participantes = repositorio_participantes
        self.repositorio_negocio = repositorio_negocio
        self.repositorio_filtros = repositorio_filtros
        self.proveedor_codigos = proveedor_codigos
        self.repositorio_resenia = repositorio_resenia
        self.repositorio_propuestas = repositorio_propuestas

    def crear_juntada(self, titulo, codigo, id_organizador):
        usuario = self.repositorio_usuarios.buscar_por_id(id_organizador)
        
        if not usuario:
            raise ExcepcionSistema("Usuario no encontrado")
        if codigo is not None and self.repositorio_juntadas.buscar_por_codigo(codigo) is not None:
            raise ExcepcionSistema("El codigo de juntada ya esta en uso")
            
        juntada = Juntada(titulo, codigo, usuario, self.proveedor_codigos, self.repositorio_juntadas)
        self.repositorio_juntadas.guardar(juntada)
        return juntada
    
    def unirse_a_juntada(self,codigo,id_usuario):
        juntada = self.repositorio_juntadas.buscar_por_codigo(codigo)

        if juntada is None: 
            raise ExcepcionSistema("Codigo invalido o juntada inexistente")
        
        if juntada.ya_paso():
            raise ExcepcionSistema("No se puede unir a una juntada que ya paso")

        if self.repositorio_participantes.ya_es_participante(juntada.id,id_usuario):
            raise ExcepcionSistema("Ya formas parte de esta juntada")
        
        self.repositorio_participantes.guardar(juntada.id,id_usuario)
        return juntada
    
    def salir_de_juntada(self, id_juntada, id_usuario):
        """
        Permite a un usuario salir de una juntada a la que se había unido previamente.
        Verifica que la juntada exista, que no haya pasado, y que el usuario sea efectivamente un participante antes de eliminar 
        su participación.
        """
        juntada = self.repositorio_juntadas.buscar_por_id(id_juntada)

        if juntada is None: 
            raise ExcepcionSistema("ID de juntada invalido o juntada inexistente")
        
        if juntada.ya_paso():
            raise ExcepcionSistema("No se puede salir de una juntada que ya paso")

        if not self.repositorio_participantes.ya_es_participante(juntada.id,id_usuario):
            raise ExcepcionSistema("No formas parte de esta juntada")

        es_organizador = getattr(juntada.organizador, "id", None) == id_usuario
        if es_organizador:
            # El organizador puede salir, pero debe delegar el rol a algún participante existente.
            ids_participantes = self.repositorio_participantes.obtener_ids_participantes(juntada.id)
            if not ids_participantes:
                raise ExcepcionSistema("El organizador no puede salir si no hay otros participantes")

            nuevo_id_organizador = ids_participantes[0]
            nuevo_organizador = self.repositorio_usuarios.buscar_por_id(nuevo_id_organizador)
            if not nuevo_organizador:
                raise ExcepcionSistema("Usuario no encontrado")

            juntada.organizador = nuevo_organizador
            self.repositorio_juntadas.actualizar_organizador(juntada.id, nuevo_id_organizador)
            # Evita que el nuevo organizador figure como participante.
            self.repositorio_participantes.eliminar(juntada.id, nuevo_id_organizador)

        propuestas = repositorio_propuestas.listar_propuestas_por_juntada(juntada.id)
        reabrio_votacion = False
        for propuesta in propuestas[0]:
            repositorio_votacion.borrar_votacion_usuario(id_usuario, propuesta.id)

            if propuesta.id_usuario == id_usuario:
                if propuesta.id == juntada.id_propuesta_ganadora():
                    juntada.abrir_votacion(juntada.organizador.id)
                    reabrio_votacion = propuesta.id

                repositorio_propuestas.eliminar_propuesta(propuesta.id)

        if reabrio_votacion:
            self.repositorio_juntadas.actualizar_estado_votacion(
                juntada.id,
                juntada.estado(),
                juntada.id_propuesta_ganadora()
            )

        if not es_organizador:
            self.repositorio_participantes.eliminar(juntada.id, id_usuario)

    def eliminar_juntada(self, id_juntada, id_usuario):
        juntada = self.repositorio_juntadas.buscar_por_id(id_juntada)

        if not juntada:
            raise ExcepcionSistema("Juntada no encontrada")

        if getattr(juntada.organizador, "id", None) != id_usuario:
            raise ExcepcionSistema("Solo el organizador puede eliminar la juntada")

        if juntada.estado() not in ("PENDIENTE", "CONFIRMADA"):
            raise ExcepcionSistema("Solo se puede eliminar una juntada pendiente o confirmada")

        self.repositorio_propuestas.eliminar_propuestas_por_juntada(id_juntada)
        self.repositorio_participantes.eliminar_por_juntada(id_juntada)
        self.repositorio_juntadas.eliminar(id_juntada)
        return True

    def obtener_detalle_completo(self, id_juntada):
        juntada = self.repositorio_juntadas.buscar_por_id(id_juntada)
        
        if not juntada:
            return None
        
        nombres_participantes = self.repositorio_participantes.obtener_nombres_participantes(id_juntada)
        return {
            "titulo": juntada.titulo(),
            "codigo": juntada.codigo(),
            "id_organizador": int(juntada.organizador.id),
            "organizador": juntada.organizador.nombre_completo,
            "participantes": nombres_participantes,
            "estado": juntada.estado(),
            "propuesta_ganadora": int(juntada.id_propuesta_ganadora()) if juntada.id_propuesta_ganadora() else None
        }
    
    def listar_juntadas_usuario(self, usuario_id, repo_propuestas): 
        """
        Delega la obtención, control de caducidad y ordenación al repositorio, 
        manteniendo el servicio libre de lógica de persistencia o formatos temporales.
        """
        try:
            return self.repositorio_juntadas.buscar_por_usuario(usuario_id, repo_propuestas)
        except Exception as e:
            print(f"Error en servicio listar_juntadas_usuario: {e}")
            return []

    def buscar_negocio(self, busqueda, id_usuario, filtros_seleccionados=[], filtros_dia=None, filtro_hora_desde=None, filtro_hora_hasta=None, usar_filtros_usuario=False):
        if not id_usuario or not self.repositorio_usuarios.buscar_por_id(id_usuario):
            raise ExcepcionSistema("El usuario no existe")

        filtros_usuario = self.repositorio_filtros.listar_filtros_usuario(id_usuario)
        buscador = Buscador(self.repositorio_negocio)
        resultado = buscador.buscar_negocio(
            busqueda,
            filtros_usuario,
            filtros_seleccionados,
            filtros_dia,
            filtro_hora_desde,
            filtro_hora_hasta,
            usar_filtros_usuario
        )

        return resultado

    def crear_resenia(self, id_usuario, id_juntada, id_negocio, valoracion, comentario):
        self._validar_que_no_es_resenia_repetida(id_usuario, id_juntada, id_negocio)
        juntada = self.repositorio_juntadas.buscar_por_id(id_juntada)
        self._comprobar_que_juntada_sea_pasada(juntada)

        id_propuesta_ganadora = juntada.id_propuesta_ganadora()
        propuesta_ganadora = self.repositorio_propuestas.buscar_por_id(id_propuesta_ganadora)
        self._validar_que_coincide_negocio(id_negocio, propuesta_ganadora)

        resenia = Resenia(id_usuario, id_juntada, id_negocio, valoracion, comentario)
        guardo_resenia = self.repositorio_resenia.guardar_resenia(resenia)
        if not guardo_resenia:
            raise ExcepcionSistema("Fallo interno al guardar la resenia")
        
        return True

    def buscar_resenias_por_negocio(self, id_negocio):
        resenias = self.repositorio_resenia.buscar_resenias_por_negocio(id_negocio)
        return resenias
    
    def agregar_imagen_a_resenia(self, id_juntada, id_negocio, id_usuario, contenido_imagen, nombre_imagen):
        resenia = self.repositorio_resenia.buscar_resenia(id_usuario, id_juntada, id_negocio)
        imagen = Imagen(resenia, contenido_imagen, nombre_imagen)

        resenia.agregar_imagen(imagen)
        self.repositorio_resenia.guardar_resenia(resenia)

        return imagen

    def obtener_imagenes_resenia(self, id_juntada, id_negocio, id_usuario):
        resenia = self.repositorio_resenia.buscar_resenia(id_usuario, id_juntada, id_negocio)

        return resenia.imagenes()
    
    def borrar_imagenes_resenia(self, id_juntada, id_negocio, id_usuario):
        resenia = self.repositorio_resenia.buscar_resenia(id_usuario, id_juntada, id_negocio)

        resenia.borrar_imagenes()
        self.repositorio_resenia.guardar_resenia(resenia)

    def borrar_resenia(self, id_juntada, id_negocio, id_usuario):
        resenia = self.repositorio_resenia.buscar_resenia(id_usuario, id_juntada, id_negocio)
        if resenia:
            self.repositorio_resenia.borrar_resenia(resenia)

    def _validar_que_coincide_negocio(self, id_negocio, propuesta_ganadora):
        if propuesta_ganadora.id_negocio != id_negocio:
            raise ExcepcionSistema("No se puede reseñar un lugar no visitado")

    def _validar_que_no_es_resenia_repetida(self, id_usuario, id_juntada, id_negocio):
        if self.repositorio_resenia.buscar_resenia(id_usuario, id_juntada, id_negocio):
            raise ExcepcionSistema("Ya hiciste una reseña de ese lugar para esta juntada")

    def _comprobar_que_juntada_sea_pasada(self, juntada):
        if juntada.estado() != "PASADA":
            raise ExcepcionSistema("La juntada debe ser pasada y estar cerrada")
        
