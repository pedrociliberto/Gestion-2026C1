from behave import *
import requests as r
from behave.api.pending_step import StepNotImplementedError

from crear_juntada_steps import HOSTNAME_WEB

from constants import *

@given(u'que existe un negocio con nombre "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    cuerpo_query = {
        "nombre": nombre_negocio,
        "descripcion": DESCRIPCION,
        "horarios": HORARIOS,
        "ubicacion" : UBICACION,
        "sitio_web": SITIO_WEB,
        "filtros": FILTROS
    }
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    # Guardar datos con los que creo el negocio para checkearlo despues
    context.negocios = {}
    context.negocios[nombre_negocio] = cuerpo_query
    context.filtros = FILTROS

    id_usuario = context.id

    r.put(f"{HOSTNAME_WEB}{RUTA_CREAR_NEGOCIO}/{id_usuario}", json=cuerpo_query, headers=headers)

@given(u'que existe un negocio con el nombre "{nombre_negocio}" y filtro "{nombre_filtro}"')
def step_impl(context, nombre_negocio, nombre_filtro):
    id_filtro = buscar_id_filtro(nombre_filtro)
    if not id_filtro:
        raise Exception("El filtro no existe")

    cuerpo_query = {
        "nombre": nombre_negocio,
        "descripcion": DESCRIPCION,
        "horarios": HORARIOS,
        "ubicacion" : UBICACION,
        "sitio_web": SITIO_WEB,
        "filtros": [id_filtro]
    }
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    # Guardar datos con los que creo el negocio para checkearlo despues
    context.negocios = {}
    context.negocios[nombre_negocio] = cuerpo_query
    context.filtros = [nombre_filtro]

    id_usuario = context.id

    r.put(f"{HOSTNAME_WEB}{RUTA_CREAR_NEGOCIO}/{id_usuario}", json=cuerpo_query, headers=headers)

@when(u'busco un negocio con nombre "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    params = {
        "busqueda": nombre_negocio,
        "id_usuario": context.id
    }
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO, params=params)


@then(u'la busqueda no muestra resultados.')
def step_impl(context):
    respuesta_json = context.respuesta.json()
    assert respuesta_json["resultados"] == []


@then(u'la busqueda muestra en la posicion "{posicion_str}" al negocio "{nombre_negocio}"')
def step_impl(context, posicion_str, nombre_negocio):
    posicion = int(posicion_str)
    indice = posicion - 1
    respuesta_json = context.respuesta.json()

    # Agarrar los datos con los que guarde ese negocio, para comprobar
    # que se muestran todos los datos correctamente
    datos_negocio = context.negocios[nombre_negocio]
    negocio_en_esa_posicion = respuesta_json["resultados"][indice]

    assert negocio_en_esa_posicion["nombre"] == nombre_negocio
    assert negocio_en_esa_posicion["descripcion"] == datos_negocio["descripcion"]
    assert negocio_en_esa_posicion["horarios"] == datos_negocio["horarios"]
    assert negocio_en_esa_posicion["sitio_web"] == datos_negocio["sitio_web"]
    assert negocio_en_esa_posicion["filtros"] == context.filtros


@when(u'busco un negocio sin especificar el nombre')
def step_impl(context):
    params = {
        "id_usuario": context.id
    }
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO, params=params)


@when(u'busco un negocio sin identificarme')
def step_impl(context):
    params = {
        "busqueda": "Pepe",
    }
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO, params=params)


@when(u'busco un negocio con nombre "{nombre_negocio}" enviando mal mis datos de identificacion')
def step_impl(context, nombre_negocio):
    params = {
        "busqueda": nombre_negocio,
        "id_usuario": 123456789
    }
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO, params=params)


@when(u'busco un negocio con nombre vacio')
def step_impl(context):
    params = {
        "busqueda": "",
        "id_usuario": context.id
    }
    context.respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO, params=params)

def buscar_id_filtro(nombre_filtro):
    respuesta = r.get(HOSTNAME_WEB + RUTA_LISTAR_FILTROS)
    respuesta_json = respuesta.json()

    for filtro in respuesta_json["filtros"]:
        if filtro["nombre"] == nombre_filtro:
            return filtro["id"]
    
    return None