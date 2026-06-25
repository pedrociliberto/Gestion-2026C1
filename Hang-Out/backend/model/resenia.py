from model.excepciones import ExcepcionSistema

VALORACIONES_VALIDAS = [1,2,3,4,5]
LARGO_MAXIMO_COMENTARIO = 500
CANTIDAD_MAX_IMAGENES = 5

class Resenia:
    def __init__(self, id_usuario, id_juntada, id_negocio, valoracion, comentario, fecha_publicacion=""):
        self._validar_que_hay_valoracion(valoracion)
        self._validar_valoracion_correcta(valoracion)
        self._validar_que_no_hay_solo_espacios(comentario)
        self._validar_que_no_excede_largo_maximo(comentario)

        self.id_usuario = id_usuario
        self.id_juntada = id_juntada
        self.id_negocio = id_negocio
        self.valoracion = valoracion
        self.comentario = comentario
        self.fecha_publicacion = fecha_publicacion
        self._imagenes = []

    def agregar_imagen(self, imagen):
        if len(self._imagenes) >= CANTIDAD_MAX_IMAGENES:
            raise ExcepcionSistema("No se pueden cargar mas de 5 imagenes a la reseña")
        self._imagenes.append(imagen)

    def imagenes(self):
        return self._imagenes.copy()
    
    def borrar_imagenes(self):
        self._imagenes = []

    def _validar_que_no_excede_largo_maximo(self, comentario):
        if len(comentario) > LARGO_MAXIMO_COMENTARIO:
            raise ExcepcionSistema(f"El largo maximo del comentario es {LARGO_MAXIMO_COMENTARIO} caracteres")

    def _validar_que_no_hay_solo_espacios(self, comentario):
        if len(comentario) != 0 and comentario.strip() == "":
            raise ExcepcionSistema("El comentario no pueden ser solo espacios")

    def _validar_valoracion_correcta(self, valoracion):
        if not valoracion in VALORACIONES_VALIDAS:
            raise ExcepcionSistema("La valoración debe estar entre 1 y 5 (inclusive)")

    def _validar_que_hay_valoracion(self, valoracion):
        if not valoracion:
            raise ExcepcionSistema("Se debe especificar una valoración numerica")