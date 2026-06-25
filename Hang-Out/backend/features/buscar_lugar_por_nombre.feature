# language: es
Característica: Buscar lugar por nombre
Antecedentes:
Dado que no se ha registrado actividad en la aplicación
Y que estoy registrado
Y que inicie sesion

Escenario: 15.1 Busqueda sin coincidencias
Dado que existe un negocio con nombre "Cafe Martinez"
Cuando busco un negocio con nombre "McDonald's"
Entonces la busqueda no muestra resultados.

Escenario: 15.2 Busqueda con coincidencia exacta
Dado que existe un negocio con nombre "Cafe Martinez"
Cuando busco un negocio con nombre "Cafe Martinez"
Entonces la busqueda muestra en la posicion "1" al negocio "Cafe Martinez"

Escenario: 15.3 Busqueda con coincidencia con diferentes mayusculas
Dado que existe un negocio con nombre "Cafe Martinez"
Cuando busco un negocio con nombre "cafE mArtineZ"
Entonces la busqueda muestra en la posicion "1" al negocio "Cafe Martinez"

Escenario: 15.4 Busqueda con coincidencia con diferentes acentos
Dado que existe un negocio con nombre "Cafe Martinez"
Cuando busco un negocio con nombre "Café Martínez"
Entonces la busqueda muestra en la posicion "1" al negocio "Cafe Martinez"

Escenario: 15.5 Busqueda con coincidencia en algunas palabras
Dado que existe un negocio con nombre "Cafe Martinez"
Cuando busco un negocio con nombre "Cafe"
Entonces la busqueda muestra en la posicion "1" al negocio "Cafe Martinez"

Escenario: 15.6 Busqueda de usuario que no existe
Cuando busco un negocio con nombre "Cafe" enviando mal mis datos de identificacion
Entonces la operación falla con el mensaje "El usuario no existe"

Escenario: 15.7 Busqueda sin identificarse
Cuando busco un negocio sin identificarme
Entonces la operación falla con el mensaje "El usuario no existe"

Escenario: 15.8 Busqueda muestra filtros del negocio
Dado que existe un negocio con el nombre "Cafe Martinez" y filtro "Cafeteria"
Cuando busco un negocio con nombre "Cafe"
Entonces la busqueda muestra en la posicion "1" al negocio "Cafe Martinez"