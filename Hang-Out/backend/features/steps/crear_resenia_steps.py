from behave.api.pending_step import StepNotImplementedError
from behave import *
import requests as r

from constants import *
import time

@given(u'que estuve en una juntada pasada en el negocio "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    context.execute_steps(
        u'Dado que estoy en una juntada cerrada en el negocio "{nombre_negocio}"'
        .format(nombre_negocio=nombre_negocio)
    )

    # Muevo el tiempo al futuro lejano para que la juntada pase a ser pasada :)
    establecer_tiempo_actual(HORARIO_FUTURO_LEJANO)


@when(u'creo una reseña en esa juntada para el lugar "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    if nombre_negocio in context.negocios:
        context.id_negocio = context.negocios[nombre_negocio]["id"]
    else:
        context.id_negocio = 123456
    context.resenia = {}


@when(u'elijo para la reseña una valoración de "{cant_estrellas_str}" estrellas')
def step_impl(context, cant_estrellas_str):
    context.resenia["cant_estrellas"] = int(cant_estrellas_str)


@when(u'incluyo en la reseña el comentario "{comentario}"')
def step_impl(context, comentario):
    context.resenia["comentario"] = comentario


@when(u'envio la reseña')
def step_impl(context):
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }

    cuerpo_query = {
        "texto_resenia": context.resenia.get("comentario", ""),
        "juntadaId": context.id_juntada,
        "negocioId": context.id_negocio,
        "userId": context.id
    }
    if "cant_estrellas" in context.resenia:
        cuerpo_query["valoracion"] = context.resenia["cant_estrellas"]
    
    context.respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_RESENIA, json=cuerpo_query, headers=headers)


@then(u'la reseña se crea exitosamente')
def step_impl(context):
    assert context.respuesta.ok

    respuesta_json = context.respuesta.json()
    mensaje = respuesta_json["mensaje"]

    assert mensaje == "Reseña enviada"


@given(u'que ya tengo una reseña en esa juntada para el lugar "{nombre_lugar}"')
def step_impl(context, nombre_lugar):
    context.execute_steps(
        u'Cuando creo una reseña en esa juntada para el lugar "{nombre_lugar}"'
        .format(nombre_lugar=nombre_lugar)
    )
    context.execute_steps(u'Cuando elijo para la reseña una valoración de "5" estrellas')
    context.execute_steps(u'Cuando incluyo en la reseña el comentario "Lindo lugar!"')
    context.execute_steps(u'Cuando envio la reseña')


@when(u'incluyo en la reseña un comentario de largo "{largo_str}"')
def step_impl(context, largo_str):
    largo = int(largo_str)
    comentario = "A"*largo
    context.execute_steps(
        u'Cuando incluyo en la reseña el comentario "{comentario}"'
        .format(comentario=comentario)
    )

@when(u'incluyo en la reseña un comentario vacio')
def step_impl(context):
    context.resenia["comentario"] = ""

def establecer_tiempo_actual(tiempo):
    cuerpo_query = {
        "tiempo": tiempo
    }
    r.post(HOSTNAME_WEB + RUTA_MOVER_TIEMPO, json=cuerpo_query)


def cerrar_votacion(context):
    token = context.token
    id_juntada = context.id_juntada
    headers = {
        "Authorization": f"Bearer {token}"
    }
    r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA + f"/{id_juntada}" + "/cerrar", headers=headers)

def votar_propuesta(context):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }

    cuerpo_query = {}
    for propuesta in context.votos["propuestas"]:
        es_la_que_voto = propuesta["id_negocio"] == context.id_negocio
        cuerpo_query[propuesta["id"]] = es_la_que_voto

    r.post(HOSTNAME_WEB + RUTA_VOTOS + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers).json()
    return id_juntada,token

def proponer_negocio(context, horario_inicio):
    id_usuario = context.id
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    cuerpo_query = {
        "id_negocio": context.id_negocio,
        "fecha_hora_inicio": horario_inicio,
    }
    id_juntada = context.id_juntada
    respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_POSTULACION + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers)
    
    assert respuesta.ok

def buscar_negocio(context, nombre_negocio):
    context.execute_steps(
        u'Cuando busco un negocio con nombre "{nombre_negocio}"'
        .format(nombre_negocio=nombre_negocio)
    )
    respuesta_json = context.respuesta.json()
    id_negocio = respuesta_json["resultados"][0]["id"]
    return id_negocio

def buscar_juntada(context):
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    id_usuario = context.id
    resultado_juntadas = r.get(HOSTNAME_WEB + f"/juntadas/{id_usuario}", headers=headers)
    resultado_json = resultado_juntadas.json()
    primer_juntada = resultado_json[0]
    return primer_juntada["id"]

@given(u'que estoy en una juntada cerrada en el negocio "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    context.execute_steps(u'Cuando creo una juntada con titulo "Cenita" y codigo "ABCD"')
    time.sleep(0.1)
    context.id_juntada = buscar_juntada(context)

    context.execute_steps(
        u'Dado que existe un negocio con nombre "{nombre_negocio}"'
        .format(nombre_negocio=nombre_negocio)
    )

    context.id_negocio = buscar_negocio(context, nombre_negocio)
    context.negocios[nombre_negocio]["id"] = context.id_negocio

    establecer_tiempo_actual(HORARIO_ACTUAL)
    proponer_negocio(context, HORARIO_FUTURO)
    context.execute_steps(
        u'Cuando consulto mis votos'
    )
    votar_propuesta(context)
    cerrar_votacion(context)