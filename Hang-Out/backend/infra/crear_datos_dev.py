import os
import subprocess
from subprocess import PIPE, STDOUT
import sys
import requests as r
from datos_dev import DATOS_USUARIOS, DATOS_JUNTADAS, DATOS_INVITADOS, DATOS_NEGOCIOS
import time
import random
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

HOSTNAME_WEB = "http://localhost:3000"
RUTA_CREAR_USUARIO = "/registro"
RUTA_CREAR_JUNTADA = "/juntada"
RUTA_UNIRSE_JUNTADA = "/juntada/unirse"
RUTA_MODIFICAR_NEGOCIO = "/negocios/"
RUTA_CREAR_PROPUESTA = "/propuesta"
RUTA_VOTAR_PROPUESTA = "/votacion"
RUTA_CREAR_RESENIA = "/resenia"
RUTA_IMAGENES = "/negocios/imagenes"
RUTA_RESET = "/dev/reset"
PROPUESTAS_POR_JUNTADA = 3
BASE_DIR = os.path.join(os.path.dirname(__file__), "imagenes")


COMENTARIOS_SISTEMA = {
    "buenos": [
        "Excelente lugar para ir en grupo, la pasamos genial.",
        "Muy buena atención y el ambiente es hermoso.",
        "Riquísima la comida, ideal para este tipo de juntadas.",
        "Espectacular, superó las expectativas de todos.",
        "Precios super accesibles y el ambiente super relajado.",
        "Un lugar impecable, se convirtió en nuestro punto fijo.",
        "La atención de los mozos de diez, super rápido todo.",
        "Fuimos un grupo grande de la facu y nos acomodaron al toque. Clave.",
        "Muy los platos son abundantes. Para volver siempre."
    ],
    "malos": [
        "Lindo lugar pero tardaron una eternidad en traernos la cuenta.",
        "La comida llegó fría y la atención dejó bastante que desear.",
        "Ruidoso, caro para lo que ofrecen y tardaron mil años en atender.",
        "El ambiente estaba lindo pero la comida no tenía gusto a nada.",
        "Nos cobraron un recargo por ser grupo grande que nunca nos avisaron. Pésimo.",
        "Mucha facha el lugar pero la atención es lentísima, no lo recomiendo.",
        "El wifi no funcionaba y las mesas estaban super pegadas, incómodo.",
        "Pedimos una reserva para 8 y cuando llegamos no nos tenían anotados.",
        "La comida zafa pero la relación precio-calidad es malísima. Te matan."
    ]
}


def main():
    entorno_servidor = dict(os.environ)
    entorno_servidor["APP_ENV"] = "dev"
    # Creo proceso del servidor con el que se van a ejecutar los tests
    proceso_servidor = subprocess.Popen([sys.executable, "app.py"], env=entorno_servidor, text=True, stdout=PIPE, stderr=STDOUT)
    print("Abriendo servidor...")
    time.sleep(5)

    limpiar_datos()
    registrar_usuarios()
    usuarios = iniciar_sesion_usuarios()
    # Mapear los IDs reales que asignó la base de datos a los negocios correspondientes
    usuarios_negocios = [u for u in usuarios if not u.get("es_cuenta_personal", True)]
    for idx, negocio in enumerate(DATOS_NEGOCIOS):
        if idx < len(usuarios_negocios):
            negocio["id"] = usuarios_negocios[idx]["id"]

    juntadas_creadas = crear_juntadas(usuarios)
    invitados_por_juntada = unirse_a_juntadas(usuarios, juntadas_creadas)
    modificar_negocios(usuarios)
    ganadores_por_juntada =crear_propuestas_y_votos(usuarios, juntadas_creadas, DATOS_NEGOCIOS, invitados_por_juntada)
    cargar_resenias(juntadas_creadas, ganadores_por_juntada)
    cargar_imagenes(usuarios_negocios, DATOS_NEGOCIOS)
    imprimir_usuarios_activos(usuarios)

    # Cierro el proceso del servidor
    proceso_servidor.send_signal(2)

    print("\n Datos de prueba creados exitosamente")

def limpiar_datos():
    respuesta = r.get(HOSTNAME_WEB + RUTA_RESET)
    if not respuesta.ok:
        raise Exception("Fallo al limpiar los datos de la aplicacion")

def registrar_usuarios():
    usuarios = []
    for datos_usuario in DATOS_USUARIOS:
        usuario = registrar_usuario(datos_usuario)
        usuarios.append(usuario)
    return usuarios

def registrar_usuario(datos_usuario):
    cuerpo_query = {
        "nombre_completo": datos_usuario["nombre_completo"],
        "usuario": datos_usuario["usuario"],
        "email": datos_usuario["email"],
        "password": datos_usuario["password"],
        "password_confirm": datos_usuario["password_confirm"],
        "es_cuenta_personal": datos_usuario.get("es_cuenta_personal", True)
    }
    respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_USUARIO, json=cuerpo_query)

    if respuesta.status_code != 201:
        raise Exception("Fallo al crear un usuario de prueba")
    return respuesta.json()

def iniciar_sesion_usuarios():
    usuarios = []
    for datos_usuario in DATOS_USUARIOS:
        respuesta = r.post(HOSTNAME_WEB + "/login", json={
            "usuario": datos_usuario["usuario"],
            "password": datos_usuario["password"]
        })

        if respuesta.status_code != 200:
            raise Exception("Fallo al iniciar sesión con un usuario de prueba")
        
        usuario_obj = respuesta.json().get("data", {})
        usuario_obj["token"] = respuesta.json().get("token")
        usuarios.append(usuario_obj)
    
    return usuarios
    
def crear_juntadas(usuarios_creados):
    juntadas = []

    for i, datos_juntada in enumerate(DATOS_JUNTADAS):
        organizador = random.choice(usuarios_creados)
        codigo_generado = f"C{100 + i}"
        
        crear_juntada(datos_juntada, i, organizador)
        
        headers = {"Authorization": f"Bearer {organizador['token']}"}
        res_lista = r.get(f"{HOSTNAME_WEB}/juntadas/{organizador['id']}", headers=headers)
        
        if res_lista.status_code != 200:
            raise Exception("Fallo al listar juntadas del organizador")
            
        juntada_real = next((j for j in res_lista.json() if j["codigo"] == codigo_generado), None)
        if not juntada_real:
            raise Exception(f"No se encontró la juntada creada con código {codigo_generado}")
            
        juntadas.append({
            "id": juntada_real["id"],
            "titulo": juntada_real["titulo"],
            "codigo": codigo_generado,
            "id_organizador": organizador["id"],
            "organiza": organizador["id"], 
            "estado": "PASADA" if i % 2 == 0 else "PENDIENTE"
        })
        
    return juntadas

def crear_juntada(datos_juntada, indice, usuario_organizador):
    codigo_juntada = f"C{100 + indice}"
    cuerpo_query = {
        "titulo": datos_juntada["titulo"],
        "codigo": codigo_juntada,
        "id_organizador": usuario_organizador["id"]
    }
    respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query)

    if respuesta.status_code != 201:
        raise Exception("Fallo al crear una juntada de prueba")
        
    return respuesta.json()

def unirse_a_juntadas(usuarios_creados, juntadas_creadas):
    invitados_por_juntada = {}
    invitados_unidos = set()

    for datos_invitado in DATOS_INVITADOS:
        id_juntada_index = datos_invitado["id_juntada"] - 1
        id_usuario_index = datos_invitado["id_usuario"] - 1

        if not (0 <= id_juntada_index < len(juntadas_creadas)) or not (0 <= id_usuario_index < len(usuarios_creados)):
            continue

        juntada_obj = juntadas_creadas[id_juntada_index]
        usuario_obj = usuarios_creados[id_usuario_index]

        if usuario_obj["id"] == juntada_obj["organiza"] or (juntada_obj["id"], usuario_obj["id"]) in invitados_unidos:
            continue

        cuerpo_query = {
            "codigo": juntada_obj["codigo"],
            "id_usuario": usuario_obj["id"]
        }
        respuesta = r.post(HOSTNAME_WEB + RUTA_UNIRSE_JUNTADA, json=cuerpo_query)
        if respuesta.status_code not in [200, 201]:
            raise Exception("Fallo al unirse a una juntada de prueba")

        invitados_unidos.add((juntada_obj["id"], usuario_obj["id"]))
        invitados_por_juntada.setdefault(juntada_obj["id"], []).append(usuario_obj["id"])
        
    return invitados_por_juntada

def modificar_negocios(usuarios_creados):
    negocios = []
    for datos_negocio in DATOS_NEGOCIOS:
        usuario = next((u for u in usuarios_creados if u["id"] == datos_negocio["id"]), None)
        token = usuario["token"] if usuario else None
        negocio = modificar_negocio(datos_negocio, token)
        negocios.append(negocio)

    return negocios

def modificar_negocio(datos_negocio, token):
    cuerpo_query = {
        "id": datos_negocio["id"],
        "nombre": datos_negocio["nombre"],
        "descripcion": datos_negocio["descripcion"],
        "horarios": datos_negocio["horarios"],
        "ubicacion": datos_negocio["ubicacion"],
        "sitio_web": datos_negocio["sitio_web"],
        "filtros": datos_negocio["filtros"],
        "url_ubicacion": datos_negocio["url_ubicacion"]
    }
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    respuesta = r.put(HOSTNAME_WEB + f"{RUTA_MODIFICAR_NEGOCIO}{datos_negocio['id']}", json=cuerpo_query, headers=headers)

    if respuesta.status_code != 200:
        raise Exception("Fallo al modificar un negocio de prueba")
    return respuesta.json()
    
def crear_propuestas_y_votos(usuarios_creados, juntadas_creadas, negocios_creados, invitados_por_juntada):
    fecha_actual = datetime.now(ZoneInfo("America/Argentina/Buenos_Aires")).replace(tzinfo=None)
    ganadores_por_juntada = {}

    for juntada in juntadas_creadas:
        negocios_juntada = random.sample(negocios_creados, PROPUESTAS_POR_JUNTADA)
        for i in range(PROPUESTAS_POR_JUNTADA):
            total_votantes = len(invitados_por_juntada.get(juntada["id"], []))
            if juntada["estado"] == "PASADA" and i == 0:
                fecha_inicio = fecha_actual + timedelta(days=1)
                cantidad_votos = total_votantes + 1 # Invitados + Organizador
                ganadores_por_juntada[juntada["id"]] = negocios_juntada[i]
            else:
                fecha_inicio = fecha_actual + timedelta(days=7)
                cantidad_votos = 0

            negocio_random = negocios_juntada[i]

            cuerpo_propuesta = {
                "id_negocio": negocio_random["id"],
                "fecha_hora_inicio": fecha_inicio.isoformat()
            }
            
            # Obtener el token del organizador
            organizador = next((u for u in usuarios_creados if u["id"] == juntada["id_organizador"]), None)
            token_organizador = organizador["token"] if organizador else None
            headers_organizador = {"Authorization": f"Bearer {token_organizador}"} if token_organizador else {}
            
            ruta_postular = f"/postular/{juntada['id_organizador']}/{juntada['id']}"
            res_propuesta = r.post(HOSTNAME_WEB + ruta_postular, json=cuerpo_propuesta, headers=headers_organizador)
            if res_propuesta.status_code != 201:
                raise Exception("Fallo al crear una propuesta de prueba")
            
            # Consultamos las propuestas de la juntada para obtener el ID real de la propuesta que acabamos de crear
            res_lista_propuestas = r.get(HOSTNAME_WEB + f"/postular/{juntada['id_organizador']}/{juntada['id']}", headers=headers_organizador)
            if res_lista_propuestas.status_code != 200:
                raise Exception("Fallo al listar propuestas de la juntada")
            
            propuestas_juntada = res_lista_propuestas.json().get("propuestas", [])
            propuesta_real = next((p for p in propuestas_juntada if p["id_negocio"] == negocio_random["id"]), None)
            if not propuesta_real:
                raise Exception(f"No se encontró la propuesta creada para el negocio {negocio_random['id']}")
            
            propuesta_id = propuesta_real["id"]

            # Forzar simulación de votos mediante HTTP
            usuarios_votantes_disponibles = [u for u in usuarios_creados if u.get("es_cuenta_personal", True)]
            cantidad_votos_real = min(cantidad_votos, len(usuarios_votantes_disponibles))
            usuarios_votantes = random.sample(usuarios_votantes_disponibles, cantidad_votos_real)
            
            for usuario_votante in usuarios_votantes:
                id_usuario_votante = usuario_votante["id"]
                token_votante = usuario_votante["token"]
                cuerpo_voto = {
                    str(propuesta_id): True
                }
                headers_votante = {"Authorization": f"Bearer {token_votante}"}
                ruta_voto = f"/votacion/{id_usuario_votante}/{juntada['id']}"
                res_voto = r.post(HOSTNAME_WEB + ruta_voto, json=cuerpo_voto, headers=headers_votante)
                if res_voto.status_code != 201:
                    raise Exception("Fallo al registrar voto de prueba")

        # Si la juntada es PASADA, cerramos las votaciones para que se elija la ganadora
        if juntada["estado"] == "PASADA":
            organizador = next((u for u in usuarios_creados if u["id"] == juntada["id_organizador"]), None)
            token_organizador = organizador["token"] if organizador else None
            headers_organizador = {"Authorization": f"Bearer {token_organizador}"} if token_organizador else {}
            
            res_cerrar = r.post(HOSTNAME_WEB + f"/juntada/{juntada['id']}/cerrar", headers=headers_organizador)
            if res_cerrar.status_code != 200:
                raise Exception("Fallo al cerrar votaciones de la juntada")

    # Avanzar el tiempo del servidor en el futuro (10 días adelante) para que las juntadas CONFIRMADAS 
    # (cuya propuesta ganadora empieza en fecha_actual + 1 día) automáticamente pasen al estado PASADA.
    tiempo_futuro = fecha_actual + timedelta(days=10)
    res_tiempo = r.post(HOSTNAME_WEB + "/dev/establecer_tiempo", json={"tiempo": tiempo_futuro.isoformat()})
    if res_tiempo.status_code != 201:
        raise Exception("Fallo al avanzar el tiempo del servidor")
    return ganadores_por_juntada

def cargar_resenias(juntadas_creadas, negocios_ganadores):
    juntadas_pasadas = [j for j in juntadas_creadas if j["estado"] == "PASADA"]

    for idx, juntada in enumerate(juntadas_pasadas):
        if not negocios_ganadores:
            break
            
        negocio_elegido = negocios_ganadores.get(juntada["id"])
        if not negocio_elegido:
            continue
        valoracion = random.choice([1, 2, 4, 5])
        
        if valoracion >= 4:
            texto_opinion = random.choice(COMENTARIOS_SISTEMA["buenos"])
        else:
            texto_opinion = random.choice(COMENTARIOS_SISTEMA["malos"])

        cuerpo_resenia = {
            "negocioId": negocio_elegido["id"],
            "juntadaId": juntada["id"],
            "userId": juntada["id_organizador"],
            "valoracion": valoracion,
            "texto_resenia": texto_opinion
        }
        respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_RESENIA, json=cuerpo_resenia)
        if respuesta.status_code != 201:
            raise Exception("Fallo al insertar reseña automática")
        
def cargar_imagenes(usuarios_negocio, DATOS_NEGOCIO):
    for negocio in DATOS_NEGOCIO:
        usuario = next(
            (u for u in usuarios_negocio if u["id"] == negocio["id"]),
            None
        )
        token = usuario["token"]
        cargar_imagen(negocio, token)
    

def cargar_imagen(datos_negocio, token):
    nombre_negocio = datos_negocio["nombre"].lower()

    headers = {
        "Authorization": f"Bearer {token}"
    }

    if "bar" in nombre_negocio:
        carpeta = "bar"
    elif "caf" in nombre_negocio:
        carpeta = "cafe"
    elif "cine" in nombre_negocio: 
        carpeta = "cine"
    elif "sala" in nombre_negocio:
        carpeta = "sala"
    elif "helad" in nombre_negocio:
        carpeta = "heladeria"
    else: 
        carpeta = "restaurante"

    ruta_carpeta = os.path.join(BASE_DIR, carpeta)

    archivos = [
        f for f in os.listdir(ruta_carpeta)
        if f.lower().endswith((".jpg", ".jpeg", ".png", ".JPG"))
    ]

    cantidad_imagenes = random.randint(1, min(3, len(archivos)))

    for nombre_archivo in random.sample(archivos, cantidad_imagenes):
        path = os.path.join(ruta_carpeta, nombre_archivo)

        with open(path, "rb") as img:
            files = {
                "imagen": (nombre_archivo, img, "image/jpeg")
            }

            respuesta = r.post(
                f"{HOSTNAME_WEB}/negocios/imagenes/{datos_negocio['id']}",
                files=files,
                headers=headers
            )

        if respuesta.status_code != 200:
            raise Exception(
                f"Error subiendo imagen {nombre_archivo}: {respuesta.text}"
            )


def imprimir_usuarios_activos(usuarios_creados):
    usuarios_personales = [u for u in usuarios_creados if u.get("es_cuenta_personal", True)]
    usuarios_negocios = [u for u in usuarios_creados if not u.get("es_cuenta_personal", True)]
    print("\nUsuarios personales:")
    for usuario in usuarios_personales:
        print(f"- ID: {usuario['id']}, Usuario: {usuario['usuario']}, Contra: Password123!, Tipo: Personal")
    print("\nUsuarios negocios:")
    for usuario in usuarios_negocios:
        print(f"- ID: {usuario['id']}, Usuario: {usuario['usuario']}, Contra: Password123!, Tipo: Negocio")

if __name__ == "__main__":
    main()