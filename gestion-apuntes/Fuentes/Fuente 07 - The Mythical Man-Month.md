## The Mythical Man-Month

### Falta de tiempo

Los proyectos de software han presentado **falta de tiempo de calendario**.

- Las **técnicas de estimación** son pobremente desarrolladas. Reflejan estados utópicos de que "todo va a salir bien".
- Las técnicas **confunden esfuerzo con progreso**, como si la cantidad de trabajadores o meses de trabajo fuesen intercambiables.
- El progreso del cronograma no se monitorea correctamente. 
- Frecuentemente se quieren resolver los problemas **agrenando más trabajadores o más tiempo de trabajo (extra)**.

### Optimismo

Los programadores son bastante **optimistas**, usualmente creen que "todo va bien" y que las cosas van a salir siempre como deberían hacerlo. 

- En el área de programación, se construyen programas **salidos de las ideas**.
- Se suele esperar poca dificultad al implementar, pero las ideas suelen fallar, y se producen bugs.
- Con una sola tarea, asumir que funcionará define una **probabilidad**. Podría llevarse a cabo correctamente. Pero con muchas tareas, la probabilidad de que todas vayan bien y sin demoras **es muy chica**.

### The Man-Month

- El costo varía dependiendo de la **cantidad de trabajadores y de meses** involucrados en el proyecto. **El progreso no.**
- Serían intercambiables si las tareas se pudiesen dividir **sin comunicación entre ellas**.
- Agregar comunicación hace que se utilice tiempo para **entrenar y comunicar** a los trabajadores dentro del proyecto.
- Aunque con más trabajadores el tiempo del proyecto puede disminuir, agregar más relaciones en el medio puede hacer que la demora vuelva a aumentar.

### Testing

Se debe darle importancia al tiempo de *testing* en el proyecto. El autor propone:

- 1/3 para planificación
- 1/6 para codear (programar)
- 1/4 para testear componentes y sistemas iniciales
- 1/4 para testear el sistema completo

De aquí se sacan distintas conclusiones:
- La parte de **planificación** es más grande que lo normal. Incluso podría faltar más porcentaje.
- La mitad del cronograma se dedica a **debuggear código completo**.
- La parte más fácil de estimar (**código**) solo recibe 1/6 del tiempo.

### Conclusiones

- Agregar mano de obra a un proyecto tardío **hace que se atrase aún más**.
- No se puede tener **cronogramas amenos** y trabajables, usando más trabajadores y menos meses de trabajo.
- La máxima **cantidad de trabajadores** depende de la cantidad de tareas independientes a realizar.