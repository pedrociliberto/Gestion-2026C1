import sys

sys.path.append("/backend")
from models import db, ImagenNegocioDB

class RepositorioImagenes:
    def __init__(self):
        pass

    def guardar_imagen_negocio(self, id_usuario, url_imagen):
        """
        Guarda una imagen asociada a un negocio. Retorna True si se guardó correctamente, False en caso contrario.
        """
        db.session.add(ImagenNegocioDB(id_usuario=id_usuario, url_imagen=url_imagen))
        try:
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False
    
    def obtener_imagenes_negocio(self, id_usuario):
        """
        Obtiene las imágenes asociadas a un negocio. Retorna una lista con los IDs y URLs de las imágenes.
        """
        imagenes = ImagenNegocioDB.query.filter_by(id_usuario=id_usuario).all()
        return [{"id": imagen.id, "url_imagen": imagen.url_imagen} for imagen in imagenes]

    def obtener_imagen_por_id(self, id_usuario, id_imagen):
        """
        Obtiene una imagen específica asociada a un negocio. Retorna un diccionario con los datos de la imagen o None.
        """
        imagen = ImagenNegocioDB.query.filter_by(id_usuario=id_usuario, id=id_imagen).first()
        if imagen:
            return {"id": imagen.id, "url_imagen": imagen.url_imagen}
        return None
    
    def eliminar_imagen_negocio(self, id_usuario, id_imagen):
        """
        Elimina una imagen específica de un negocio. Retorna True si se eliminó correctamente, False en caso contrario.
        """
        imagen = ImagenNegocioDB.query.filter_by(id_usuario=id_usuario, id=id_imagen).first()
        if imagen:
            db.session.delete(imagen)
            try:
                db.session.commit()
                return True
            except Exception as e:
                db.session.rollback()
                return False
        return False