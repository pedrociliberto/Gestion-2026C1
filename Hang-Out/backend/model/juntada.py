import random
import string
from datetime import datetime
from model.excepciones import ExcepcionSistema
from model.negocio import analizar_horarios_juntada
from proveedores.proveedor_horario import ProveedorHorario

class Juntada:
    LONGITUD_CODIGO = 4
    
    def __init__(self, titulo, codigo, usuario, proveedor_codigos, repositorio_juntadas, id=None, estado="PENDIENTE", id_propuesta_ganadora=None):
        if titulo == None or titulo == "":
            raise ExcepcionSistema("No se especifico titulo")
        
        if codigo == None or codigo == "":
            codigo = self.__generar_codigo(proveedor_codigos, repositorio_juntadas)
        elif len(codigo) != Juntada.LONGITUD_CODIGO:
            raise ExcepcionSistema("El codigo debe tener longitud cuatro")

        self.__titulo = titulo # los atributos con __ son privados
        self.__codigo = codigo
        self.organizador = usuario
        self.id = id
        self._estado = estado
        self._id_propuesta_ganadora = id_propuesta_ganadora
    
    def titulo(self):
        return self.__titulo

    def codigo(self):
        return self.__codigo
    
    def estado(self):
        return self._estado
    
    def id_propuesta_ganadora(self):
        return self._id_propuesta_ganadora
    
    def ya_paso(self):
        return self._estado == "PASADA"
    
    def __generar_codigo(self, proveedor_codigos, repositorio_juntadas):
        codigo_generado = proveedor_codigos.generar_codigo()
        
        while repositorio_juntadas.buscar_por_codigo(codigo_generado):
            codigo_generado = proveedor_codigos.generar_codigo()
        
        return codigo_generado
    
    def cerrar_votacion(self, id_usuario_solicitante, repo_propuestas, repo_negocio):
        if self.organizador.id != id_usuario_solicitante:
            raise ExcepcionSistema("Solo el organizador de la juntada puede cerrar las votaciones.")

        if self._estado == "CONFIRMADA":
            raise ExcepcionSistema("La votación ya se encuentra cerrada y confirmada.")
        if self._estado == "PASADA":
            raise ExcepcionSistema("No se pueden cerrar votaciones de juntadas pasadas.")

        propuestas, votaciones = repo_propuestas.listar_propuestas_por_juntada(self.id)

        if not propuestas:
            raise ExcepcionSistema("La votación no se puede cerrar porque aún no ha iniciado (no hay propuestas).")

        max_votos = max(votaciones.values())
        propuestas_ganadoras = [p for p in propuestas if votaciones[p.id] == max_votos]
        propuesta_elegida = random.choice(propuestas_ganadoras)

        ahora = ProveedorHorario().horario_actual()
        if propuesta_elegida.fecha_hora_inicio < ahora:
            raise ExcepcionSistema("La propuesta más votada ya no es válida porque su fecha ha pasado. Es necesario elegir una alternativa.")


        self._estado = "CONFIRMADA"
        self._id_propuesta_ganadora = propuesta_elegida.id

        codigo_alerta = "OK"
        if propuesta_elegida.id_negocio:
            negocio = repo_negocio.obtener_negocio(propuesta_elegida.id_negocio)
            if negocio:
                codigo_alerta = analizar_horarios_juntada(
                    negocio,
                    propuesta_elegida.fecha_hora_inicio, 
                    propuesta_elegida.fecha_hora_fin
                )

        return propuesta_elegida, codigo_alerta

    def abrir_votacion(self, id_usuario_solicitante):
        if self.organizador.id != id_usuario_solicitante:
            raise ExcepcionSistema("Solo el organizador de la juntada puede abrir las votaciones.")
        if self._estado == "PASADA":
            raise ExcepcionSistema("No se puede abrir la votación de una juntada que ya pasó.")
        
        self._estado = "PENDIENTE"
        self._id_propuesta_ganadora = None