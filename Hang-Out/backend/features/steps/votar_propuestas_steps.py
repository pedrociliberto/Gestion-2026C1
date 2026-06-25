from behave import *
from behave.api.pending_step import StepNotImplementedError
import requests as r

from constants import *

@given(u'que estoy en una juntada de nombre "{titulo_juntada}"')
def step_impl(context, titulo_juntada):
    # Crear juntada
    token = context.token
    cuerpo_query = {
        "titulo": titulo_juntada,
        "codigo": CODIGO_JUNTADA,
        "id_organizador": context.id
    }
    headers = {
        "Authorization": f"Bearer {token}"
    }
    r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query, headers=headers)

    # Buscar juntada
    id_usuario = context.id
    resultado_juntadas = r.get(HOSTNAME_WEB + f"/juntadas/{id_usuario}", headers=headers)
    resultado_json = resultado_juntadas.json()
    primer_juntada = resultado_json[0]
    context.id_juntada = primer_juntada["id"]


@given(u'que existe una propuesta en la juntada "{titulo_juntada}" al lugar "{nombre_personalizado}"')
def step_impl(context, titulo_juntada, nombre_personalizado):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    cuerpo_query = {
        "lugar_personalizado": nombre_personalizado,
        "fecha_hora_inicio": "2026-10-05T10:00:00",
        "fecha_hora_fin": "2026-10-05T12:00:00",
    }
    res = r.post(HOSTNAME_WEB + RUTA_CREAR_POSTULACION + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers)

@when(u'consulto mis votos')
def step_impl(context):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    context.votos = r.get(HOSTNAME_WEB + RUTA_POSTULACIONES + f"/{id_usuario}/{id_juntada}", headers=headers).json()

@then(u'deberia ver que no vote ninguna propuesta')
def step_impl(context):
    for propuesta in context.votos["propuestas"]:
        assert not propuesta["yo_vote"]


@when(u'voto la propuesta "{titulo_propuesta}"')
def step_impl(context, titulo_propuesta):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    cuerpo_query = {}

    for propuesta in context.votos["propuestas"]:
        es_la_que_voto = propuesta["lugar_personalizado"] == titulo_propuesta
        cuerpo_query[propuesta["id"]] = es_la_que_voto

    r.post(HOSTNAME_WEB + RUTA_VOTOS + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers).json()


@then(u'deberia ver que vote la propuesta "{lugar_propuesta}"')
def step_impl(context, lugar_propuesta):
    print(context.votos["propuestas"])
    for propuesta in context.votos["propuestas"]:
        if propuesta["lugar_personalizado"] == lugar_propuesta:
            assert propuesta["yo_vote"]


@then(u'deberia ver que no vote la propuesta "{lugar_propuesta}"')
def step_impl(context, lugar_propuesta):
    for propuesta in context.votos["propuestas"]:
        if propuesta["lugar_personalizado"] == lugar_propuesta:
            assert not propuesta["yo_vote"]


@when(u'voto las propuestas "{titulo_juntada_uno}" y "{titulo_juntada_dos}"')
def step_impl(context, titulo_juntada_uno, titulo_juntada_dos):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    cuerpo_query = {}
    cuerpo_query[context.votos["propuestas"][0]["id"]] = True
    cuerpo_query[context.votos["propuestas"][1]["id"]] = True
    r.post(HOSTNAME_WEB + RUTA_VOTOS + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers).json()


@when(u'elimino mis votos')
def step_impl(context):
    id_usuario = context.id
    id_juntada = context.id_juntada
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    cuerpo_query = {}

    for i,propuesta in enumerate(context.votos["propuestas"]):
        cuerpo_query[context.votos["propuestas"][i]["id"]] = False
    
    r.post(HOSTNAME_WEB + RUTA_VOTOS + f"/{id_usuario}/{id_juntada}", json=cuerpo_query, headers=headers).json()