## Indicadores Objetivos para Control de Proyectos

Los proyectos de software suelen terminar con **desvíos en costo y calendario** (quizás directamente no terminan).

- No se suele contar con **herramientas de control**.
- Los líderes se hacen estas preguntas: ¿Cuál es el grado de avance? ¿En qué fecha terminaremos? ¿Cuánto gastaremos realmente?

### ¿Cuál es el grado de avance?

#### Problema

Se cometen errores clásicos al medir alcance:
- **Avance por calendario**: solo se tiene en cuenta el paso del tiempo para definir un porcentaje.
- **Avance por código completo**: se ve únicamente la porción del código completa, sin tener en cuenta su estabilidad.
#### Indicador de Funcionalidad Completa

*No hay avance si la funcionalidad no está completa.
La funcionalidad no está completa si no está **desarrollada, probada y estabilizada**.*

Se definen ***Puntos de Funcionalidad Completa (PFC)*** en base a los pesos de cada funcionalidad, y a medida que se completan, **los puntos se acumulan**.

1. **Determinar funcionalidades**: dividir el producto en partes, ¿cuántas se generan?
2. **Asignar pesos a funcionalidades**: encontrar el costo de cada una. Jerarquizarlas según **pesos** si no se encuentran los costos exactos. Hay distintas opciones:
	- Clasificarlas en **Simples** (1), **Medianas** (2) o **Complejas** (3).
	- Variante usando **5 estados** en vez de 3.
	- Definir peso en base a la **cantidad de esfuerzo** estimado para construir cada funcionalidad.
3. **Estimar la fecha** en que la funcionalidad estará completa.
4. Registrar **fechas reales** de cada funcionalidad (comparando con las estimadas).

![[Indicador de Funcionalidad Completa.png]]

##### Informacion adicional

- **Curva de código completo**: cuándo se entrega código nuevo al equipo de control de calidad. No es probado ni estabilizado.
- **Curva de funcionalidad aprovada por usuario**: validada por el usuario final. Avance más seguro, pero no se tiene avance semanal con la curva. 
- **Productividad**: dividir los PFC reales sobre la unidad de tiempo. Permite extrapolar los tiempos para predecir el final del proyecto.

##### Consideraciones

- **Validez**: en etapa de construcción (no se usa al final).
- **Proceso**: no genera avance si el equipo no quiere cerrar temas (indicador binario: *completa* o *no completa*).
- **Síndrome del 0%**: si solo registramos completas a las que no tienen ningún defecto, entonces no podremos ver avance (siempre habrá algún defecto pendiente).
- **Funcionalidad = código**: solo considera funcionalidad al producto final para el usuario.

#### Indicador de Nivel de Calidad

- Necesidad de usar **estados intermedios**.
- Muestran el **ciclo de vida** del producto en base a su calidad.
- Se pueden hacer distintos **análisis de avance** con diversos niveles de detalle: analizar porcentajes de productos terminados en base a cantidad de **funcionalidades aprobadas por usuario**.

![[Indicador de Nivel de Calidad.png]]

### ¿En qué fecha terminaremos?

#### Problema

- Hay incertidumbre frente al costo de tiempo y recursos de la **estabilización del producto**.
- La etapa de estabilización arranca cuando está todo construido: hay que **corregir defectos pendientes**. 

#### Indicador de Evolución de la Prueba

Medir los defectos, cuántos aparecen y cuántos se cierran por día.

1. Registrar los **defectos nuevos** a medida que aparecen.
2. Registrar sus cambios de estado hasta que se cierran definitivamente.

![[Indicador de Evolucion de la Prueba.png]]

Permite obtener estadísticas que determinan:
- La **velocidad de corrección**.
- El **tiempo estimado** para estabilizar el producto.
- Qué tipo de **conclusiones** se obtienen (el proyecto está bajo control, o aparecen más defectos que los que se cierran por día).
##### Consideraciones

- **Validez**: durante todo el proyecto cuando hay prueba en paralelo.
- **Proceso**: útil para detectar el síndrome del 90%. Ayuda a detectar la correctitud del proceso de desarrollo y prueba en paralelo.

#### Indicador de Cobertura de la Prueba

Esta medición se realiza a partir de los **estados de los casos de prueba**:
- **Planificados**: cantidad de casos a ejecutar.
- **Disponibles**: se pueden ejecutar habiendo sido entregados al equipo de prueba.
- **Ejecutados**: los que el equipo de prueba pudo ejecutar.
- **Ejecutados OK**: casos ejecutados sin errores. 

![[Indicador de Cobertura de la Prueba.png]]

### ¿Cuánto gastaremos realmente?

#### Indicador de Earned Value (EV)

- **Alcance**: separar costos que no están asociados directamente a la **construcción del software**.
- **Validez**: solo aplica a la construcción.
- **Margen de error**: se aceptan errores para no aumentar la carga administrativa:
	- Los pesos generan información inexacta.
	- El Actual se basa en funcionalidades no completadas.
	- Los costos indirectos de los PFC pueden generar márgenes de error.

### Conclusiones

Se sacan conclusiones al analizar este tipo de indicadores y su impacto en los proyectos:

- **Objetividad**: información objetiva, basada en evidencia física y no opiniones subjetivas.
- **Administración**: no deben poseer demasiada carga administrativa para lograr eficiencia.
- **Foco en resultados**: no miden inversión, sino lo que se obtienen de ella.
- **Comprensión**: deben mostrar información comprensible por el auditorio.