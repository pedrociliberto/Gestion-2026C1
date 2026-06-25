# language: es
Característica: Buscar lugar por nombre
Antecedentes:
Dado que no se ha registrado actividad en la aplicación
Y que estoy registrado
Y que inicie sesion

Escenario: 24.0 Primera votacion, una sola propuesta
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Cuando consulto mis votos
Entonces deberia ver que no vote ninguna propuesta

Escenario: 24.1 Primera votacion, una sola propuesta
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Cuando consulto mis votos
Y voto la propuesta "Pizzeria Manolo"
Y consulto mis votos
Entonces deberia ver que vote la propuesta "Pizzeria Manolo"

Escenario: 24.2 Primera votacion, muchas propuesta
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Kike"
Cuando consulto mis votos
Y voto la propuesta "Pizzeria Manolo"
Y consulto mis votos
Entonces deberia ver que vote la propuesta "Pizzeria Manolo"
Y deberia ver que no vote la propuesta "Pizzeria Kike"

Escenario: 24.3 Muchas votaciones, muchas propuesta
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Kike"
Cuando consulto mis votos
Y voto las propuestas "Pizzeria Manolo" y "Pizzeria Kike"
Y consulto mis votos
Entonces deberia ver que vote la propuesta "Pizzeria Manolo"
Y deberia ver que vote la propuesta "Pizzeria Kike"

Escenario: 24.4 Eliminar votacion
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Cuando consulto mis votos
Y voto la propuesta "Pizzeria Manolo"
Y elimino mis votos
Y consulto mis votos
Entonces deberia ver que no vote ninguna propuesta

Escenario: 24.5 Actualizar votacion
Dado que estoy en una juntada de nombre "Cena"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Manolo"
Y que existe una propuesta en la juntada "Cena" al lugar "Pizzeria Kike"
Cuando consulto mis votos
Y voto la propuesta "Pizzeria Manolo"
Y voto la propuesta "Pizzeria Kike"
Y consulto mis votos
Entonces deberia ver que vote la propuesta "Pizzeria Kike"
Y deberia ver que no vote la propuesta "Pizzeria Manolo"