from models import FiltroDB, FiltroDeUsuarioDB, db

class RepositorioFiltro:
    def listar_filtros(self):
        """
        Lista todos los filtros disponibles.
        """
        return FiltroDB.query.order_by(FiltroDB.nombre.asc()).all()

    def actualizar_filtros_usuario(self, id_usuario, filtros):
        """
        Actualiza los filtros asociados a un usuario específico.
        """
        # Borra los filtros anteriores
        FiltroDeUsuarioDB.query.filter_by(id_usuario=id_usuario).delete()
        
        # Agrega los filtros nuevos
        for id in filtros:
            filtro_usuario = FiltroDeUsuarioDB(id_filtro=id, id_usuario=id_usuario)
            db.session.add(filtro_usuario)

        try:
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            return False
        
    def listar_filtros_usuario(self, id_usuario):
        """
        Lista los filtros asociados a un usuario específico.
        """
        return FiltroDB.query \
        .join(FiltroDeUsuarioDB, FiltroDB.id == FiltroDeUsuarioDB.id_filtro) \
        .filter(FiltroDeUsuarioDB.id_usuario == id_usuario) \
        .order_by(FiltroDB.nombre.asc()) \
        .all()