# language: es
Característica: Crear reseña
Antecedentes:
Dado que no se ha registrado actividad en la aplicación
Y que estoy registrado
Y que inicie sesion
Y que existe un negocio con nombre "Cafe Martinez"
Y que existe un negocio con nombre "Cafe Cito"

Escenario: 25.1 Reseña exitosa
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la reseña se crea exitosamente

Escenario: 25.2 Reseña doble al mismo lugar para la misma juntada
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Y que ya tengo una reseña en esa juntada para el lugar "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la operación falla con el mensaje "Ya hiciste una reseña de ese lugar para esta juntada"

Escenario: 25.3 Reseña con valoración invalida
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "7" estrellas
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la operación falla con el mensaje "La valoración debe estar entre 1 y 5 (inclusive)"

Escenario: 25.4 Reseña a lugar no visitado
Dado que estuve en una juntada pasada en el negocio "Cafe Cito"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la operación falla con el mensaje "No se puede reseñar un lugar no visitado"


Escenario: 25.5 Reseña sin valoración
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la operación falla con el mensaje "Se debe especificar una valoración numerica"

Escenario: 25.6 Reseña con comentario vacio
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña un comentario vacio
Y envio la reseña
Entonces la reseña se crea exitosamente

Escenario: 25.7 Reseña con espacios en blanco
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña el comentario "  "
Y envio la reseña
Entonces la operación falla con el mensaje "El comentario no pueden ser solo espacios"

Escenario: 25.8 Reseña muy larga
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña un comentario de largo "501"
Y envio la reseña
Entonces la operación falla con el mensaje "El largo maximo del comentario es 500 caracteres"

Escenario: 25.9 Reseña en juntada que no se cerro y paso
Dado que estoy en una juntada cerrada en el negocio "Cafe Martinez"
Cuando creo una reseña en esa juntada para el lugar "Cafe Martinez"
Y elijo para la reseña una valoración de "5" estrellas
Y incluyo en la reseña el comentario "Lindo lugar!"
Y envio la reseña
Entonces la operación falla con el mensaje "La juntada debe ser pasada y estar cerrada"