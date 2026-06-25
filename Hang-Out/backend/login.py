from flask import Blueprint, request, jsonify
from werkzeug.security import check_password_hash
from models import db, UsuarioDB
from sqlalchemy import or_
from flask_jwt_extended import create_access_token

login_bp = Blueprint('login', __name__)

@login_bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    # 1. Validar campos vacíos
    # 'credencial' puede ser el nombre de usuario o el email
    credencial_ingresada = data.get('usuario', '').strip() 
    password_ingresada = data.get('password', '')

    if not credencial_ingresada or not password_ingresada:
        return jsonify({"error": "Debe completar el usuario/email y la contraseña"}), 400

    # 2. Verificar si el usuario existe por 'usuario' O por 'email'
    # Usamos or_ para que coincida con cualquiera de las dos columnas
    user = UsuarioDB.query.filter(
        or_(
            UsuarioDB.usuario == credencial_ingresada,
            UsuarioDB.email == credencial_ingresada.lower() # Normalizamos a minúsculas como en el registro
        )
    ).first()

    if not user:
        return jsonify({"error": "Las credenciales no corresponden a un usuario registrado"}), 401

    # 3. Validar contraseña coincidente
    if not check_password_hash(user.password_hash, password_ingresada):
        return jsonify({"error": "Contraseña incorrecta"}), 401

    token_acceso = create_access_token(identity=str(user.id))

    # 4. Éxito en el login
    return jsonify({
        "message": "Inicio de sesión exitoso",
        "token": token_acceso,
        "data": {
            "id": user.id,
            "nombre": user.nombre_completo,
            "usuario": user.usuario,
            "email": user.email,
            "es_cuenta_personal": user.es_cuenta_personal
        }
    }), 200