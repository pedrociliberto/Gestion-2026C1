import os
from proveedores.proveedor_horario import ProveedorHorario
from repositorios.repositorio_filtro import RepositorioFiltro
from repositorios.repositorio_usuario import RepositorioUsuario
from repositorios.repositorio_juntada import RepositorioJuntada
from repositorios.repositorio_participante import RepositorioParticipante
from repositorios.repositorio_resenia import RepositorioResenia
from repositorios.repositorio_negocio import RepositorioNegocio
from repositorios.repositorio_propuestas import RepositorioPropuestas
from proveedores.proveedor_codigos import ProveedorCodigos
from model.sistema import Sistema

VARIABLE_DB_URL = {
    "prod": "PROD_DB_URL",
    "dev": "DEV_DB_URL",
    "test": "TEST_DB_URL"
}

PROVEEDOR_HORARIO = ProveedorHorario()

def obtener_url_bdd():
    if not "APP_ENV" in os.environ:
        raise Exception("Se debe especificar la variable de entorno APP_ENV")

    app_env = os.getenv("APP_ENV")
    variable_url = VARIABLE_DB_URL[app_env]
    return os.getenv(variable_url)

def crear_sistema_con_repositorios():
    repo_juntada = RepositorioJuntada(PROVEEDOR_HORARIO)
    repo_usuario = RepositorioUsuario()
    repo_participante = RepositorioParticipante()
    repositorio_negocio = RepositorioNegocio()
    repositorio_filtro = RepositorioFiltro()
    repositorio_resenia = RepositorioResenia(PROVEEDOR_HORARIO)
    repositorio_propuestas = RepositorioPropuestas()
    proveedor_codigos = ProveedorCodigos()
    return Sistema(repo_juntada, repo_usuario, repo_participante, repositorio_negocio, repositorio_filtro, repositorio_resenia, repositorio_propuestas, proveedor_codigos)