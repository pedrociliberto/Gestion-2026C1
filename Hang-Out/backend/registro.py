import re
from flask import Blueprint, request, jsonify
from werkzeug.security import generate_password_hash
from models import db, UsuarioDB

registro_bp = Blueprint('auth', __name__)

@registro_bp.route('/registro', methods=['POST'])
def registro():
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    requerido = ['nombre_completo', 'usuario', 'email', 'password', 'password_confirm', 'es_cuenta_personal']
    for campo in requerido:
        if campo not in data or not str(data.get(campo)).strip():
            return jsonify({"error": f"Debe completar todos los campos para poder registrarse"}), 400

    nombre_completo = data.get('nombre_completo').strip()
    usuario = data.get('usuario').strip()
    email = data.get('email').strip().lower()
    password = data.get('password')
    password_confirm = data.get('password_confirm')
    es_cuenta_personal = data.get('es_cuenta_personal')

    # Validación de email:
    if not re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email):
        return jsonify({"error": "Correo electrónico inválido."}), 400

    # Chequeo de unicidad:
    if UsuarioDB.query.filter_by(email=email).first():
        return jsonify({"error": "El correo electrónico ya está registrado."}), 400
    if UsuarioDB.query.filter_by(usuario=usuario).first():
        return jsonify({"error": "El nombre de usuario ya está en uso."}), 400

    # Confirmación de contraseña:
    if password != password_confirm:
        return jsonify({"error": "La confirmación de la contraseña no coincide."}), 400

    valid, msg = _validar_password(password)
    if not valid:
        return jsonify({"error": msg}), 400

    # Todo bien -> se crea el usuario
    try:
        pw_hash = generate_password_hash(password)
        nuevo = UsuarioDB(
            nombre_completo=nombre_completo,
            usuario=usuario,
            email=email,
            password_hash=pw_hash,
            es_cuenta_personal=es_cuenta_personal
        )
        db.session.add(nuevo)
        db.session.commit()
        return jsonify({"message": "Usuario creado correctamente."}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": f"Error al crear usuario: {str(e)}"}), 500

def _validar_password(pw: str):
    if not pw:
        return False, "La contraseña no puede estar vacía."
    if len(pw) < 8:
        return False, "La contraseña debe tener al menos 8 caracteres."
    if len(pw) > 64:
        return False, "La contraseña no puede tener más de 64 caracteres."
    if not re.search(r"[A-Z]", pw):
        return False, "La contraseña debe tener al menos una letra mayúscula."
    if not re.search(r"[a-z]", pw):
        return False, "La contraseña debe tener al menos una letra minúscula."
    if not re.search(r"[0-9]", pw):
        return False, "La contraseña debe tener al menos un número."
    if not re.search(r"[@#$%*\!\?\-_=+:/\\\\|\(\)\[\]{}.,;]", pw):
        return False, "La contraseña debe tener al menos un carácter especial."
    return True, None