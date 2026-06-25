from behave import *
import requests as r

from behave.api.pending_step import StepNotImplementedError
from constants import *

@given(u'que estoy registrado')
def step_impl(context):
    cuerpo_query = {
        "nombre_completo": NOMBRE_COMPLETO_PRUEBA,
        "usuario": USUARIO_PRUEBA,
        "email": EMAIL_PRUEBA,
        "password": PASSWORD_PRUEBA,
        "password_confirm": PASSWORD_PRUEBA,
        "es_cuenta_personal": True
    }
    r.post(HOSTNAME_WEB + RUTA_CREAR_USUARIO, json=cuerpo_query)

@given(u'que inicie sesion')
def step_impl(context):
    cuerpo_query = {
        "usuario": USUARIO_PRUEBA,
        "password": PASSWORD_PRUEBA,
    }
    respuesta = r.post(HOSTNAME_WEB + RUTA_INICIAR_SESION, json=cuerpo_query)
    respuesta_json = respuesta.json()
    context.id = respuesta_json["data"]["id"]
    context.usuario = respuesta_json["data"]["usuario"]
    context.nombre_completo = respuesta_json["data"]["nombre"]
    context.token = respuesta_json["token"]


@when(u'creo una juntada con titulo "{titulo}" y codigo "{codigo}"')
def step_impl(context, titulo, codigo):
    cuerpo_query = {
        "titulo": titulo,
        "codigo": codigo,
        "id_organizador": context.id
    }
    context.respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query)


@then(u'la juntada se crea exitosamente')
def step_impl(context):
    print("Estado: " + f"{context.respuesta.status_code}")
    respuesta_json = context.respuesta.json()
    assert respuesta_json["mensaje"] == MSJ_JUNTADA_CREADA_EXITOSAMENTE


@then(u'el codigo de la juntada es "{codigo}"')
def step_impl(context, codigo):
    respuesta_json = context.respuesta.json()
    assert respuesta_json["juntada"]["codigo"] == codigo


@then(u'el organizador de la juntada soy yo')
def step_impl(context):
    respuesta_json = context.respuesta.json()
    assert respuesta_json["juntada"]["organizador"] == context.nombre_completo


@when(u'creo una juntada con titulo {titulo_juntada}')
def step_impl(context, titulo_juntada):
    cuerpo_query = {
        "titulo": titulo_juntada,
        "id_organizador": context.id
    }
    context.respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query)


@then(u'el largo del codigo de la juntada es "{largo_str}"')
def step_impl(context, largo_str):
    largo = int(largo_str)
    respuesta_json = context.respuesta.json()
    assert len(respuesta_json["juntada"]["codigo"]) == largo


@when(u'creo una juntada sin titulo y con codigo "{codigo}"')
def step_impl(context, codigo):
    cuerpo_query = {
        "codigo": codigo,
        "id_organizador": context.id
    }
    context.respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query)

@then(u'la operación falla con el mensaje "{mensaje_error}"')
def step_impl(context, mensaje_error):
    assert context.respuesta.status_code == 400
    respuesta_json = context.respuesta.json()
    print(respuesta_json["error"])
    print("El codigo debe tener longitud cuatro")
    assert respuesta_json["error"] == mensaje_error

@given(u'que existe una juntada con codigo "{codigo}"')
def step_impl(context, codigo):
    cuerpo_query = {
        "titulo": "Titulo random",
        "codigo": codigo,
        "id_organizador": context.id
    }
    context.respuesta = r.post(HOSTNAME_WEB + RUTA_CREAR_JUNTADA, json=cuerpo_query)

@given(u'que no se ha registrado actividad en la aplicación')
def step_impl(context):
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_RESET)

@given(u'que el generador de caracteres va a generar el codigo "{codigo}"')
def step_impl(context, codigo):
    cuerpo_query = {
        "codigo": codigo
    }
    r.post(HOSTNAME_WEB + RUTA_GENERADOR_CODIGOS, json=cuerpo_query)

@then(u'el codigo de la juntada no es "{codigo}"')
def step_impl(context, codigo):
    respuesta_json = context.respuesta.json()
    assert respuesta_json["juntada"]["codigo"] != codigo