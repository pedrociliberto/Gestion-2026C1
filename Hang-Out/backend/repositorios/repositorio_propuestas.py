from datetime import datetime
from models import db, PropuestaDB, VotacionDB
from repositorios.repositorio_votacion import RepositorioVotacion

class RepositorioPropuestas:

    def contar_postulaciones_usuario(self, id_juntada, id_usuario):
        """Cuenta cuántas propuestas activas tiene un usuario en una juntada."""
        return PropuestaDB.query.filter_by(
            id_juntada=id_juntada, # Este usuario para esta juntada
            id_usuario=id_usuario
        ).count()

    def existe_propuesta_identica(self, id_juntada, fecha_inicio, fecha_fin=None, id_negocio=None, lugar_personalizado=None):
        """
        Verifica si ya existe una propuesta idéntica activa.
        Mismo lugar, misma fecha y hora de inicio dentro de la misma juntada.
        """
        propuesta = PropuestaDB.query.filter_by(
            id_juntada=id_juntada,
            id_negocio=id_negocio,
            lugar_personalizado=lugar_personalizado,
            fecha_hora_inicio=fecha_inicio,
            fecha_hora_fin=fecha_fin
        )
        return len(propuesta.all()) > 0

    def guardar_propuesta(self, id_juntada, id_usuario, fecha_inicio, fecha_fin=None, id_negocio=None, lugar_personalizado=None, es_ganadora=False):
        """
        Persiste el objeto en la base de datos.
        """
        nueva_propuesta = PropuestaDB(
            id_juntada=id_juntada,
            id_usuario=id_usuario,
            id_negocio=id_negocio,
            lugar_personalizado=lugar_personalizado,
            fecha_hora_inicio=fecha_inicio,
            fecha_hora_fin=fecha_fin,
            es_ganadora=es_ganadora
        )
        
        try:
            db.session.add(nueva_propuesta)
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False
        
    def listar_propuestas_por_juntada(self, id_juntada):
        """
        Obtiene todas las propuestas asociadas a una juntada específica.
        
        **PRE**: Recibe el id de la juntada

        **POST**: Devuelve una tupla (propuestas, votaciones), donde:

        - propuestas: son las filas de propuestas (de tipo PropuestaDB)
        - votaciones: diccionario donde clave=id_propuesta, valor=cantidad_votos
        """
        propuestas = PropuestaDB.query.filter_by(id_juntada=id_juntada) \
                                      .order_by(PropuestaDB.fecha_hora_inicio.asc()) \
                                      .all()
        
        votos_por_propuesta = {}
        for propuesta in propuestas:
            cantidad_votos = RepositorioVotacion().contar_votaciones_postulacion(propuesta.id)
            votos_por_propuesta[propuesta.id] = cantidad_votos
        return propuestas, votos_por_propuesta

    def buscar_por_id(self, id_propuesta):
        """Busca una propuesta por su ID."""
        return PropuestaDB.query.get(id_propuesta)

    def listar_propuestas_por_negocio(self, id_negocio):
        """
        Obtiene todas las propuestas asociadas a un negocio registrado.
        """
        return PropuestaDB.query.filter_by(id_negocio=id_negocio) \
                              .order_by(PropuestaDB.fecha_hora_inicio.asc()) \
                              .all()

    def eliminar_propuesta(self, id_propuesta):
        """Elimina una propuesta específica por su ID."""
        propuesta = PropuestaDB.query.get(id_propuesta)
        if propuesta:
            try:
                VotacionDB.query.filter_by(id_propuesta=id_propuesta).delete()
                db.session.delete(propuesta)
                db.session.commit()
                return True
            except Exception as e:
                db.session.rollback()
                return False
        return False

    def eliminar_propuestas_por_juntada(self, id_juntada):
        """Elimina todas las propuestas y votaciones asociadas a una juntada."""
        propuestas = PropuestaDB.query.filter_by(id_juntada=id_juntada).all()
        if not propuestas:
            return True

        try:
            ids_propuestas = [propuesta.id for propuesta in propuestas]
            VotacionDB.query.filter(VotacionDB.id_propuesta.in_(ids_propuestas)).delete(synchronize_session=False)
            for propuesta in propuestas:
                db.session.delete(propuesta)
            db.session.commit()
            return True
        except Exception:
            db.session.rollback()
            return False
    
    def marcar_como_ganadora(self, id_propuesta):
        propuesta_ganadora = PropuestaDB.query.filter_by(id=id_propuesta).first()
        propuesta_ganadora.es_ganadora = True
        
        try:
            db.session.commit()
        except:
            db.session.rollback()
            return False
        
        return True
    
    def desmarcar_como_ganadora(self, id_propuesta):
        propuesta = PropuestaDB.query.filter_by(id=id_propuesta).first()
        propuesta.es_ganadora = False
        
        try:
            db.session.commit()
        except:
            db.session.rollback()
            return False
        
        return True
    
    def buscar_ganadora_de_juntada(self, id_juntada):
        return PropuestaDB.query.filter_by(id_juntada=id_juntada, es_ganadora=True).first()