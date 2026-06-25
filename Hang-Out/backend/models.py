from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey, ForeignKeyConstraint

db = SQLAlchemy()

class UsuarioDB(db.Model):
    __tablename__ = 'usuarios'
    id = db.Column(db.Integer, primary_key=True)
    nombre_completo = db.Column(db.String(120), nullable=False)
    usuario = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(200), nullable=False)
    es_cuenta_personal = db.Column(db.Boolean, default=True, nullable=False)
    juntadas_creadas = db.relationship("JuntadaDB", back_populates="usuario_organizador")

class JuntadaDB(db.Model):
    __tablename__ = "juntadas"
    id = db.Column(db.Integer, primary_key=True)
    titulo = db.Column(db.String(30))
    codigo = db.Column(db.String(4))
    id_organizador = db.Column(db.Integer, ForeignKey("usuarios.id"), nullable=False)
    usuario_organizador = db.relationship("UsuarioDB", back_populates="juntadas_creadas")
    estado = db.Column(db.String(20), default="PENDIENTE", nullable=False)  # PENDIENTE, CONFIRMADA, PASADA

class ParticipanteDB(db.Model):
    __tablename__ = "participantes"
    id_juntada = db.Column(db.Integer, ForeignKey("juntadas.id"),primary_key=True) 
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"),primary_key=True)
    
class NegocioDB(db.Model):
    __tablename__ = "negocios"
    id = db.Column(db.Integer, ForeignKey("usuarios.id"),primary_key=True)
    nombre = db.Column(db.String(30))
    descripcion = db.Column(db.String(280))
    horarios = db.Column(db.String(280))
    ubicacion = db.Column(db.String(280))
    sitio_web = db.Column(db.String(2000))
    url_ubicacion = db.Column(db.String(2000))

class FiltroDB(db.Model):
    __tablename__ = "filtros"
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(30))

class FiltroDeUsuarioDB(db.Model):
    __tablename__ = "filtros_usuarios"
    id_filtro = db.Column(db.Integer, ForeignKey("filtros.id"), primary_key=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"),primary_key=True)

class PropuestaDB(db.Model):
    __tablename__ = "propuestas"
    id = db.Column(db.Integer, primary_key=True)
    id_juntada = db.Column(db.Integer, ForeignKey("juntadas.id"), nullable=False)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), nullable=False)
    id_negocio = db.Column(db.Integer, ForeignKey("negocios.id"), nullable=True) # Puede ser nulo si la propuesta es de un lugar personalizado
    lugar_personalizado = db.Column(db.String(120), nullable=True) # Puede ser nulo si la propuesta es de un negocio registrado
    fecha_hora_inicio = db.Column(db.DateTime, nullable=False)
    fecha_hora_fin = db.Column(db.DateTime, nullable=True)
    es_ganadora = db.Column(db.Boolean, nullable=False)

class VotacionDB(db.Model):
    __tablename__ = "votaciones"
    id_propuesta = db.Column(db.Integer, ForeignKey("propuestas.id"), primary_key=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), primary_key=True)
    
class ImagenNegocioDB(db.Model):
    __tablename__ = "imagenes_negocios"
    id = db.Column(db.Integer, primary_key=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), nullable=False)
    url_imagen = db.Column(db.String(2000), nullable=False)

class BeneficioDB(db.Model):
    __tablename__ = "beneficios"
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(60), nullable=False)
    descripcion = db.Column(db.String(280), nullable=False)

class BeneficioNegocioDB(db.Model):
    __tablename__ = "beneficios_negocios"
    id_beneficio = db.Column(db.Integer, ForeignKey("beneficios.id"), primary_key=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), primary_key=True)

class ReseniaDB(db.Model):
    __tablename__ = "resenias"
    id_negocio = db.Column(db.Integer, ForeignKey("negocios.id"), nullable=False, primary_key=True, unique=True)
    id_juntada = db.Column(db.Integer, ForeignKey("juntadas.id"), nullable=False, primary_key=True, unique=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), nullable=False, primary_key=True, unique=True)
    puntaje = db.Column(db.Integer, nullable=False)
    resenia = db.Column(db.String(500), nullable=True)
    fecha_publicacion = db.Column(db.DateTime, nullable=False)

class notificacionPopUpRestantes(db.Model):
    __tablename__ = "notificaciones_pop_up_restantes"
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), primary_key=True)
    notificaciones_restantes = db.Column(db.Integer, nullable=False)

class notificacionPopUp(db.Model):
    __tablename__ = "notificaciones_pop_up"
    id = db.Column(db.Integer, primary_key=True)
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"))
    titulo = db.Column(db.String(30), nullable=False)
    descripcion = db.Column(db.String(280), nullable=False)

class usuarioReceptorNotificacionPopUp(db.Model):
    # Una vez que se envían al usuario, deberían borrarse de esta tabla (es como una cola).
    __tablename__ = "usuarios_receptor_notificaciones_pop_up"
    id_usuario = db.Column(db.Integer, ForeignKey("usuarios.id"), primary_key=True)
    id_notificacion = db.Column(db.Integer, ForeignKey("notificaciones_pop_up.id"), primary_key=True)
    
class ImagenReseniaDB(db.Model):
    __tablename__ = "imagenes_resenias"
    id = db.Column(db.Integer, primary_key=True)
    id_negocio = db.Column(db.Integer, nullable=False)
    id_juntada = db.Column(db.Integer, nullable=False)
    id_usuario = db.Column(db.Integer, nullable=False)
    __table_args__ = (ForeignKeyConstraint(
        [id_negocio, id_juntada, id_usuario],[ReseniaDB.id_negocio, ReseniaDB.id_juntada, ReseniaDB.id_usuario]
        ),
        {}
    ) # Tuve que declararlo asi para que SQLAlchemy se de cuenta de que la ForeignKey era de mas de una columna
    nombre_imagen = db.Column(db.String(1000), nullable=False)

class DescuentoDB(db.Model):
    __tablename__ = "descuentos"
    id = db.Column(db.Integer, primary_key=True)
    id_negocio = db.Column(db.Integer, db.ForeignKey("negocios.id"), nullable=False)
    descripcion = db.Column(db.String(280), nullable=False)
    porcentaje = db.Column(db.Integer, nullable=True) 
    monto = db.Column(db.Float, nullable=True)
    codigo = db.Column(db.String(20), nullable=False)
