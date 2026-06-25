from flask import Blueprint, request
from models import FiltroDB, FiltroDeUsuarioDB, JuntadaDB, NegocioDB, ParticipanteDB, UsuarioDB, VotacionDB, PropuestaDB, ImagenNegocioDB, ReseniaDB, ImagenReseniaDB, db
import os
from datetime import datetime
from init import PROVEEDOR_HORARIO

test_bp = Blueprint('test', __name__)

# Este endpoint solo funciona si la aplicación esta en modo test
# sirve para resetear los datos en los tests de aceptación
if os.getenv("APP_ENV", "prod") in ["test", "dev"]:
    @test_bp.get("/dev/reset")
    def resetear_datos():
        ImagenReseniaDB.query.delete()
        ReseniaDB.query.delete()
        ImagenNegocioDB.query.delete()
        VotacionDB.query.delete()
        PropuestaDB.query.delete()
        FiltroDeUsuarioDB.query.delete()
        NegocioDB.query.delete()
        ParticipanteDB.query.delete()
        JuntadaDB.query.delete()
        UsuarioDB.query.delete()
        #FiltroDB.query.delete() Creo que no hace falta borrar los filtros
        db.session.commit()

        return "OK", 200
    
    @test_bp.post("/dev/codigo")
    def cambiar_codigo_a_generar():
        data = request.get_json()
        os.environ["CODIGO_A_GENERAR"] = data.get("codigo", 200)

        return "OK", 201
    
    @test_bp.post("/dev/establecer_tiempo")
    def establecer_tiempo():
        data = request.get_json()
        tiempo_str = data["tiempo"]
        tiempo = datetime.fromisoformat(tiempo_str)
        PROVEEDOR_HORARIO.setear_hora(tiempo)

        return "OK", 201

