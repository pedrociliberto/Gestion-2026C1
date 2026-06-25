from flask import Blueprint, jsonify, request
from models import FiltroDB, db
from repositorios.repositorio_filtro import RepositorioFiltro

repositorio_filtro = RepositorioFiltro()
from model.excepciones import ExcepcionSistema

filtro_bp = Blueprint("filtro", __name__)

NOMBRES_FILTROS = [
    "Pet friendly", "Familiar", "Rampa de acceso", "Ascensor",
    "Restaurant", "Bar", "Negocio", "Cafeteria", "Entretenimiento",
    "+18", "+21", "Apto vegano", "Apto vegetariano", "Apto celiacos"
]

def agregar_filtros():
    """
    Agrega los filtros predefinidos a la base de datos si no existen. Este método se llama al iniciar la aplicación para asegurar que los filtros estén disponibles.
    Si ya existen filtros en la base de datos, no hace nada.
    """
    filtros = FiltroDB.query.all()
    if filtros:
        return

    for nombre in NOMBRES_FILTROS:
        filtro = FiltroDB(
            nombre=nombre
        )
        db.session.add(filtro)
    
    db.session.commit()

@filtro_bp.get("/filtros/listar")
def listar_filtros_disponibles():
    """
    Lista los filtros disponibles en el sistema. Retorna un JSON con una lista de filtros, donde cada filtro tiene su id y nombre.
    """
    filtros = repositorio_filtro.listar_filtros()

    return jsonify({
        "filtros": [
            {
                "id": filtro.id,
                "nombre": filtro.nombre,
            }
            for filtro in filtros
        ]
    }), 200

@filtro_bp.get("/filtros/listar/<int:usuario_id>")
def listar_filtros_usuario(usuario_id):
    """
    Lista los filtros asociados a un usuario específico. Retorna un JSON con una lista de filtros, donde cada filtro tiene su id y nombre.
    """
    filtros = repositorio_filtro.listar_filtros_usuario(usuario_id)

    return jsonify({
        "filtros": [
            {
                "id": filtro.id,
                "nombre": filtro.nombre,
            }
            for filtro in filtros
        ]
    }), 200


@filtro_bp.put("/filtros/agregar/<int:usuario_id>")
def guardar_filtros_usuario(usuario_id):
    """
    Agrega una lista de filtros a un usuario específico. Recibe un JSON con una lista de ids de filtros a agregar. Retorna un mensaje de éxito o error.
    """
    try:
        data = request.get_json()
        ids_filtros = data.get("filtros")

        if ids_filtros is None:
            return jsonify({"error": "Se debe enviar una lista de ids de filtros"}), 400

        repositorio_filtro.actualizar_filtros_usuario(usuario_id, ids_filtros)

        return jsonify({"mensaje": "Filtros actualizados al usuario exitosamente"}), 200

    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    