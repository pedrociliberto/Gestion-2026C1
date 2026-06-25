from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from repositorios.repositorio_beneficio import RepositorioBeneficio
from proveedores.proveedor_pago import ProveedorPago
from models import BeneficioDB, db
repositorio_beneficio = RepositorioBeneficio()
proveedor_pago = ProveedorPago()

beneficios_bp = Blueprint("beneficios", __name__)

NOMBRES_BENEFICIOS = [
    {
        "nombre": "Subscripción a posicionamiento en búsquedas",
        "descripcion": "Tu negocio aparecerá destacado en los resultados de búsqueda dentro de la aplicación, aumentando su visibilidad y atrayendo a más clientes potenciales."
    },
    { 
        "nombre": "Subscripción a recomendaciones en notificaciones Pop-Up",
        "descripcion": "Los usuarios recibirán recomendaciones de tu negocio en notificaciones Pop-Up."
    }
]

def agregar_beneficios():
    """
    Agrega los beneficios predefinidos a la base de datos si no existen. Esto se ejecuta al iniciar la aplicación para asegurar que los beneficios estén disponibles para su asignación a los negocios.
    """
    beneficios = BeneficioDB.query.all()
    if beneficios:
        return 
    for beneficio in NOMBRES_BENEFICIOS:
        nuevo_beneficio = BeneficioDB(nombre=beneficio["nombre"], descripcion=beneficio["descripcion"])
        db.session.add(nuevo_beneficio)
    db.session.commit()

@beneficios_bp.get("/beneficios/<int:id_usuario>")
@jwt_required()
def obtener_beneficios(id_usuario):
    _verificar_token(id_usuario)

    activos = repositorio_beneficio.listar_activos_de_usuario(id_usuario)
    if not activos:
        return jsonify({
            "activos": [],
            "mensaje": "No hay ningún beneficio activo"
        }), 200

    return jsonify({
        "activos": [{"id": b.id, "nombre": b.nombre, "descripcion": b.descripcion} for b in activos]
    }), 200

@beneficios_bp.get("/beneficios/<int:id_usuario>/disponibles")
@jwt_required()
def obtener_beneficios_disponibles(id_usuario):
    _verificar_token(id_usuario)

    disponibles = repositorio_beneficio.listar_disponibles_para_usuario(id_usuario)
    return jsonify({
        "disponibles": [{"id": b.id, "nombre": b.nombre, "descripcion": b.descripcion} for b in disponibles]
    }), 200

@beneficios_bp.post("/beneficios/<int:id_usuario>/suscribir")
@jwt_required()
def suscribir_beneficio(id_usuario):
    _verificar_token(id_usuario)

    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    id_beneficio = data.get("id_beneficio")
    numero       = data.get("numero_tarjeta", "")
    mes          = data.get("mes_vencimiento")
    anio         = data.get("anio_vencimiento")
    cvv          = data.get("cvv", "")
    nombre       = data.get("nombre_titular", "")

    if not id_beneficio:
        return jsonify({"error": "Debe indicar el beneficio a contratar"}), 400

    try:
        proveedor_pago.validar_tarjeta(numero, mes, anio, cvv, nombre)
    except ValueError as e:
        return jsonify({"error": str(e)}), 400

    # Los datos de tarjeta se usan solo para el cobro y se descartan
    if not proveedor_pago.cobrar(numero, mes, anio, cvv):
        return jsonify({"error": "El pago falló. Verificá los datos de tu tarjeta e intentá nuevamente"}), 402

    if not repositorio_beneficio.activar(id_usuario, id_beneficio):
        return jsonify({"error": "No se pudo activar el beneficio"}), 500

    return jsonify({"mensaje": "¡Tu beneficio ya está activo!"}), 200

@beneficios_bp.post("/beneficios/<int:id_usuario>/desactivar")
@jwt_required()
def desactivar_beneficio(id_usuario):
    _verificar_token(id_usuario)
    
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    id_beneficio = data.get("id_beneficio")
    if not id_beneficio:
        return jsonify({"error": "Debe indicar el beneficio a desactivar"}), 400

    if not repositorio_beneficio.desactivar(id_usuario, id_beneficio):
        return jsonify({"error": "No se pudo desactivar el beneficio"}), 500

    return jsonify({"mensaje": "El beneficio ha sido desactivado correctamente."}), 200

def _verificar_token(id_usuario):
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para realizar la operación."}), 403
    except Exception:
        return jsonify({"error": "Token inválido."}), 401