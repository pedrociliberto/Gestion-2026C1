import os

from flask import Flask
from flask_jwt_extended import JWTManager

from registro import registro_bp
from juntadas import juntada_bp
from login import login_bp
from negocios import negocio_bp
from filtros import filtro_bp
from propuestas import propuesta_bp
from test import test_bp
from votaciones import votacion_bp
from perfil import perfil_bp
from resenias import resenia_bp
from models import db
from init import obtener_url_bdd
from filtros import agregar_filtros
from beneficios import agregar_beneficios
from beneficios import beneficios_bp
from descuentos import descuentos_bp
from datetime import timedelta

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = obtener_url_bdd()
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'HangOut2026C1-GDSI-ClaveSuperSeguraYExtensaParaNuestroProyecto' 
app.config['IMAGENES_NEGOCIOS_FOLDER'] = os.path.join(app.root_path, 'uploads', 'negocios')
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(days=30)
app.config["JWT_REFRESH_TOKEN_EXPIRES"] = timedelta(days=30)
os.environ["IMAGENES_NEGOCIOS_FOLDER"] = os.path.join(app.root_path, 'uploads', 'negocios')
os.makedirs(app.config['IMAGENES_NEGOCIOS_FOLDER'], exist_ok=True)
jwt = JWTManager(app)

@app.route("/")
def hello_world():
    return "<p>Que grandes los gedasdases</p>"

# El blueprint sirve para definir las rutas en diferentes archivos
# así tenemos todo ordenado:
app.register_blueprint(registro_bp)
app.register_blueprint(juntada_bp)
app.register_blueprint(login_bp)
app.register_blueprint(negocio_bp)
app.register_blueprint(filtro_bp)
app.register_blueprint(propuesta_bp)
app.register_blueprint(test_bp)
app.register_blueprint(votacion_bp)
app.register_blueprint(perfil_bp)
app.register_blueprint(resenia_bp)
app.register_blueprint(beneficios_bp)
app.register_blueprint(descuentos_bp)

PORT = 3000

if __name__ == '__main__':
    print('Iniciando servidor en http://localhost:3000')
    db.init_app(app)
    with app.app_context():
        db.create_all()
        agregar_filtros()
        agregar_beneficios()
    app.run(host='0.0.0.0', debug=True, port=PORT)
    print('Servidor finalizado')
