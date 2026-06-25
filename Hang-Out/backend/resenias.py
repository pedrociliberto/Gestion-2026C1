from flask import Blueprint, request, jsonify, send_from_directory, url_for
from init import crear_sistema_con_repositorios
from model.sistema import ExcepcionSistema
import os

TAMANIO_MAXIMO_IMAGEN_RESENIA = 5*(10**6) #5MB
resenia_bp = Blueprint("resenia", __name__)

@resenia_bp.post("/resenia")
def crear_resenia():
    
    try:
        data = request.get_json()
        id_usuario = data.get("userId", None)
        id_juntada = data.get("juntadaId", None)
        id_negocio = data.get("negocioId", None)
        valoracion = data.get("valoracion", None)
        comentario = data.get("texto_resenia", None)

        sistema = crear_sistema_con_repositorios()
        sistema.crear_resenia(id_usuario, id_juntada, id_negocio, valoracion, comentario)

        return jsonify({"mensaje": "Reseña enviada"}), 201
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    

@resenia_bp.get("/resenia/<id_negocio>")
def ver_resenias(id_negocio):
    try:
        sistema = crear_sistema_con_repositorios()
        resenias = sistema.buscar_resenias_por_negocio(id_negocio)
        resenias_con_usuario = []
        for (resenia, usuario) in resenias:
            urls_imagenes = []

            for imagen in resenia.imagenes():
                url_imagen = url_for("resenia.visualizar_imagenes_resenia", nombre_imagen=imagen.nombre)
                urls_imagenes.append(url_imagen)

            resenias_con_usuario.append({
                    "usuario": usuario,
                    "puntaje": resenia.valoracion,
                    "resenia": resenia.comentario,
                    "fecha_publicacion": resenia.fecha_publicacion,
                    "imagenes": urls_imagenes
            })
        return jsonify({"resenias": resenias_con_usuario}), 200
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    
@resenia_bp.route("/resenia/<id_negocio>/<id_juntada>/<id_usuario>/imagenes", methods=["POST"])
def cargar_imagen_a_resenia(id_negocio, id_juntada, id_usuario):
    try:
        imagen = request.files.get("imagen")
        nombre_imagen = imagen.filename

        contenido_imagen = imagen.stream.read(TAMANIO_MAXIMO_IMAGEN_RESENIA)
        # Esto es polemico hacerlo aca, pero la alternativa para checkear esto en el
        # sistema era leer toda la imagen en memoria y despues decirle que era muy pesada jaja
        assert_tamanio_valido(imagen.stream)

        sistema = crear_sistema_con_repositorios()
        sistema.agregar_imagen_a_resenia(id_juntada, id_negocio, id_usuario, contenido_imagen, nombre_imagen)

        return {"mensaje": "OK"}, 201
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400

@resenia_bp.route("/resenia/<id_negocio>/<id_juntada>/<id_usuario>", methods=["DELETE"])
def borrar_resenia(id_negocio, id_juntada, id_usuario):
    sistema = crear_sistema_con_repositorios()
    sistema.borrar_resenia(id_juntada, id_negocio, id_usuario)

    return jsonify({"mensaje": "OK"})

@resenia_bp.route("/resenia/<id_negocio>/<id_juntada>/<id_usuario>/imagenes", methods=["GET"])
def obtener_imagenes_resenia(id_negocio, id_juntada, id_usuario):
    sistema = crear_sistema_con_repositorios()
    imagenes = sistema.obtener_imagenes_resenia(id_juntada, id_negocio, id_usuario)

    imagenes_formateadas = []
    for imagen in imagenes:
        nombre_imagen = imagen.nombre
        url_imagen = url_for("resenia.visualizar_imagenes_resenia", nombre_imagen=nombre_imagen)
        imagenes_formateadas.append(url_imagen)

    return jsonify({"imagenes": imagenes_formateadas}), 201

@resenia_bp.route("/resenia/imagenes/<nombre_imagen>", methods=["GET"])
def visualizar_imagenes_resenia(nombre_imagen):
    # Se podria aca checkear que esa imagen sea efectivam
    carpeta_imagenes = os.getenv("IMAGENES_NEGOCIOS_FOLDER")
    return send_from_directory(carpeta_imagenes, nombre_imagen)

@resenia_bp.route("/resenia/<id_negocio>/<id_juntada>/<id_usuario>/imagenes", methods=["DELETE"])
def borrar_imagenes_resenia(id_negocio, id_juntada, id_usuario):
    sistema = crear_sistema_con_repositorios()
    sistema.borrar_imagenes_resenia(id_juntada, id_negocio, id_usuario)

    return jsonify({"mensaje": "OK"})

def assert_tamanio_valido(imagen_stream):
    if imagen_stream.read(1) != b'':
        raise ExcepcionSistema("Las imagenes deben pesar menos de 5MB")
