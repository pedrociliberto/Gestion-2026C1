
from models import db, PropuestaDB, VotacionDB

class RepositorioVotacion:
    def borrar_votacion_usuario(self, id_usuario, id_propuesta):
        """
        **PRE**: recibe el id de un usuario y el id de una propuesta
        
        **POST**: borra la fila con ese id_usuario y ese id_propuesta (i.e borra la votación)
        """
        votacion = VotacionDB.query.filter_by(id_usuario=id_usuario, id_propuesta = id_propuesta).first()
        if not votacion:
            return False
        
        try:
            db.session.delete(votacion)
            db.session.commit()
        except:
            db.session.rollback()
            return False
        
        return True

    def guardar_votacion_usuario(self, id_usuario, id_propuesta):
        """
        **PRE**: recibe el id de un usuario y el id de una propuesta
        
        **POST**: guarda la fila con ese id_usuario y ese id_propuesta (i.e agrega esa votación)
        """
        votacion = VotacionDB(id_usuario=id_usuario, id_propuesta=id_propuesta)
        try:
            db.session.add(votacion)
            db.session.commit()
        except:
            db.session.rollback()
            return False
        
        return True

    def contar_votaciones_postulacion(self, id_postulacion):
        """Cuenta cuántas votaciones tiene una propuesta."""
        return VotacionDB.query.filter_by(id_propuesta=id_postulacion).count()
    
    def existe_votacion_usuario(self, id_propuesta, id_usuario):
        """Verifica si un usuario ya votó por una propuesta específica."""
        votacion = VotacionDB.query.filter_by(id_propuesta=id_propuesta, id_usuario=id_usuario).first()
        return votacion is not None