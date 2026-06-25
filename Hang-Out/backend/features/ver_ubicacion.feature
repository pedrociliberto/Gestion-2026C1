#language: es
Característica: Ver ubicación de un lugar
Antecedentes:
Dado que no se ha registrado actividad en la aplicación
Y que estoy registrado
Y que inicie sesion

Escenario: 10.1 Negocio con ubicacion cargada
Dado que existe un negocio "Cafe Martinez" con su ubicación cargada
Cuando veo los detalles del negocio "Cafe Martinez"
Entonces puedo ver la ubicación cargada en el negocio