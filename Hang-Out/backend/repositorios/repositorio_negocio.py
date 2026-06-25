import sys

sys.path.append("/backend")
from model.negocio import Negocio
from repositorios.repositorio_filtro import RepositorioFiltro
from models import db, NegocioDB , BeneficioDB , BeneficioNegocioDB

class RepositorioNegocio:
    def __init__(self):
         self.repositorio_filtro = RepositorioFiltro()

    def guardar_negocio(self, id_usuario, nombre, descripcion, horarios, ubicacion, sitio_web, url_ubicacion):
        """
        Guarda o actualiza la información de un negocio.
        """
        negocio = NegocioDB.query.get(id_usuario)

        if negocio is None:
            negocio = NegocioDB(id=id_usuario)

        negocio.nombre = nombre
        negocio.descripcion = descripcion
        negocio.horarios = horarios
        negocio.ubicacion = ubicacion
        negocio.sitio_web = sitio_web
        negocio.url_ubicacion = url_ubicacion

        db.session.add(negocio)
        try:
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False

    def obtener_negocio(self, id_usuario):
        """
        Obtiene la información de un negocio específico.
        """
        return NegocioDB.query.get(id_usuario)
    
    def buscar_por_nombre_en_minusculas(self, nombre_buscado):
         negocios_db = NegocioDB.query.filter(NegocioDB.nombre.ilike(nombre_buscado)).all()

         negocios = list(map(self._crear_negocio_desde_negocio_db, negocios_db))

         return negocios
    
    def buscar_por_contencion_de_palabra(self, palabra):
         # Asumamos que no nos van a hacer una inyeccion SQL. Jejej
         negocios_db = NegocioDB.query.filter(NegocioDB.nombre.ilike(f"%{palabra}%")).all()
         negocios = list(map(self._crear_negocio_desde_negocio_db, negocios_db))

         return negocios

    def _crear_negocio_desde_negocio_db(self, negocio_db):
         id = negocio_db.id
         nombre = negocio_db.nombre
         descripcion = negocio_db.descripcion
         horarios = negocio_db.horarios
         ubicacion = negocio_db.ubicacion
         sitio_web = negocio_db.sitio_web
         url_ubicacion = negocio_db.url_ubicacion
         filtros = self.repositorio_filtro.listar_filtros_usuario(negocio_db.id)

         #Se verifica si tiene activo el beneficio de posicionamiento
         beneficio_pos = BeneficioDB.query.filter(
               BeneficioDB.nombre.ilike("%posicionamiento%")
          ).first()
         tiene_posicionamiento = False
         if beneficio_pos:
               tiene_posicionamiento = BeneficioNegocioDB.query.filter_by(
                    id_usuario=negocio_db.id,
                    id_beneficio=beneficio_pos.id
               ).first() is not None

         return Negocio(
               nombre,
               descripcion,
               horarios,
               ubicacion,
               sitio_web,
               filtros,
               url_ubicacion=url_ubicacion,
               id=negocio_db.id,
               tiene_posicionamiento=tiene_posicionamiento,
          )
    
    def listar_todos(self):
         negocios_db = NegocioDB.query.all()
         negocios = list(map(self._crear_negocio_desde_negocio_db, negocios_db))

         return negocios

