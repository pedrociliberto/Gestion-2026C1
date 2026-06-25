import random
import string
from models import DescuentoDB, db

class RepositorioDescuento:
    
    def listar_descuentos_negocio(self, id_negocio):
        """Retorna todos los descuentos activos de un negocio específico."""
        return DescuentoDB.query.filter_by(id_negocio=id_negocio).all()

    def contar_descuentos_negocio(self, id_negocio):
        """Devuelve la cantidad actual de descuentos de un negocio."""
        return DescuentoDB.query.filter_by(id_negocio=id_negocio).count()

    def obtener_descuento_por_id(self, id_descuento):
        """Retorna un descuento puntual por su ID de clave primaria."""
        return DescuentoDB.query.get(id_descuento)

    def generar_codigo_unico(self):
        """Genera un código alfanumérico aleatorio de 6 caracteres en mayúsculas."""
        while True:
            codigo = ''.join(random.choices(string.ascii_uppercase + string.asc + string.digits, k=6))
            if not DescuentoDB.query.filter_by(codigo=codigo).first():
                return codigo

    def buscar_descuento_por_codigo(self, codigo):
        """Busca un descuento por su código."""
        return DescuentoDB.query.filter_by(codigo=codigo).first()

    def crear_descuento(self, id_negocio, descripcion, porcentaje=None, monto=None, codigo=None):
        """
        Persiste un nuevo descuento realizando las inserciones en la BD.
        Asume que los datos ya fueron validados en la capa superior.
        """
        if not codigo or not codigo.strip():
            codigo = self.generar_codigo_unico()
        else:
            codigo = codigo.strip()

        nuevo_descuento = DescuentoDB(
            id_negocio=id_negocio,
            descripcion=descripcion.strip(),
            porcentaje=porcentaje,
            monto=monto,
            codigo=codigo
        )
        
        try:
            db.session.add(nuevo_descuento)
            db.session.commit()
            return nuevo_descuento
        except Exception:
            db.session.rollback()
            return None

    def eliminar_descuento(self, descuento):
        """Elimina un descuento físico de la base de datos."""
        try:
            db.session.delete(descuento)
            db.session.commit()
            return True
        except Exception:
            db.session.rollback()
            return False