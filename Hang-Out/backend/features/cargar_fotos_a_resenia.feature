# language: es
Característica: Cargar fotos a reseña
Antecedentes:
Dado que no se ha registrado actividad en la aplicación
Y que estoy registrado
Y que inicie sesion
Y que existe un negocio con nombre "Cafe Martinez"

Escenario: 27.1 Carga de imagenes exitosa
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Y que cree y envie una reseña en esa juntada para el lugar "Cafe Martinez"
Cuando agrego a la reseña "2" imagenes de "4" MB
Entonces la reseña contiene las imagenes que agregue

Escenario: 27.2 Carga de imagenes demasiado pesadas
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Y que cree y envie una reseña en esa juntada para el lugar "Cafe Martinez"
Cuando agrego a la reseña "1" imagenes de "7" MB
Entonces la operación falla con el mensaje "Las imagenes deben pesar menos de 5MB"

Escenario: 27.3 Carga de demasiadas imagenes
Dado que estuve en una juntada pasada en el negocio "Cafe Martinez"
Y que cree y envie una reseña en esa juntada para el lugar "Cafe Martinez"
Cuando agrego a la reseña "6" imagenes de "4" MB
Entonces la operación falla con el mensaje "No se pueden cargar mas de 5 imagenes a la reseña"