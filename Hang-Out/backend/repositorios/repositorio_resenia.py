import sys
from models import ReseniaDB, db
from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_imagen_resenia import RepositorioImagenResenia

sys.path.append("/backend")
from model.resenia import Resenia

class RepositorioResenia:
    def __init__(self, proveedor_horario):
        self.proveedor_horario = proveedor_horario
        self.repositorio_imagenes = RepositorioImagenResenia()

    def guardar_resenia(self, resenia):
        resenia_db = ReseniaDB(
            id_usuario = resenia.id_usuario,
            id_juntada = resenia.id_juntada,
            id_negocio = resenia.id_negocio,
            puntaje = resenia.valoracion,
            resenia = resenia.comentario,
            fecha_publicacion = self.proveedor_horario.horario_actual().isoformat(timespec="seconds")
        )

        self.repositorio_imagenes.actualizar_imagenes_resenia(resenia)
        
        try:
            db.session.add(resenia_db)
            db.session.commit()
        except Exception as e:
            return False
        
        return True
    
    def buscar_resenia(self, id_usuario, id_juntada, id_negocio):
        resenia_db = ReseniaDB.query.filter_by(
            id_usuario=id_usuario,
            id_juntada=id_juntada,
            id_negocio=id_negocio
        ).first()


        if not resenia_db:
            return None
        
        resenia = Resenia(
            id_usuario=resenia_db.id_usuario, 
            id_juntada=resenia_db.id_juntada, 
            id_negocio=resenia_db.id_negocio, 
            valoracion=resenia_db.puntaje,
            comentario=resenia_db.resenia,
            fecha_publicacion=resenia_db.fecha_publicacion
        )
        imagenes_resenia = self.repositorio_imagenes.buscar_imagenes_en_resenia(resenia)
        for imagen in imagenes_resenia:
            resenia.agregar_imagen(imagen)
        
        return resenia

    def buscar_resenias_por_negocio(self, id_negocio):
        resenias_db = ReseniaDB.query.filter_by(id_negocio=id_negocio).all()
        resenias_db_con_usuario = []
        for resenia_db in resenias_db:
            usuario = RepositorioUsuario().buscar_nombre_por_id(resenia_db.id_usuario)
            if not usuario:
                raise Exception("Fallo interno al buscar las reseñas por negocio")
            # Importante: Vamos a tener que handlear el caso de que el usuario haya sido eliminado
            # cuando podamos borrar un usuario.
            resenias_db_con_usuario.append((resenia_db, usuario))
        
        resenias_con_usuario = []

        for resenia_db, usuario in resenias_db_con_usuario:
            resenia = Resenia(
                id_usuario=resenia_db.id_usuario, 
                id_juntada=resenia_db.id_juntada, 
                id_negocio=resenia_db.id_negocio, 
                valoracion=resenia_db.puntaje,
                comentario=resenia_db.resenia,
                fecha_publicacion=resenia_db.fecha_publicacion
            )
            imagenes_resenia = self.repositorio_imagenes.buscar_imagenes_en_resenia(resenia)
            for imagen in imagenes_resenia:
                resenia.agregar_imagen(imagen)
            
            resenias_con_usuario.append((resenia, usuario))
        
        return resenias_con_usuario
    
    def borrar_resenia(self, resenia):
        id_usuario = resenia.id_usuario
        id_juntada = resenia.id_juntada
        id_negocio = resenia.id_negocio

        ReseniaDB.query.filter_by(
            id_usuario=id_usuario,
            id_juntada=id_juntada,
            id_negocio=id_negocio
        ).delete()

        db.session.commit()

