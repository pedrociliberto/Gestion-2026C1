from flask import jsonify

from model.excepciones import ExcepcionSistema
from repositorios.repositorio_imagenes import RepositorioImagenes
from repositorios.repositorio_negocio import RepositorioNegocio

repositorio_negocio = RepositorioNegocio()
repositorio_imagenes = RepositorioImagenes()

MAX_IMAGENES_POR_NEGOCIO = 5

def agregar_imagen_negocio(id_usuario, url_imagen):
    """
    Agrega una imagen a un negocio existente. Si el negocio no existe o si ocurre un error al guardar la imagen,
    se lanza una ExcepcionSistema.
    """
    if not repositorio_negocio.obtener_negocio(id_usuario):
        raise ExcepcionSistema("El negocio indicado no existe")
    
    if len(obtener_imagenes_negocio(id_usuario)) >= MAX_IMAGENES_POR_NEGOCIO:
        raise ExcepcionSistema(f"No se pueden agregar más de {MAX_IMAGENES_POR_NEGOCIO} imágenes a un negocio")

    if not repositorio_imagenes.guardar_imagen_negocio(id_usuario, url_imagen):
        raise ExcepcionSistema("Error al guardar la imagen del negocio")

def obtener_imagenes_negocio(id_usuario):
    """
    Obtiene las imágenes asociadas a un negocio. Si el negocio no existe, se lanza una ExcepcionSistema.
    """
    if not repositorio_negocio.obtener_negocio(id_usuario):
        raise ExcepcionSistema("El negocio indicado no existe")

    return repositorio_imagenes.obtener_imagenes_negocio(id_usuario)

def obtener_imagen_por_id(id_usuario, id_imagen):
    """
    Obtiene una imagen específica asociada a un negocio. Si el negocio no existe, se lanza una ExcepcionSistema.
    """
    if not repositorio_negocio.obtener_negocio(id_usuario):
        raise ExcepcionSistema("El negocio indicado no existe")

    return repositorio_imagenes.obtener_imagen_por_id(id_usuario, id_imagen)

def eliminar_imagen_negocio(id_usuario, id_imagen):
    """
    Elimina una imagen específica de un negocio. Si el negocio no existe o si ocurre un error al eliminar la imagen,
    se lanza una ExcepcionSistema.
    """
    if not repositorio_negocio.obtener_negocio(id_usuario):
        raise ExcepcionSistema("El negocio indicado no existe")

    if not repositorio_imagenes.eliminar_imagen_negocio(id_usuario, id_imagen):
        raise ExcepcionSistema("Error al eliminar la imagen del negocio")