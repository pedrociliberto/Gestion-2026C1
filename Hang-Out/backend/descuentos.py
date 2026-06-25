from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity

from model.excepciones import ExcepcionSistema
from repositorios.repositorio_descuentos import RepositorioDescuento
from model.descuento import validar_y_crear_descuento, validar_y_borrar_descuento

descuentos_bp = Blueprint("descuentos", __name__)
repositorio_descuento = RepositorioDescuento()

def _serializar_descuento(d):
    return {
        "id": d.id,
        "id_negocio": d.id_negocio,
        "descripcion": d.descripcion,
        "porcentaje": d.porcentaje,
        "monto": d.monto,
        "codigo": d.codigo
    }

@descuentos_bp.post("/negocios/<int:id_usuario>/descuentos")
@jwt_required()
def crear_descuento(id_usuario):
    """
    Endpoint para crear un descuento. Delega las validaciones a la capa model.
    """
    error_autenticacion = _verificar_token(id_usuario)
    if error_autenticacion:
        return error_autenticacion
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    try:
        nuevo_descuento = validar_y_crear_descuento(id_usuario, data, repositorio_descuento)
        return jsonify({
            "mensaje": "¡Descuento creado correctamente!",
            "descuento": _serializar_descuento(nuevo_descuento)
        }), 201
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400


@descuentos_bp.get("/negocios/<int:id_negocio>/descuentos")
def listar_descuentos(id_negocio):
    """
    Endpoint para que los usuarios clientes puedan ver los descuentos 
    asociados al consultar un negocio.
    """
    descuentos = repositorio_descuento.listar_descuentos_negocio(id_negocio)
    return jsonify({
        "descuentos": [_serializar_descuento(d) for d in descuentos]
    }), 200


@descuentos_bp.delete("/negocios/<int:id_usuario>/descuentos/<int:id_descuento>")
@jwt_required()
def borrar_descuento(id_usuario, id_descuento):
    """
    Endpoint para eliminar un descuento de un negocio.
    """
    error_autenticacion = _verificar_token(id_usuario)
    if error_autenticacion:
        return error_autenticacion

    try:
        validar_y_borrar_descuento(id_usuario, id_descuento, repositorio_descuento)
        return jsonify({"mensaje": "Descuento eliminado exitosamente"}), 200
    except ExcepcionSistema as e:
        status_code = 404 if "no existe" in e.mensaje else 400
        return jsonify({"error": e.mensaje}), status_code


def _verificar_token(id_usuario):
    """
    Valida la identidad del token JWT. Retorna una respuesta JSON de error si falla,
    o None si la verificación es exitosa.
    """
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para realizar la operación."}), 403
    except Exception:
        return jsonify({"error": "Token inválido o corrupto."}), 401
    return None