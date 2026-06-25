# Repositorio que se encarga de manejar las operaciones en la tabla "Usuario"

import sys
from werkzeug.security import check_password_hash

sys.path.append("/backend")
from models import UsuarioDB, db
class RepositorioUsuario:
    def buscar_por_id(self, id_usuario):
        return UsuarioDB.query.get(id_usuario)
    
    def buscar_nombre_por_id(self, id_usuario):
        usuario = UsuarioDB.query.get(id_usuario)
        return usuario.usuario if usuario else None

    def buscar_por_credenciales(self, nombre_usuario, password):
        usuario = UsuarioDB.query.filter_by(usuario=nombre_usuario).first()
        if not usuario:
            return None
        return usuario if check_password_hash(usuario.password_hash, password) else None
    
    def modificar_usuario(self, id_usuario,nombre_usuario, email):

        usuario = UsuarioDB.query.filter_by(id=id_usuario).first()

        if not usuario:
            return None

        usuario.email = email
        usuario.usuario = nombre_usuario

        try:
            db.session.commit()
            return usuario
        except Exception as e:
            db.session.rollback()
            raise e