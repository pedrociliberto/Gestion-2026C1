import os
import uuid

from flask import Blueprint, request, jsonify, current_app, send_from_directory, url_for
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename

from model import imagenes
from model.excepciones import ExcepcionSistema
from models import JuntadaDB
from repositorios.repositorio_filtro import RepositorioFiltro
from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_juntada import RepositorioJuntada
from repositorios.repositorio_participante import RepositorioParticipante
from repositorios.repositorio_resenia import RepositorioResenia
from proveedores.proveedor_codigos import ProveedorCodigos
from repositorios.repositorio_negocio import RepositorioNegocio
from init import PROVEEDOR_HORARIO, crear_sistema_con_repositorios
from repositorios.repositorio_propuestas import RepositorioPropuestas
from repositorios.repositorio_notificacion import RepositorioNotificacion
from models import JuntadaDB
from model.estadisiticas_negocio import obtener_apariciones_negocio

from model.sistema import Sistema

# Se instancia globalmente el repositorio_negocio para ser utilizado en las rutas.
repositorio_negocio = RepositorioNegocio()
repositorio_filtro = RepositorioFiltro()
repositorio_notificacion = RepositorioNotificacion()

negocio_bp = Blueprint("negocio", __name__)

def _serializar_negocio(negocio, filtros):
    """
    Serializa un objeto NegocioDB a un diccionario para ser enviado como JSON.
    """
    return {
        "id": negocio.id,
        "nombre": negocio.nombre or "",
        "descripcion": negocio.descripcion or "",
        "horarios": negocio.horarios or "",
        "ubicacion":   negocio.ubicacion   or "",
        "sitio_web": negocio.sitio_web or "",
        "url_ubicacion": negocio.url_ubicacion,
        "filtros": [{"id": f.id, "nombre": f.nombre} for f in filtros],
    }

@negocio_bp.get("/negocios/<int:id_usuario>")
@jwt_required()
def obtener_negocio(id_usuario):
        """
        Obtiene la información del negocio asociado a un usuario específico.
        Si el negocio no existe, se devuelve un objeto con campos vacíos y un mensaje indicando que el negocio aún no fue cargado.
        """
        try:
            id_token = int(get_jwt_identity())
            if id_token != id_usuario:
                return jsonify({"error": "No autorizado para ver la información de este negocio"}), 403
        except Exception:
            return jsonify({"error": "Token inválido o corrupto"}), 401
        
        negocio = repositorio_negocio.obtener_negocio(id_usuario)

        if negocio is None:
            return jsonify({
                "existe": False,
                "negocio": {
                    "id": id_usuario,
                    "nombre": "",
                    "descripcion": "",
                    "horarios": "",
                    "ubicacion": "",
                    "sitio_web": "",
                    "filtros": [],
                },
                "mensaje": "El negocio aun no fue cargado",
            }), 200
        
        filtros_negocio = repositorio_filtro.listar_filtros_usuario(id_usuario)

        return jsonify({
            "existe": True,
            "negocio": _serializar_negocio(negocio, filtros_negocio),
        }), 200


@negocio_bp.put("/negocios/<int:id_usuario>")
@jwt_required()
def guardar_negocio(id_usuario):
    """
    Guarda o actualiza la información del negocio asociado a un usuario específico.
    Se espera un JSON con los campos: nombre, descripcion, horarios, sitio_web y filtros (lista de ids de filtros).
    Valida que todos los campos estén presentes y no estén vacíos, y que no se exceda el límite de 5 filtros. Si el negocio no existe, se crea uno nuevo. Si ya existe, se actualiza la información.
     En caso de error en la validación o al guardar, se devuelve un mensaje de error específico. Si la operación es exitosa, se devuelve un mensaje de éxito.
    """
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para modificar este negocio"}), 403
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
    
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    requerido = ['nombre', 'descripcion', 'horarios','ubicacion', 'filtros']
    for campo in requerido:
        if campo not in data or not str(data.get(campo)).strip():
            return jsonify({"error": f"Debe completar todos los campos para poder registrarse"}), 400
        
    filtros = data.get("filtros", [])
    if len(filtros) > 5:
        return jsonify({"error": "Límite de 5 filtros excedido"}), 400
    
    print(data.get("url_ubicacion"))
    nombre = data.get("nombre").strip()
    descripcion = data.get("descripcion").strip()
    horarios = data.get("horarios").strip()
    ubicacion = data.get("ubicacion", "").strip()
    url_ubicacion = data.get("url_ubicacion", None)
    if "sitio_web" in data and data.get("sitio_web") is not None:
        sitio_web = data.get("sitio_web").strip()
    else:
        sitio_web = ""

    if not repositorio_negocio.guardar_negocio(id_usuario, nombre, descripcion, horarios, ubicacion, sitio_web, url_ubicacion):
        return jsonify({"error": "Error al guardar el negocio"}), 500
    
    if not repositorio_filtro.actualizar_filtros_usuario(id_usuario, filtros):
        return jsonify({"error": "Error al guardar los filtros del negocio, info. del negocio guardada exitosamente"}), 500

    return jsonify({"mensaje": "Información del negocio guardada exitosamente"}), 200

@negocio_bp.get("/negocios")
def buscar_negocios():
    try:
        busqueda = request.args.get("busqueda", None)
        id_usuario = request.args.get("id_usuario", None)
        filtros_seleccionados = request.args.getlist("filtros")
        filtro_dia = request.args.getlist("horario_dia") or None  
        filtro_hora_desde = request.args.get("horario_hora_desde", None) 
        filtro_hora_hasta = request.args.get("horario_hora_hasta", None)  
        usar_filtros_usuario = request.args.get("usar_filtros_usuario", None)  

        sistema = crear_sistema_con_repositorios()
        resultados = sistema.buscar_negocio(
            busqueda,
            id_usuario,
            filtros_seleccionados,
            filtro_dia,
            filtro_hora_desde,
            filtro_hora_hasta,
            usar_filtros_usuario
        )
        resultados_formateados = list(map(lambda negocio: {
            "id": negocio.id,
            "nombre": negocio.nombre,
            "descripcion": negocio.descripcion,
            "horarios": negocio.horarios,
            "ubicacion": negocio.ubicacion or "",
            "sitio_web": negocio.sitio_web,
            "url_ubicacion": negocio.url_ubicacion,
            "filtros": [filtro.nombre for filtro in negocio.filtros],
            "tiene_posicionamiento": negocio.tiene_posicionamiento
        }, resultados))
        print(resultados_formateados)
        respuesta = {"resultados": resultados_formateados}

        return jsonify(respuesta), 200
    except ExcepcionSistema as excepcion:
        return jsonify({"error": excepcion.mensaje}), 400
    
@negocio_bp.get("/negocios/apariciones/<int:id_usuario>")
@jwt_required()
def visualizar_apariciones_negocio(id_usuario):
    """
    Visualiza las apariciones de un negocio específico en propuestas de juntadas.
    """
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para ver la información de este negocio"}), 403
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401

    respuesta = obtener_apariciones_negocio(id_usuario, RepositorioPropuestas, JuntadaDB)
    return jsonify(respuesta), 200


@negocio_bp.get("/negocios/imagenes/archivo/<path:nombre_archivo>")
def visualizar_imagen_negocio(nombre_archivo):
    """
    Visualiza una imagen de un negocio a partir del nombre del archivo.
    """
    carpeta_imagenes = current_app.config["IMAGENES_NEGOCIOS_FOLDER"]
    return send_from_directory(carpeta_imagenes, nombre_archivo)
    
@negocio_bp.get("/negocios/imagenes/<int:id_usuario>")
def obtener_imagenes_negocio(id_usuario):
    """
    Obtiene las imágenes asociadas a un negocio específico.
        Si el negocio no existe, se devuelve un mensaje de error.
        En caso de éxito, se devuelve una lista con los IDs y URLs de las imágenes del negocio.
    """
    try:
        fotos = imagenes.obtener_imagenes_negocio(id_usuario)
        imagenes_formateadas = [
            {
                "id": foto["id"],
                "url": url_for("negocio.visualizar_imagen_negocio", nombre_archivo=foto["url_imagen"]),
            }
            for foto in fotos
        ]
        return jsonify({"imagenes": imagenes_formateadas}), 200
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400

@negocio_bp.post("/negocios/imagenes/<int:id_usuario>")
@jwt_required()
def agregar_imagen_negocio(id_usuario):
    """
    Agrega una imagen a un negocio específico. Se espera un archivo en request.files["imagen"]. 
        Si el negocio no existe, se devuelve un mensaje de error. Si el token es inválido o no coincide con el id_usuario, se devuelve un mensaje de error de autorización. 
        En caso de éxito, se devuelve un mensaje indicando que la imagen fue agregada exitosamente.
        Si no se envía una imagen válida, se devuelve un mensaje de error indicando que la imagen es obligatoria. 
        Si ocurre un error al guardar la imagen, se devuelve un mensaje de error indicando que hubo un problema al guardar la imagen del negocio.
    """
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para modificar este negocio"}), 403
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
    
    archivo_imagen = request.files.get("imagen") or request.files.get("file")
    nombre_original = archivo_imagen.filename if archivo_imagen is not None else ""
    if not nombre_original or not nombre_original.strip():
        return jsonify({"error": "Debe enviar una imagen válida"}), 400

    nombre_guardado = _guardar_imagen_en_disco(nombre_original, archivo_imagen)
    
    try:
        imagenes.agregar_imagen_negocio(id_usuario, nombre_guardado)
    except ExcepcionSistema as e:
        _borrar_imagen_de_disco(nombre_guardado)
        return jsonify({"error": e.mensaje}), 400

    return jsonify({"mensaje": "Imagen agregada al negocio exitosamente"}), 200

@negocio_bp.delete("/negocios/imagenes/<int:id_usuario>")
@jwt_required()
def borrar_imagen_negocio(id_usuario):
    """
    Eliminar una imagen de un negocio específico. Se espera un JSON con el campo "id_imagen".
        Si el negocio no existe, se devuelve un mensaje de error. Si el token es inválido o no coincide con el id_usuario, 
        se devuelve un mensaje de error de autorización.
        En caso de éxito, se devuelve un mensaje indicando que la imagen fue eliminada exitosamente.
    """
    try:
        id_token = int(get_jwt_identity())
        if id_token != id_usuario:
            return jsonify({"error": "No autorizado para modificar este negocio"}), 403
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
    
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400
    
    id_imagen = data.get("id_imagen")
    if not id_imagen:
        return jsonify({"error": "El campo 'id_imagen' es requerido"}), 400

    try:
        imagen = imagenes.obtener_imagen_por_id(id_usuario, id_imagen)
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400

    if not imagen:
        return jsonify({"error": "La imagen no existe"}), 400

    try:
        imagenes.eliminar_imagen_negocio(id_usuario, id_imagen)
        _borrar_imagen_de_disco(imagen["url_imagen"])
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    
    return jsonify({"mensaje": "Imagen eliminada del negocio exitosamente"}), 200

@negocio_bp.get("/negocios/notificaciones")
@jwt_required()
def obtener_notificaciones_restantes():
    try:
        id_usuario = int(get_jwt_identity())
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    
    restantes = repositorio_notificacion.obtener_restantes(id_usuario)
    return jsonify({"notificaciones_restantes": restantes}), 200

@negocio_bp.post("/negocios/notificaciones")
@jwt_required()
def cargar_notificacion():
    try:
        id_usuario = int(get_jwt_identity())
    except ExcepcionSistema as e:
        return jsonify({"error": e.mensaje}), 400
    
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "JSON inválido"}), 400

    titulo = data.get("titulo")
    descripcion = data.get("descripcion")

    if not titulo or not str(titulo).strip() or not descripcion or not str(descripcion).strip():
        return jsonify({"error": "Debe completar todos los campos"}), 400
        
    restantes = repositorio_notificacion.obtener_restantes(id_usuario)
    if restantes <= 0:
        return jsonify({"error": "No tiene notificaciones restantes para este beneficio"}), 403

    if not repositorio_notificacion.cargar_notificacion(id_usuario, str(titulo).strip(), str(descripcion).strip()):
        return jsonify({"error": "Error al cargar la notificación"}), 500
 
    return jsonify({"mensaje": "Notificación cargada exitosamente"}), 200

@negocio_bp.get("/negocios/notificaciones/creadas/<int:id_usuario>")
@jwt_required()
def obtener_notificaciones_creadas(id_usuario):
    try:
        id_usuario = int(get_jwt_identity())
    except Exception:
        return jsonify({"error": "Token inválido o corrupto"}), 401
    
    notificaciones = repositorio_notificacion.obtener_notificaciones_usuario(id_usuario)
    res = []
    for n in notificaciones:
        res.append({
            "id": n.id,
            "titulo": n.titulo,
            "descripcion": n.descripcion
        })
    return jsonify({"notificaciones": res}), 200

@negocio_bp.delete("/negocios/notificaciones/eliminar/<int:id_notificacion>")
@jwt_required()
def eliminar_notificacion(id_notificacion):
    try:
        repositorio_notificacion.eliminar_notificacion(id_notificacion)
        return jsonify({"mensaje": "Notificación eliminada correctamente"}), 200
    except Exception:
        return jsonify({"error": "No se pudo eliminar la notificación o no existe"}), 400


#  --------------------------------------------------------------
# Funciones asociadas a la gestión de imágenes en el disco,
# no forman parte de la interfaz del repositorio ni del modelo.
# Si no les gusta, pueden moverlas a otro lado, puede que sea
# conveniente:

def _borrar_imagen_de_disco(nombre_archivo):
    """
    Elimina una imagen del disco dada su ruta. Si la imagen no existe, no hace nada.
    """
    ruta_imagen = os.path.join(current_app.config["IMAGENES_NEGOCIOS_FOLDER"], nombre_archivo)
    if os.path.exists(ruta_imagen):
        os.remove(ruta_imagen)

def _guardar_imagen_en_disco(nombre_archivo, archivo_imagen):
    """
    Guarda una imagen en el disco y retorna el nombre con el que fue guardada. Si ocurre un error al guardar, se lanza una ExcepcionSistema.
    """
    nombre_original = secure_filename(nombre_archivo)
    nombre_guardado = f"{uuid.uuid4().hex}_{nombre_original}"
    carpeta_imagenes = current_app.config["IMAGENES_NEGOCIOS_FOLDER"]
    ruta_guardado = os.path.join(carpeta_imagenes, nombre_guardado)

    try:
        # Esto es para que el type checker entienda que archivo_imagen no es None a partir de este punto,
        # ya que si fuera None se habría retornado un error en la validación anterior.
        assert archivo_imagen is not None
        archivo_imagen.save(ruta_guardado)
        return nombre_guardado
    except Exception as _:
        raise ExcepcionSistema("Error al guardar la imagen del negocio")