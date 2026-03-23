## The New User Story Backlog is a Map

### Problemas del backlog plano

El autor propone varios puntos importantes acerca de la inutilidad del User Story Backlog:

- Ordenar las historias según el orden de implementación **no ayuda a entender lo que hace el sistema**. Si los interesados nos preguntan "¿qué hace tu sistema?", seguro no podamos responderles usando estas historias.
- Entender todo el sistema es la parte difícil del desarrollo de software. Usualmente los equipos Agile pierden el foco principal del sistema.
- Se necesita contexto para realmente contar una historia acerca del sistema.
- El backlog plano no ayuda a determinar si uno ha identificado todas las historias (y correctamente).

### Constuir un Story Map

#### Utilidades

Se proponen varias nociones acerca del Story Map y sus utilidades:

- Organizar las historias de usuario en formas útiles: crear **mapas de historias de usuario**.
- Arriba de todo están las "big stories", también llamadas **USER ACTIVITIES**. Son grandes actividades que hacen las personas, usualmente con muchos pasos, y no necesariamente siguiendo un recorrido preciso. 
- Abajo de ellas existen las **USER TASKS**, tareas que hacen los usuarios para llegar a sus objetivos. Se colocan en forma de grilla debajo de las grandes actividades.
- La grilla consiste de una línea temporal moviéndose de izquierda a derecha, para priorizar **hacer una tarea antes que la otra**, en el orden temporal indicado.
- Para definir el orden, es útil **explicar qué hace el sistema, qué actividades se pueden hacer**: el orden de la respuesta puede usarse para definir el orden temporal de las actividades y tareas del mapa.
- Se construye el mapa en una forma que **ayude a contar la razón del sistema**.

#### Epics

En ciertos momentos aparecen las grandes historias llamadas **EPICS**. 

- Esa historia es **contexto**: no se debe quitar del mapa. 
- Es una forma simple de pensar acerca de la **actividad completa** que se quiere tratar con esa historia.
- También sirve para explicarle a otros rápidamente de qué se trata el sistema.

#### Colocar el mapa ya terminado

El autor habla de **caminar alrededor del mapa** ya confeccionado. 

- Se puede tener una mejor discusión con los interesados acerca de las historias **cuando el mapa es visible para todos**.
- Permite tocar (físicamente) los puntos importantes cuando se está hablando de ellos.
- También es útil para encontrar cosas que faltan en el mapa.

#### Estructura del Story Map

- Las **user activities** (historias grandes) conforman el **backbone** (columna vertebral) del mapa.
- Las **user tasks** (historias pequeñas) conforman el **skeleton** (esqueleto) del mapa.
- Se priorizan las *ribs* que conforman el esqueleto; aquellas que aparecen por debajo de las user activities (columna vertebral).
- Se colocan más arriba las más necesarias, y más abajo las menos prioritarias.
- Cuando se construye así, todas las historias colocadas más arriba (por ejemplo, en la primera fila del esqueleto) conformarán el **sistema minimal que puede dar la funcionalidad necesaria** (*end to end*).
- No se priorizan los items de la columna vertebral. Todos ellos son **esenciales**: se necesitan para **completar el MVP (*Minimum Viable Product*)**.