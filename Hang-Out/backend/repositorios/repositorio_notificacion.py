import sys

sys.path.append("/backend")
from models import (
    db, 
    notificacionPopUp, 
    notificacionPopUpRestantes, 
    usuarioReceptorNotificacionPopUp,
    FiltroDeUsuarioDB,
    BeneficioNegocioDB,
    UsuarioDB
)

class RepositorioNotificacion:
    def obtener_restantes(self, id_usuario):
        """
        Devuelve la cantidad de notificaciones restantes que un negocio puede enviar.
        """
        restantes = notificacionPopUpRestantes.query.filter_by(id_usuario=id_usuario).first()
        if restantes:
            return restantes.notificaciones_restantes
        return 0

    def cargar_notificacion(self, id_usuario, titulo, descripcion):
        """
        Carga una nueva notificación Pop-Up.
        - Se carga en notificacionPopUp.
        - Se agrega a usuariosReceptorNotificacionPopUp (los usuarios que tengan algún filtro en común).
        - Decrementa el valor en notificacionPopUpRestantes.
        - Si llega a 0, revoca el beneficio.
        """
        try:
            nueva_notificacion = notificacionPopUp(
                id_usuario=id_usuario, 
                titulo=titulo, 
                descripcion=descripcion
            )
            db.session.add(nueva_notificacion)
            db.session.flush()

            filtros_negocio = FiltroDeUsuarioDB.query.filter_by(id_usuario=id_usuario).all()
            ids_filtros = [f.id_filtro for f in filtros_negocio]

            if ids_filtros:
                usuarios_con_filtros = FiltroDeUsuarioDB.query.filter(
                    FiltroDeUsuarioDB.id_filtro.in_(ids_filtros)
                ).all()
                ids_usuarios = set([u.id_usuario for u in usuarios_con_filtros if u.id_usuario != id_usuario])

                if ids_usuarios:
                    usuarios_validos = UsuarioDB.query.filter(
                        UsuarioDB.id.in_(list(ids_usuarios)),
                        UsuarioDB.es_cuenta_personal == True
                    ).all()
                    
                    for u in usuarios_validos:
                        receptor = usuarioReceptorNotificacionPopUp(
                            id_usuario=u.id, 
                            id_notificacion=nueva_notificacion.id
                        )
                        db.session.add(receptor)

            restantes = notificacionPopUpRestantes.query.filter_by(id_usuario=id_usuario).first()
            if restantes:
                restantes.notificaciones_restantes -= 1
                if restantes.notificaciones_restantes <= 0:
                    db.session.delete(restantes)
                    beneficio = BeneficioNegocioDB.query.filter_by(
                        id_usuario=id_usuario, 
                        id_beneficio=2
                    ).first()
                    if beneficio:
                        db.session.delete(beneficio)

            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False
        
    def obtener_notificaciones_usuario(self, id_usuario):
        notificaciones_creadas = notificacionPopUp.query.filter(notificacionPopUp.id_usuario == id_usuario).all()
        return notificaciones_creadas

    def eliminar_notificacion(self, id_notificacion):
        """
        Elimina una notificación Pop-Up.
        - Se elimina de notificacionPopUp.
        - Aumenta el valor en notificacionPopUpRestantes.
        """
        notificacion = notificacionPopUp.query.get(id_notificacion)
        if not notificacion:
            return False
        
        query_usuarios = usuarioReceptorNotificacionPopUp.query.filter_by(id_notificacion=id_notificacion);

        try:
            query_usuarios.delete(synchronize_session=False)
            db.session.delete(notificacion)
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False
        
    def obtener_pendientes(self, id_usuario):

        return (
            db.session.query(notificacionPopUp)
            .join(
                usuarioReceptorNotificacionPopUp,
                usuarioReceptorNotificacionPopUp.id_notificacion == notificacionPopUp.id
            )
            .filter(
                usuarioReceptorNotificacionPopUp.id_usuario == id_usuario
            )
            .all()
        )
    
    def marcar_vista(self, id_usuario, id_notificacion):

        pendiente = (
            usuarioReceptorNotificacionPopUp.query
            .filter_by(
                id_usuario=id_usuario,
                id_notificacion=id_notificacion
            )
            .first()
        )

        if pendiente:
            db.session.delete(pendiente)

        db.session.commit()

