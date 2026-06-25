# Repositorio que se encarga de manejar las operaciones en la tabla "Juntada"

from sqlalchemy import case
from datetime import datetime
from zoneinfo import ZoneInfo
import sys

sys.path.append("/backend")
from models import UsuarioDB, JuntadaDB, ParticipanteDB, PropuestaDB, db
from repositorios.repositorio_propuestas import RepositorioPropuestas
from model.juntada import Juntada
from proveedores.proveedor_codigos import ProveedorCodigos

class RepositorioJuntada:
    def __init__(self, proveedor_horario):
        self.proveedor_horario = proveedor_horario

    def guardar(self, juntada):
        juntada_db = JuntadaDB(
            titulo=juntada.titulo(),
            codigo=juntada.codigo(),
            id_organizador=juntada.organizador.id,
            estado=juntada.estado(),
        )
        db.session.add(juntada_db)
        db.session.commit()
        juntada.id = juntada_db.id
    
    def buscar_por_id(self, id_juntada):
        juntada_db = JuntadaDB.query.get(id_juntada)
        if not juntada_db: return None

        organizador = UsuarioDB.query.get(juntada_db.id_organizador)
        propuesta_ganadora = RepositorioPropuestas().buscar_ganadora_de_juntada(juntada_db.id)

        if propuesta_ganadora and propuesta_ganadora.fecha_hora_inicio < self.proveedor_horario.horario_actual():
            juntada_db.estado = "PASADA"
            try:
                db.session.commit()
            except:
                db.session.rollback()
                return None

        return Juntada(
            str(juntada_db.titulo),
            str(juntada_db.codigo),
            organizador,
            ProveedorCodigos(),
            RepositorioJuntada(self.proveedor_horario),
            juntada_db.id,
            juntada_db.estado,
            propuesta_ganadora.id if propuesta_ganadora else None
        )
    
    def buscar_por_codigo(self, codigo):
        juntada_db = JuntadaDB.query.filter_by(codigo=codigo).first()
        if not juntada_db: return None

        organizador = UsuarioDB.query.get(juntada_db.id_organizador)

        return Juntada(
            str(juntada_db.titulo),
            str(juntada_db.codigo),
            organizador,
            ProveedorCodigos(),
            RepositorioJuntada(self.proveedor_horario),
            juntada_db.id,
            juntada_db.estado,
        )
    
    def buscar_por_usuario(self, usuario_id, repo_propuestas):
        """
        Recupera las juntadas del usuario, gestiona la caducidad temporal 
        comparando con la hora de Buenos Aires y devuelve las listas limpias.
        """
        try:
            creadas_db = JuntadaDB.query.filter(JuntadaDB.id_organizador == usuario_id).all()
            invitado_db = JuntadaDB.query.join(ParticipanteDB).filter(ParticipanteDB.id_usuario == usuario_id).all()

            ahora_ba = self.proveedor_horario.horario_actual()

            def procesar_y_mapear(j, rol_usuario):
                estado_actual = getattr(j, 'estado', 'PENDIENTE')
                fecha_ini_iso = None
                fecha_fin_iso = None
                dt_sort = None
                propuesta_ganadora = RepositorioPropuestas().buscar_ganadora_de_juntada(j.id)

                if (estado_actual == "CONFIRMADA" or estado_actual == "PASADA") and propuesta_ganadora:
                    propuesta = repo_propuestas.buscar_por_id(propuesta_ganadora.id)
                    if propuesta and propuesta.fecha_hora_inicio:
                        dt_sort = propuesta.fecha_hora_inicio.replace(tzinfo=None)
                        if estado_actual == "CONFIRMADA" and dt_sort < ahora_ba:
                            estado_actual = "PASADA"
                            j.estado = "PASADA"
                            db.session.commit()
                        fecha_ini_iso = propuesta.fecha_hora_inicio.isoformat()
                        fecha_fin_iso = propuesta.fecha_hora_fin.isoformat() if propuesta.fecha_hora_fin else None
                return {
                    "id": j.id,
                    "titulo": j.titulo,
                    "codigo": j.codigo,
                    "id_organizador": int(j.id_organizador),
                    "estado": estado_actual,
                    "rol": rol_usuario,
                    "id_propuesta_ganadora": getattr(j, 'id_propuesta_ganadora', None),
                    "fecha_hora_inicio": fecha_ini_iso,
                    "fecha_hora_fin": fecha_fin_iso,
                    "_dt_sort": dt_sort
                }

            lista_juntadas = []
            for j in creadas_db:
                lista_juntadas.append(procesar_y_mapear(j, "Creador"))
            for j in invitado_db:
                lista_juntadas.append(procesar_y_mapear(j, "Invitado"))

            def criterio_ordenamiento(x):
                estado = x["estado"]
                fecha = x["_dt_sort"]
                if estado == "CONFIRMADA":
                    return (1, fecha.timestamp() if fecha else 0)
                elif estado == "PASADA":
                    return (2, -(fecha.timestamp() if fecha else 0))
                else:
                    return (3, 0)

            lista_juntadas.sort(key=criterio_ordenamiento)
            for item in lista_juntadas:
                item.pop("_dt_sort", None)
            return lista_juntadas
        except Exception as e:
            print(f"Error en RepositorioJuntada.buscar_por_usuario: {e}")
            db.session.rollback()
            return []
    
    def actualizar_estado_votacion(self, juntada_id, juntada_estado, id_propuesta_ganadora=None):
        """Impacta los cambios de cierre o reapertura de votación en la base de datos"""
        juntada_db = JuntadaDB.query.get(juntada_id)
        if juntada_db:
            juntada_db.estado = juntada_estado
            db.session.commit()

        if id_propuesta_ganadora:
            repositorio_propuestas = RepositorioPropuestas()
            repositorio_propuestas.marcar_como_ganadora(id_propuesta_ganadora)

    def actualizar_organizador(self, juntada_id, id_organizador):
        """Actualiza el organizador de una juntada en la base de datos"""
        juntada_db = JuntadaDB.query.get(juntada_id)
        if juntada_db:
            juntada_db.id_organizador = id_organizador
            db.session.commit()

    def eliminar(self, id_juntada):
        juntada_db = JuntadaDB.query.get(id_juntada)
        if not juntada_db:
            return False

        try:
            db.session.delete(juntada_db)
            db.session.commit()
            return True
        except Exception:
            db.session.rollback()
            return False