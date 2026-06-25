from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from model.votaciones import votar_propuestas
from model.excepciones import ExcepcionSistema
from repositorios.repositorio_juntada import RepositorioJuntada
from repositorios.repositorio_votacion import RepositorioVotacion
from init import PROVEEDOR_HORARIO

votacion_bp = Blueprint('votacion', __name__)

@votacion_bp.post("/votacion/<int:id_usuario>/<int:id_juntada>")
@jwt_required()
def actualizar_votacion(id_usuario, id_juntada):
    """
    Espera en el body un JSON con el formato {<id_propuesta>: <bool>, ...} con cada propuesta,
    donde el valor indica si se voto esa propuesta o no.
    """
    id_token = int(get_jwt_identity())
    if id_token != id_usuario:
        return jsonify({"error": "No autorizado para realizar postulaciones en nombre de este usuario"}), 403
    
    repositorio_juntada = RepositorioJuntada(PROVEEDOR_HORARIO)
    juntada = repositorio_juntada.buscar_por_id(id_juntada)
    if juntada and juntada.estado() == "CONFIRMADA":
        return jsonify({"error": "No se pueden votar propuestas de una juntada con votación cerrada"}), 400

    try:
        propuestas_con_voto = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400
    
    repositorio_votacion = RepositorioVotacion()
    try:
        votar_propuestas(id_usuario, propuestas_con_voto, repositorio_votacion)
        return jsonify({"mensaje": "Votación actualizada"}), 201
    except ExcepcionSistema:
        return jsonify({"error": "Ocurrio un error actualizando la votación"}), 500