# Repositorio que se encarga de manejar las operaciones en la tabla "Participante"

import sys
sys.path.append("/backend")
from models import ParticipanteDB, JuntadaDB, UsuarioDB, db

class RepositorioParticipante:
     def guardar(self, id_juntada,id_usuario):
          nuevo_participante = ParticipanteDB(
               id_juntada=id_juntada,
               id_usuario=id_usuario
          )
          db.session.add(nuevo_participante)
          db.session.commit()

     def ya_es_participante(self,id_juntada, id_usuario):
          organizador = JuntadaDB.query.filter_by(
               id=id_juntada,
               id_organizador=id_usuario).first()
          
          participante = ParticipanteDB.query.filter_by(
               id_juntada=id_juntada,
               id_usuario=id_usuario).first()
          
          return (organizador is not None) or (participante is not None) # Devuelve False si no encontró nada 

     def obtener_nombres_participantes(self, id_juntada):
          participantes = db.session.query(UsuarioDB.nombre_completo).join(
               ParticipanteDB, UsuarioDB.id == ParticipanteDB.id_usuario
          ).filter(ParticipanteDB.id_juntada == id_juntada).all()
          
          return [p[0] for p in participantes]

     def obtener_ids_participantes(self, id_juntada):
          participantes = ParticipanteDB.query.filter_by(id_juntada=id_juntada).all()
          return [p.id_usuario for p in participantes]
     
     def eliminar(self, id_juntada, id_usuario):
          participante = ParticipanteDB.query.filter_by(
               id_juntada=id_juntada,
               id_usuario=id_usuario).first()
          
          if participante:
               db.session.delete(participante)
               db.session.commit()

     def eliminar_por_juntada(self, id_juntada):
          try:
               ParticipanteDB.query.filter_by(id_juntada=id_juntada).delete(synchronize_session=False)
               db.session.commit()
               return True
          except Exception:
               db.session.rollback()
               return False