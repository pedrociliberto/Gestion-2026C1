# language: es

Característica: crear una juntada
Antecedentes:
Dado que no se ha registrado actividad en la aplicación

Escenario: 13.1 Juntada creada exitosamente
Dado que estoy registrado
Y que inicie sesion
Cuando creo una juntada con titulo "Cenita" y codigo "ABCD"
Entonces la juntada se crea exitosamente
Y el codigo de la juntada es "ABCD"
Y el organizador de la juntada soy yo

Escenario: 13.2 Juntada sin codigo
Dado que estoy registrado
Y que inicie sesion
Cuando creo una juntada con titulo "Cenita"
Entonces la juntada se crea exitosamente
Y el largo del codigo de la juntada es "4"

Escenario: 13.3 Juntada sin titulo
Dado que estoy registrado
Y que inicie sesion
Cuando creo una juntada sin titulo y con codigo "ABCD"
Entonces la operación falla con el mensaje "No se especifico titulo"

Escenario: 13.4 Juntada con codigo de largo != 4
Dado que estoy registrado
Y que inicie sesion
Cuando creo una juntada con titulo "Cenita" y codigo "ABCDE"
Entonces la operación falla con el mensaje "El codigo debe tener longitud cuatro"

Escenario: 13.5 Juntada con codigo ya usado
Dado que estoy registrado
Y que inicie sesion
Y que existe una juntada con codigo "ABCD"
Cuando creo una juntada con titulo "Cenita" y codigo "ABCD"
Entonces la operación falla con el mensaje "El codigo de juntada ya esta en uso"

Escenario: 13.6 Juntada genera otro codigo si el generado ya esta usado
Dado que estoy registrado
Y que inicie sesion
Y que existe una juntada con codigo "ABCD"
Y que el generador de caracteres va a generar el codigo "ABCD"
Cuando creo una juntada con titulo "Cenita"
Entonces la juntada se crea exitosamente
Y el codigo de la juntada no es "ABCD"