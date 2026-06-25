import sys
sys.path.append("/backend")
from models import db, BeneficioDB, BeneficioNegocioDB

class RepositorioBeneficio:

    def listar_todos(self):
        return BeneficioDB.query.all()

    def listar_activos_de_usuario(self, id_usuario):
        activos = BeneficioNegocioDB.query.filter_by(id_usuario=id_usuario).all()
        ids = [a.id_beneficio for a in activos]
        if not ids:
            return []
        return BeneficioDB.query.filter(BeneficioDB.id.in_(ids)).all()

    def listar_disponibles_para_usuario(self, id_usuario):
        activos = BeneficioNegocioDB.query.filter_by(id_usuario=id_usuario).all()
        ids_activos = [a.id_beneficio for a in activos]
        if ids_activos:
            return BeneficioDB.query.filter(BeneficioDB.id.notin_(ids_activos)).all()
        return BeneficioDB.query.all()

    def activar(self, id_usuario, id_beneficio):
        ya_existe = BeneficioNegocioDB.query.filter_by(
            id_usuario=id_usuario, id_beneficio=id_beneficio
        ).first()
        if ya_existe:
            return False
        nuevo = BeneficioNegocioDB(id_usuario=id_usuario, id_beneficio=id_beneficio)
        db.session.add(nuevo)
        
        if id_beneficio == 2:
            from models import notificacionPopUpRestantes
            restantes = notificacionPopUpRestantes(id_usuario=id_usuario, notificaciones_restantes=3)
            db.session.add(restantes)
            
        try:
            db.session.commit()
            return True
        except Exception:
            db.session.rollback()
            return False

    def desactivar(self, id_usuario, id_beneficio):
        registro = BeneficioNegocioDB.query.filter_by(
            id_usuario=id_usuario, id_beneficio=id_beneficio
        ).first()
        if not registro:
            return False
        db.session.delete(registro)
        
        if id_beneficio == 2:
            from models import notificacionPopUpRestantes
            restantes = notificacionPopUpRestantes.query.filter_by(id_usuario=id_usuario).first()
            if restantes:
                db.session.delete(restantes)
                
        try:
            db.session.commit()
            return True
        except Exception:
            db.session.rollback()
            return False