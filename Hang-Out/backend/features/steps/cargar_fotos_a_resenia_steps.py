import requests as r
from behave import *
from behave.api.pending_step import StepNotImplementedError
from constants import *
import os

@given(u'que cree y envie una reseña en esa juntada para el lugar "{nombre_negocio}"')
def step_impl(context, nombre_negocio):
    context.execute_steps(
        u'Cuando creo una reseña en esa juntada para el lugar "{nombre_negocio}"'
        .format(nombre_negocio=nombre_negocio)
    )
    context.execute_steps(
        u'Cuando elijo para la reseña una valoración de "5" estrellas'
    )
    context.execute_steps(
        u'Cuando incluyo en la reseña un comentario de largo "100"'
    )
    context.execute_steps(
        u'Cuando envio la reseña'
    )


@when(u'agrego a la reseña "{cantidad_imagenes_str}" imagenes de "{peso_imagenes_str}" MB')
def step_impl(context, cantidad_imagenes_str, peso_imagenes_str):
    cantidad_imagenes = int(cantidad_imagenes_str)
    peso_imagenes = int(peso_imagenes_str)
    context.cantidad_imagenes = cantidad_imagenes

    id_negocio = context.id_negocio
    id_juntada = context.id_juntada
    id_usuario = context.id
    context.ruta_imagenes_resenia = HOSTNAME_WEB + RUTA_CREAR_RESENIA + f"/{id_negocio}" + f"/{id_juntada}" + f"/{id_usuario}" + RUTA_CARGAR_IMAGEN_RESENIA

    with open(RUTA_IMAGEN_TEMPORAL, "wb") as imagen_temporal:
        imagen_temporal.write(b"A"*(peso_imagenes*(10**6)))
    
    for i in range(cantidad_imagenes):
        files = {"imagen": open(RUTA_IMAGEN_TEMPORAL, "rb")}
        context.respuesta = r.post(
            context.ruta_imagenes_resenia,
            files=files
        )

    os.remove(RUTA_IMAGEN_TEMPORAL)


@then(u'la reseña contiene las imagenes que agregue')
def step_impl(context):
    respuesta = r.get(context.ruta_imagenes_resenia)
    assert respuesta.status_code == 201
    respuesta_json = respuesta.json()

    assert len(respuesta_json["imagenes"]) == context.cantidad_imagenes