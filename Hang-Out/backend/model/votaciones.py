from model.excepciones import ExcepcionSistema

def votar_propuestas(id_usuario, propuestas_con_voto, repositorio_votacion):
    """
    **PRE**:
    
    Recibe:

    - Id del usuario
    - Diccionario donde clave=id_propuesta y valor=booleano indicando si la vote
    - Un RepositorioVotacion

    **POST**:
    
    - En caso de exito, actualiza la votación del usuario para cada propuesta
    - Si falla algo, lanza una ExcepcionSistema con el mensaje de error
    """
    for id_propuesta in propuestas_con_voto:
        vote_propuesta = propuestas_con_voto[id_propuesta]

        repositorio_votacion.borrar_votacion_usuario(id_usuario, id_propuesta)

        if vote_propuesta:
            guardo_votacion = repositorio_votacion.guardar_votacion_usuario(id_usuario, id_propuesta)
            if not guardo_votacion:
                raise ExcepcionSistema("No se pudo guardar la votacion")
