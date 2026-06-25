from constants import *
import requests as r
from behave import *

@given(u'que existe un negocio "{nombre_negocio}" con su ubicación cargada')
def step_impl(context, nombre_negocio):
    cuerpo_query = {
        "nombre": nombre_negocio,
        "descripcion": DESCRIPCION,
        "horarios": HORARIOS,
        "ubicacion" : UBICACION,
        "sitio_web": SITIO_WEB,
        "filtros": FILTROS,
        "url_ubicacion": URL_UBICACION
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


@when(u'veo los detalles del negocio "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    context.nombre_negocio = nombre_negocio
    token = context.token
    headers = {
        "Authorization": f"Bearer {token}"
    }
    respuesta = r.get(HOSTNAME_WEB + RUTA_BUSCAR_NEGOCIO + f"/{context.id}", headers=headers)
    print(respuesta)
    respuesta_json = respuesta.json()
    context.datos_negocio = respuesta_json["negocio"]


@then(u'puedo ver la ubicación cargada en el negocio')
def step_impl(context):
    nombre_negocio = context.nombre_negocio
    url_ubicacion_esperada = context.negocios[nombre_negocio]["url_ubicacion"]

    assert context.datos_negocio["url_ubicacion"] == url_ubicacion_esperada