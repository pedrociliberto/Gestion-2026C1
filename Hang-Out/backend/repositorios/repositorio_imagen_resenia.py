import sys
import os
from uuid import uuid4
sys.path.append("/backend")

from models import ImagenReseniaDB, db
from model.imagen import Imagen
import os

class RepositorioImagenResenia:
    def __init__(self):
        self.nombre = "Repositorio imagenes resenias"

    def buscar_imagenes_en_resenia(self, resenia):
        id_juntada = resenia.id_juntada
        id_usuario = resenia.id_usuario
        id_negocio = resenia.id_negocio

        imagenes_db = ImagenReseniaDB.query.filter_by(
            id_juntada = id_juntada,
            id_usuario = id_usuario,
            id_negocio = id_negocio
        ).all()

        imagenes = [self._armar_imagen(resenia, imagen_db) for imagen_db in imagenes_db]

        return imagenes

    def guardar(self, imagen):
        """
        Guarda una imagen en disco
        """

        resenia = imagen.resenia
        id_negocio = resenia.id_negocio
        id_juntada = resenia.id_juntada
        id_usuario = resenia.id_usuario

        nombre_imagen = self._guardar_contenido_imagen(imagen)

        imagen_db = ImagenReseniaDB(
            id_negocio=id_negocio,
            id_juntada=id_juntada,
            id_usuario=id_usuario,
            nombre_imagen=nombre_imagen
        )
        
        try:
            db.session.add(imagen_db)
            db.session.commit()
        except:
            db.session.rollback()
            raise Exception("Fallo al guardar imagen de resenia")
    
    def actualizar_imagenes_resenia(self, resenia):
        id_juntada = resenia.id_juntada
        id_usuario = resenia.id_usuario
        id_negocio = resenia.id_negocio

        imagenes = resenia.imagenes()

        ImagenReseniaDB.query.filter_by(
            id_juntada = id_juntada,
            id_usuario = id_usuario,
            id_negocio = id_negocio
        ).delete()

        db.session.commit()

        for imagen in imagenes:
            self.guardar(imagen)
    
    def _guardar_contenido_imagen(self, imagen):
        _, extension_imagen = os.path.splitext(imagen.nombre) #Separa nombre y extension
        nombre_imagen = f"{uuid4().hex}{extension_imagen}"
        carpeta_imagenes = os.getenv("IMAGENES_NEGOCIOS_FOLDER", "")
        ruta_imagen = os.path.join(carpeta_imagenes, nombre_imagen)

        with open(ruta_imagen, "wb") as archivo_imagen:
            archivo_imagen.write(imagen.contenido)

        return nombre_imagen
    
    def _armar_imagen(self, resenia, imagen_db):
        ruta_imagen = os.path.join(os.environ.get('IMAGENES_NEGOCIOS_FOLDER'), imagen_db.nombre_imagen)
        with open(ruta_imagen, "rb") as archivo_imagen:
            contenido_imagen = archivo_imagen.read(5*(10**6))

        return Imagen(resenia, contenido_imagen, imagen_db.nombre_imagen)

