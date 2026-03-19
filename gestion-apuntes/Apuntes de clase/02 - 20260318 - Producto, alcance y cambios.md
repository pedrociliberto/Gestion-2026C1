## Clase 2 - Producto, alcance y cambios

### Descubrimiento - Ingeniería de requisitos

**Requisito**: condición necesaria para algo. Condición, requerimiento, exigencia, obligación.

- Necesitamos **descubir el producto** que queremos construir.
- A veces el cliente no tiene claro el producto. 
- Hay que distinguir entre **cliente** (quien paga el desarrollo) y el **usuario** (quien usa el producto).
- Se le suele poner foco a *cómo* resolver un problema, pero no le damos importancia a ***qué debemos resolver***.
### Herramientas de descubrimiento

- Elevator Pitch
- Lean Startup
- Lean Canvas
- Lean Inception
- User Story Mapping
- Backlog

*Algunas de estas se definen y explican en la fuente de [Product Discovery de 10Pines](obsidian://open?vault=gestion-apuntes&file=Fuentes%2FFuente%202%20-%2010Pines%20Product%20Discovery).*

#### Historia de usuario

Se compone de:
- **Cómo** [un tipo de usuario]
- **Quiero** [una funcionalidad]
- **Para lograr** [un beneficio]

**Criterios de aceptación**: acerca de una característica/feature. Especifican más en detalle la historia de usuario.
- **Dado** [un estado inicial]
- **Cuando** [acción]
- **Entonces** [resultado]

### Alcance

**Trabajo necesario** para completar los entregables del proyecto.
- Qué haremos y qué no haremos en el proyecto.
- No es lo mismo que las *características* del producto.

El *sprint backlog* es un ejemplo de **especificación de alcance**: habla de las tareas y el trabajo a hacer, no de las características.
#### Cambios de alcance

- Pedidos de clientes u otros interesados
- Mejora del entendimiento de necesidades
- Cambios del entorno
- Riesgos materializados

El alcanze definitivo **se conoce al final del proyecto**. Se elabora de manera **progresiva**.

- Necesitamos un procedimiento de **gestión de cambios**: poner en el contrato qué voy a hacer cuando el cliente pida un cambio
- El cliente se debe acostumbrar a aceptar que **no todos los cambios serán necesariamente aceptados**.
- Solo se deben incluir en el alcance los **cambios aprobados**.
- Implica cambios en cronograma, presupuestos, etc.

#### Cambios - visión ágil

El alcance es **totalmente adaptativo**. 

En **XP**: hay que *"abrazar/adoptar el cambio"*.

En **Scrum**, define el Product Owner sobre el Product Backlog:
- Priorización, agregado o eliminación de PBI (*product backlog items*).
- Sprint Backlog: solo lo puede cambiar el **DevTeam**.
### Scope Creep (deslizamiento)

Agregar características y **ampliar el alcance**, sin tener en cuenta el efecto sobre tiempos, costos y recursos.

- No siempre es malo (*¡si trabajamos por hora nos sirve!*). Pero si afectamos las tres variables puede ser problemático.
- Se evita cuando sabemos cómo transmitirle la información al cliente, en la **forma de comunicarse**. 

### Gold Plating

Se realizan **mejoras sin consentimiento**. Desde el lado del desarrollador, se incorporan en el producto ciertas características que suponen ser "mejoras" sin preguntarle al cliente.

Acarrea varios inconvenientes:
- El cliente puede decir: "no lo quiero", o incluso "sacalo".
- No existen criterios de aceptación para estas *features* agregadas, **no hay validación y es peligroso**.

### Especificación mediante ejemplos

*[Gojko Adzic]* Los ejemplos (de uso del sistema) pueden servir tanto para **especificar requerimientos**, como para **escribir tests**.

- Para desarrollar software correctamente, debo partir de **ejemplos** escritos de forma tal que me sirvan como tests y como pruebas de aceptación.
- Hablamos de **prácticas colaborativas** de construcción de software (participan todos).
- La automatización puede ser útil para la rapidez y replicabilidad. 

## Clase práctica

### Lean Inception

**Product Vision**: define la razón de ser y el valor del producto para el equipo y los *stakeholders*.

- **Para**: usuario objetivo
- **Qué necesita**: problema
- **Nuestro producto es**: tipo de producto
- **Qué permite**: beneficio principal
- **A diferencia de**: alternativa (la competencia, producto/lugar/actividad/empresa)
- **Nuestro producto**: diferenciador

