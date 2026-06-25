from zoneinfo import ZoneInfo

from flask import Blueprint, request, jsonify
from datetime import datetime
from flask_jwt_extended import jwt_required, get_jwt_identity

import sys
sys.path.append("/backend")
from repositorios.repositorio_propuestas import RepositorioPropuestas
from repositorios.repositorio_negocio import RepositorioNegocio
from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_votacion import RepositorioVotacion

propuesta_bp = Blueprint('propuestas', __name__)
repositorio_propuestas = RepositorioPropuestas()
repositorio_negocio = RepositorioNegocio()
repositorio_usuario = RepositorioUsuario()
repositorio_votacion = RepositorioVotacion()

@propuesta_bp.route('/postular/<int:id_usuario>/<int:id_juntada>', methods=['GET'])
@jwt_required()
def visualizar_propuestas(id_usuario, id_juntada):
    """
    Visualiza todas las propuestas asociadas a una juntada específica.
    """
    id_token = int(get_jwt_identity())
    if id_token != id_usuario:
        return jsonify({"error": "No autorizado para ver las propuestas de este usuario"}), 403
    propuestas, votaciones = repositorio_propuestas.listar_propuestas_por_juntada(id_juntada)

    propuestas_serializadas = []
    for propuesta in propuestas:
        nombre_negocio = repositorio_negocio.obtener_negocio(propuesta.id_negocio).nombre if propuesta.id_negocio else None
        nombre_usuario = repositorio_usuario.buscar_por_id(propuesta.id_usuario).nombre_completo
        propuestas_serializadas.append({
            "id": propuesta.id,
            "id_usuario": propuesta.id_usuario,
            "id_negocio": propuesta.id_negocio if propuesta.id_negocio else None,
            "nombre_negocio": nombre_negocio,
            "nombre_usuario": nombre_usuario,
            "lugar_personalizado": propuesta.lugar_personalizado if propuesta.lugar_personalizado else None,
            "fecha_hora_inicio": propuesta.fecha_hora_inicio.isoformat(),
            "fecha_hora_fin": propuesta.fecha_hora_fin.isoformat() if propuesta.fecha_hora_fin else None,
            "cantidad_votos": votaciones[propuesta.id],
            "yo_vote": repositorio_votacion.existe_votacion_usuario(propuesta.id, id_usuario)
        })

    return jsonify({
        "propuestas": propuestas_serializadas
    }), 200

@propuesta_bp.route('/postular/<int:id_usuario>/<int:id_juntada>', methods=['POST'])
@jwt_required()
def postular_lugar(id_usuario, id_juntada):
    """
    Endpoint para que un usuario postule un lugar (negocio registrado o lugar personalizado) para una juntada específica.
    """
    id_token = int(get_jwt_identity())
    if id_token != id_usuario:
        return jsonify({"error": "No autorizado para realizar postulaciones en nombre de este usuario"}), 403
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    id_negocio = data.get('id_negocio')
    lugar_personalizado = data.get('lugar_personalizado')

    fecha_inicio_str = data.get('fecha_hora_inicio')
    fecha_fin_str = data.get('fecha_hora_fin') # Opcional

    if (not lugar_personalizado and not id_negocio) or not fecha_inicio_str:
        return jsonify({"error": "Faltan campos obligatorios (lugar o fecha de inicio)"}), 400

    # Parseo de fechas desde el formato ISO estándar que enviará Kotlin (yyyy-MM-dd'T'HH:mm:ss)
    try:
        fecha_inicio = datetime.fromisoformat(fecha_inicio_str)
        fecha_fin = datetime.fromisoformat(fecha_fin_str) if fecha_fin_str else None
    except ValueError:
        return jsonify({"error": "Formato de fecha inválido."}), 400

    # 1. Validación de lugar: Debe ser uno u otro, no ambos ni ninguno
    if (id_negocio and lugar_personalizado) or (not id_negocio and not lugar_personalizado):
        return jsonify({"error": "Debe proporcionar exactamente un lugar: o un negocio registrado o un lugar personalizado."}), 400

    # 2. Reglas de negocio: Validaciones temporales autónomas del objeto
    valido, mensaje_error = es_valida_temporalmente(fecha_inicio, fecha_fin)
    if not valido:
        return jsonify({"error": mensaje_error}), 400

    # 3. Reglas de negocio: Límite de 3 postulaciones por usuario en la juntada
    if repositorio_propuestas.contar_postulaciones_usuario(id_juntada, id_usuario) >= 3:
        return jsonify({"error": "Has alcanzado el límite máximo de 3 postulaciones para esta juntada."}), 400

    # 4. Reglas de negocio: Identificar y rechazar duplicados exactos
    if repositorio_propuestas.existe_propuesta_identica(id_juntada, fecha_inicio, fecha_fin, id_negocio,lugar_personalizado):
        return jsonify({"error": "Ya existe una propuesta idéntica (mismo lugar, fecha y hora) activa dentro de la misma juntada."}), 400

    # 5. Éxito: Almacenamiento y vinculación
    try:
        nueva_propuesta = repositorio_propuestas.guardar_propuesta(id_juntada, id_usuario, fecha_inicio, fecha_fin, id_negocio, lugar_personalizado)
        return jsonify({
            "message": "Propuesta almacenada con éxito, lista para la etapa de votación."
        }), 201
    except Exception as e:
        return jsonify({"error": f"Error interno al guardar la propuesta: {str(e)}"}), 500
    
@propuesta_bp.route('/eliminar/<int:id_usuario>/<int:id_propuesta>', methods=['DELETE'])
@jwt_required()
def eliminar_propuesta(id_usuario, id_propuesta):
    """
    Endpoint para que un usuario elimine una propuesta que haya realizado.
    """
    id_token = int(get_jwt_identity())
    if id_token != id_usuario:
        return jsonify({"error": "No autorizado para eliminar propuestas en nombre de este usuario"}), 403

    propuesta = repositorio_propuestas.buscar_por_id(id_propuesta)
    if not propuesta:
        return jsonify({"error": "Propuesta no encontrada"}), 404

    if propuesta.id_usuario != id_usuario:
        return jsonify({"error": "No autorizado para eliminar esta propuesta"}), 403

    try:
        repositorio_propuestas.eliminar_propuesta(id_propuesta)
        return jsonify({"message": "Propuesta eliminada con éxito."}), 200
    except Exception as e:
        return jsonify({"error": f"Error interno al eliminar la propuesta: {str(e)}"}), 500

def es_valida_temporalmente(fecha_hora_inicio, fecha_hora_final):
    """
    Valida que las restricciones de tiempo se cumplan.
    Permite pasar 'momento_actual' para facilitar el testing.
    """
    fecha_hora_actual = datetime.now(ZoneInfo("America/Argentina/Buenos_Aires")).replace(tzinfo=None)

    if fecha_hora_inicio < fecha_hora_actual:
        return False, "La fecha y hora de inicio no pueden ser anteriores al momento actual."

    if fecha_hora_final and (fecha_hora_final <= fecha_hora_inicio):
        return False, "El horario de finalización debe ser posterior al horario de inicio."
    return True, ""
