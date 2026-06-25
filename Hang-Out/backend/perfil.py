import re
from flask import Blueprint, request, jsonify
from model import usuario
from models import db, UsuarioDB
from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_notificacion import RepositorioNotificacion
from flask_jwt_extended import jwt_required, get_jwt_identity

perfil_bp = Blueprint('perfil', __name__)

repositorio_usuario = RepositorioUsuario()
repositorio_notificacion = RepositorioNotificacion()

@perfil_bp.route('/perfil/modificar/<int:id_usuario>', methods=['PUT'])
def modificar_datos(id_usuario):
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    usuario_actual = UsuarioDB.query.get(id_usuario)
    if not usuario_actual:
        return jsonify({"error": "Usuario no encontrado."}), 404

    nuevo_nombre = data.get('nombre_completo')
    nuevo_user = data.get('usuario')
    nuevo_email = data.get('email')

    if nuevo_nombre is None and nuevo_user is None and nuevo_email is None:
        return jsonify({"error": "No se enviaron datos para modificar."}), 400

    cambios = {}

    if nuevo_nombre is not None:
        nombre_strip = str(nuevo_nombre).strip()
        if not nombre_strip:
            return jsonify({"error": "El nombre completo no puede quedar vacío."}), 400
        if nombre_strip != usuario_actual.nombre_completo:
            cambios['nombre_completo'] = nombre_strip

    if nuevo_user is not None:
        user_strip = str(nuevo_user).strip()
        if not user_strip:
            return jsonify({"error": "El nombre de usuario no puede quedar vacío."}), 400
        if user_strip != usuario_actual.usuario:
            user_en_uso = UsuarioDB.query.filter_by(usuario=user_strip).first()
            if user_en_uso:
                return jsonify({"error": "El nombre de usuario ya está en uso."}), 400
            cambios['usuario'] = user_strip

    if nuevo_email is not None:
        email_strip = str(nuevo_email).strip().lower()
        if not email_strip:
            return jsonify({"error": "El correo electrónico no puede quedar vacío."}), 400
        if email_strip != usuario_actual.email:
            if not re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email_strip):
                return jsonify({"error": "Correo electrónico inválido."}), 400
            email_en_uso = UsuarioDB.query.filter_by(email=email_strip).first()
            if email_en_uso:
                return jsonify({"error": "El correo electrónico ya está registrado."}), 400
            cambios['email'] = email_strip

    if not cambios:
        return jsonify({"mensaje": "No se detectaron cambios nuevos.", "data": usuario_actual.to_dict()}), 200

    try:
        for campo, valor in cambios.items():
            setattr(usuario_actual, campo, valor)    
        db.session.commit() 
        return jsonify({"mensaje": "Datos actualizados correctamente."}), 200

    except Exception as e:
        db.session.rollback()
        return jsonify({"error": f"Error al modificar usuario: {str(e)}"}), 500

@perfil_bp.route('/perfil/<int:id_usuario>', methods=['GET'])
def obtener_datos_usuario(id_usuario):
    try:
        usuario = repositorio_usuario.buscar_por_id(id_usuario)
        if not usuario:
            return jsonify({"error": "Usuario no encontrado."}), 404
        
        return jsonify({
            "id": usuario.id,
            "nombre_completo": usuario.nombre_completo,
            "usuario": usuario.usuario,
            "email": usuario.email
        }), 200
    except Exception as e:
        return jsonify({"error": f"Error al obtener datos del usuario: {str(e)}"}), 500
    
@perfil_bp.route('/perfil/notificaciones/<int:id_usuario>', methods=['GET'])
@jwt_required()
def procesar_notificaciones_pendientes(id_usuario):
    try:
        id_usuario = int(get_jwt_identity())
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
    
    notificaciones = repositorio_notificacion.obtener_pendientes(id_usuario)
    res = []
    for n in notificaciones:
        res.append({
            "id": n.id,
            "titulo": n.titulo,
            "descripcion": n.descripcion
        })
    return jsonify({"notificaciones": res}), 200


@perfil_bp.route(
    '/perfil/notificaciones/<int:id_notificacion>/leida',
    methods=['POST']
)
@jwt_required()
def marcar_notificacion_leida(id_notificacion):
    try:
        id_usuario = int(get_jwt_identity())
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
   
    repositorio_notificacion.marcar_vista(id_usuario, id_notificacion)
    return jsonify({
        "mensaje": "Notificación marcada como leída"
    }), 200


@perfil_bp.route(
    '/perfil/notificaciones/<int:id_notificacion>',
    methods=['DELETE']
)
@jwt_required()
def borrar_notificacion(id_notificacion):
    try:
        id_usuario = int(get_jwt_identity())
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401

    repositorio_notificacion.marcar_vista(id_usuario, id_notificacion)
    return jsonify({"mensaje": "Notificación borrada"}), 200