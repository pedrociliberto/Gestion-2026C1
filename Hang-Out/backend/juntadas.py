from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity, verify_jwt_in_request

from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_juntada import RepositorioJuntada
from repositorios.repositorio_participante import RepositorioParticipante
from repositorios.repositorio_negocio import RepositorioNegocio
from repositorios.repositorio_filtro import RepositorioFiltro
from repositorios.repositorio_propuestas import RepositorioPropuestas
from repositorios.repositorio_resenia import RepositorioResenia
from init import PROVEEDOR_HORARIO, crear_sistema_con_repositorios

from proveedores.proveedor_codigos import ProveedorCodigos

from model.excepciones import ExcepcionSistema  
from model.sistema import Sistema

juntada_bp = Blueprint('juntada', __name__)

sistema = crear_sistema_con_repositorios()

@juntada_bp.post("/juntada")
def crear_juntada():
    try:
        data = request.get_json()

        juntada = sistema.crear_juntada(
            data.get("titulo"), 
            data.get("codigo"), 
            data.get("id_organizador")
        )

        return jsonify({
            "mensaje": "La juntada se creo exitosamente",
            "juntada": {
                "titulo": juntada.titulo(),
                "codigo": juntada.codigo(),
                "organizador": juntada.organizador.nombre_completo
            }
        }), 201
    except ExcepcionSistema as e:
        return jsonify({
            "error": e.mensaje
        }), 400

@juntada_bp.post("/juntada/unirse")
def unirse_a_juntada():
    try:
        data = request.get_json()
        id_usuario = data.get("id_usuario")
        codigo = data.get("codigo")

        if not id_usuario or not codigo:
            return jsonify({"error":"Se debe enviar id_usuario y codigo"}),400
        
        sistema = crear_sistema_con_repositorios()
        juntada = sistema.unirse_a_juntada(codigo, id_usuario)

        return jsonify({
            "mensaje":"Te uniste a la juntada exitosamente",
            "juntada":{
                "titulo": juntada.titulo(),
                "codigo": juntada.codigo(),
                "organizador": juntada.organizador.nombre_completo
            }
        }), 200

    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}),400
    
@juntada_bp.post("/juntada/salir") # Copio estructura de unirse a juntada.
def salir_de_juntada():
    """
    Permite a un usuario salir de una juntada a la que se había unido previamente.
    """
    try:
        data = request.get_json()
        id_usuario = data.get("id_usuario")
        id_juntada = data.get("id_juntada")

        if not id_usuario or not id_juntada:
            return jsonify({"error":"Se debe enviar id_usuario y id_juntada"}),400
        
        sistema = crear_sistema_con_repositorios()
        sistema.salir_de_juntada(id_juntada, id_usuario)

        return jsonify({
            "mensaje":"Saliste de la juntada exitosamente",
        }), 200

    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}),400

@juntada_bp.route('/juntadas/<int:usuario_id>', methods=['GET'])
@jwt_required()
def obtener_juntadas(usuario_id):
    try:
        id_token = int(get_jwt_identity())
        if id_token != usuario_id:
            return jsonify({"error": "No autorizado para acceder a estas juntadas"}), 403
        sistema = crear_sistema_con_repositorios()
        lista_juntadas = sistema.listar_juntadas_usuario(usuario_id, RepositorioPropuestas())
        return jsonify(lista_juntadas), 200

    except Exception as e:
        return jsonify({"error": "Error al obtener el listado"}), 500

@juntada_bp.route('/juntada/<int:juntada_id>', methods=['GET'])
def obtener_juntada(juntada_id):
    try:
        sistema = crear_sistema_con_repositorios()
        detalle = sistema.obtener_detalle_completo(juntada_id)
        if not detalle:
            return jsonify({"error": "Juntada no encontrada"}), 404
        return jsonify(detalle), 200
    
    except Exception as e:
        print(str(e))
        return jsonify({"error": str(e)}), 500
    
@juntada_bp.post("/juntada/<int:id_juntada>/cerrar")
@jwt_required()
def cerrar_votaciones(id_juntada):
    try:
        id_usuario_autenticado = int(get_jwt_identity())
        
        repo_juntada = RepositorioJuntada(PROVEEDOR_HORARIO)
        repo_propuestas = RepositorioPropuestas()
        repo_negocio = RepositorioNegocio()
        
        juntada = repo_juntada.buscar_por_id(id_juntada)
        if not juntada:
            return jsonify({"error": "Juntada no encontrada"}), 404
            
        propuesta_ganadora, codigo_alerta = juntada.cerrar_votacion(
            id_usuario_autenticado, 
            repo_propuestas, 
            repo_negocio
        )
        
        repo_juntada.actualizar_estado_votacion(
            juntada.id, 
            juntada.estado(), 
            juntada.id_propuesta_ganadora()
        )
        
        return jsonify({
            "mensaje": "Votación cerrada con éxito. Juntada en estado confirmado.",
            "codigo_alerta_horario": codigo_alerta,
            "propuesta_ganadora": int(propuesta_ganadora.id) if propuesta_ganadora else None
        }), 200

    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@juntada_bp.post("/juntada/<int:id_juntada>/abrir")
@jwt_required()
def abrir_votaciones(id_juntada):
    try:
        id_usuario_autenticado = int(get_jwt_identity())
        repo_juntada = RepositorioJuntada(PROVEEDOR_HORARIO)
        
        juntada = repo_juntada.buscar_por_id(id_juntada)
        if not juntada:
            return jsonify({"error": "Juntada no encontrada"}), 404
        
        RepositorioPropuestas().desmarcar_como_ganadora(juntada.id_propuesta_ganadora())
            
        juntada.abrir_votacion(id_usuario_autenticado)
        
        repo_juntada.actualizar_estado_votacion(
            juntada.id, 
            juntada.estado(), 
            juntada.id_propuesta_ganadora()
        )
        
        return jsonify({"mensaje": "La votación ha sido reabierta exitosamente."}), 200
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@juntada_bp.delete("/juntada/<int:id_juntada>")
@jwt_required()
def eliminar_juntada(id_juntada):
    """
    Permite a un usuario eliminar una juntada.
    """
    try:
        id_usuario = int(get_jwt_identity())
        sistema = crear_sistema_con_repositorios()
        sistema.eliminar_juntada(id_juntada, id_usuario)

        return jsonify({
            "mensaje": "Juntada eliminada exitosamente",
        }), 200

    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400