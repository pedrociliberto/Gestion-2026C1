from datetime import datetime
from zoneinfo import ZoneInfo
from models import JuntadaDB
from repositorios.repositorio_propuestas import RepositorioPropuestas


def obtener_apariciones_negocio(id_negocio, clase_repositorio_propuestas=None, clase_repositorio_juntada=None):
    """
    Obtiene los datos de apariciones de un negocio en propuestas de juntadas.
    """
    propuestas_repo_class = clase_repositorio_propuestas or RepositorioPropuestas
    juntada_db_class = clase_repositorio_juntada or JuntadaDB

    propuestas = propuestas_repo_class().listar_propuestas_por_negocio(id_negocio)
    ahora_ba = datetime.now(ZoneInfo("America/Argentina/Buenos_Aires")).replace(tzinfo=None)

    apariciones_serializadas = []
    ganadoras_pasadas = []
    ganadoras_futuras = []
    cantidad_en_votacion = 0
    cantidad_no_ganadoras = 0

    for propuesta in propuestas:
        juntada = juntada_db_class.query.get(propuesta.id_juntada)
        en_votacion = False

        if propuesta.es_ganadora is False:
            if juntada and getattr(juntada, 'estado', None) == 'PENDIENTE':
                en_votacion = True
                cantidad_en_votacion += 1
            else:
                cantidad_no_ganadoras += 1

        aparicion = {
            "id_propuesta": propuesta.id,
            "id_juntada": propuesta.id_juntada,
            "fecha_hora_inicio": propuesta.fecha_hora_inicio.isoformat() if propuesta.fecha_hora_inicio else None,
            "fecha_hora_fin": propuesta.fecha_hora_fin.isoformat() if propuesta.fecha_hora_fin else None,
            "es_ganadora": bool(propuesta.es_ganadora),
            "en_votacion": en_votacion,
        }
        apariciones_serializadas.append(aparicion)

        if propuesta.es_ganadora:
            if propuesta.fecha_hora_inicio and propuesta.fecha_hora_inicio < ahora_ba:
                ganadoras_pasadas.append(aparicion)
            else:
                ganadoras_futuras.append(aparicion)

    return {
        "id_negocio": id_negocio,
        "cantidad_apariciones_total": len(propuestas),
        "cantidad_apariciones_ganadoras": len(ganadoras_pasadas) + len(ganadoras_futuras),
        "cantidad_apariciones_no_ganadoras": cantidad_no_ganadoras,
        "cantidad_apariciones_en_votacion": cantidad_en_votacion,
        "apariciones": apariciones_serializadas,
        "propuestas_ganadoras": {
            "cantidad_total": len(ganadoras_pasadas) + len(ganadoras_futuras),
            "cantidad_pasadas": len(ganadoras_pasadas),
            "cantidad_futuras": len(ganadoras_futuras),
            "pasadas": ganadoras_pasadas,
            "futuras": ganadoras_futuras,
        },
    }
