import random
import string
from model.excepciones import ExcepcionSistema

def validar_y_crear_descuento(id_usuario, data, repositorio_descuento):
    """
    Validaciones estrictas para la creación de un descuento.
    """
    descripcion = data.get("descripcion", "").strip()
    if not descripcion:
        raise ExcepcionSistema("La descripción del descuento es obligatoria")

    porcentaje = data.get("porcentaje")
    if porcentaje is not None:
        try:
            if isinstance(porcentaje, float):
                raise ExcepcionSistema("El porcentaje de descuento debe ser un número entero")
            porcentaje = int(porcentaje)
            if porcentaje <= 0 or porcentaje > 100:
                raise ExcepcionSistema("El porcentaje de descuento debe ser un número entero entre 1 y 100")
        except (ValueError, TypeError):
            raise ExcepcionSistema("El porcentaje provisto no es un número entero válido")

    monto = data.get("monto")
    if monto is not None:
        try:
            monto = float(monto)
            if monto <= 0:
                raise ExcepcionSistema("El monto del descuento debe ser mayor a 0")
        except (ValueError, TypeError):
            raise ExcepcionSistema("El monto provisto no es un número válido")

    if repositorio_descuento.contar_descuentos_negocio(id_usuario) >= 3:
        raise ExcepcionSistema("Se alcanzó el límite máximo de 3 descuentos por negocio")

    codigo = data.get("codigo", "").strip()
    if not codigo:
        codigo = _generar_codigo_unico(repositorio_descuento)
    else:
        if len(codigo) != 6:
            raise ExcepcionSistema("El código del descuento debe tener 6 caracteres alfanuméricos")
        if repositorio_descuento.buscar_descuento_por_codigo(codigo):
            raise ExcepcionSistema("Ya existe un descuento con ese código")


    nuevo_descuento = repositorio_descuento.crear_descuento(
        id_negocio=id_usuario,
        descripcion=descripcion,
        porcentaje=porcentaje,
        monto=monto,
        codigo=codigo
    )
    
    if not nuevo_descuento:
        raise ExcepcionSistema("Error interno al procesar e insertar el descuento")
        
    return nuevo_descuento


def validar_y_borrar_descuento(id_usuario, id_descuento, repositorio_descuento):
    """
    Lógica de negocio y validación de propiedad para eliminar un descuento.
    """
    descuento = repositorio_descuento.obtener_descuento_por_id(id_descuento)
    if not descuento:
        raise ExcepcionSistema("El descuento que intenta borrar no existe")

    if descuento.id_negocio != id_usuario:
        raise ExcepcionSistema("Este descuento no pertenece a tu negocio")

    exito = repositorio_descuento.eliminar_descuento(descuento)
    if not exito:
        raise ExcepcionSistema("Error interno al intentar eliminar el descuento")


def _generar_codigo_unico(repositorio_descuento):
    """Genera un código alfanumérico aleatorio de 6 caracteres únicos."""
    caracteres = string.ascii_letters + string.digits
    while True:
        codigo = ''.join(random.choices(caracteres, k=6))
        if not repositorio_descuento.buscar_descuento_por_codigo(codigo):
            return codigo