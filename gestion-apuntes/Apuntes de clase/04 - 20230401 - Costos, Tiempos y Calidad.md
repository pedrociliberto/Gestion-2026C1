## Clase 4 - Costos, Tiempos y Calidad

#### Triángulo de hierro

Hay 3 variables indicadoras para mantener la calidad:
- Alcance
- Costo
- Tiempo

Cualquier modificación de alguna variable implica la modificación de alguna(s) de las otras dos variables. 

#### Terminología

- **Duración**: tiempo calendario para completar una actividad. Se expresa en días/semanas laborales. Asociado a **calendarios y tiempos**.
- **Esfuerzo**: unidades de trabajo reqeueridas para completar una actividad. Se expresa en horas de trabajo (hh). Asociado a **tamaños y costos**.

### Gestión de costos

- **Costo**: lo que le cuesta al desarrollador.
- **Precio**: lo que le cuesta al cliente que compra el producto. Es una cuestión del mercado, debo sacar el mejor posible como desarrollador.

**Cashflow de un proyecto/producto**:

- Se debería garantizar que los **ingresos cubran los costos** (sean mayores).
- Evitar requerir mayor cantidad de recursos (o requerirlos a destiempo).
- Considerar los egresos cuando se producen (sueldos, amortizaciones).
- Obtener financiamento (de otros proyectos, entidades financieras).

**Recursos incluídos**: todos los asignados al proyecto. Medidos en **unidades monetarias** ($) o **esfuerzo** (hh).
- Trabajo
- Materiales
- Equipos (amortizaciones)
- Servicios
- Mitigación de riesgos, contingencias

### Gestión del tiempo

**Actividades**:
- Estimaciones de tiempos (por actividades o más abarcativas)
- Secuenciación/orden de actividades (priorización)
- Duración de actividades
- Cronograma: distribución de actividades (hitos, entregas, etc)
- Técnicas de compresión

#### Compresión de cronograma

- Acorta el cronograma del proyecto, para cumplir con restricciones de fechas sin cambiar el alcance.
- Implica mayores costos y riesgos.
- Se pierden grados de libertad.

Existen diversas **técnicas de compresión** (y consecuentemente sus problemas en la práctica).

**Crashing**: agregar horas o gente.
- No siempre funciona.
- Se suman tiempos de coordinación y transferencia,

**Fast Tracking**: paralelizar trabajo previamente no paralelizado.
- Existe riesgo de re-trabajo.
- Requiere que se ejecute trabajo con información incompleta.

### Gestión de la calidad

"Calidad" es un **atributo del producto**.

- Lo qué más (y realmente) importa es la **calidad del producto**.
- Puede definirse como el momento en el que algunas características **satisfacen las necesidades del cliente**.
- También puede verse como el **valor** que una persona le da a algo.

#### ¿Cómo probamos el producto?

- **Verificación**: que el programador escribió **lo que él mismo quería escribir**.
	- Revisiones de pares
	- Pruebas unitarias (código)
	- Pruebas de componentes
- **Validación**: que el programador escribió **lo que el cliente quería**.
	- **Pruebas de comportamiento** (de usuario sin interfaz de usuario)
	- **UAT** - User Acceptance Test (de punta a punta)
	- Validación **en producción** (A/B, Canary, monitoreo)

#### Regresiones y fallas en producción

- **Regresiones**: fallas debidas a cambios con efectos inesperados.
- **Fallas en producción**: una vez que los usuarios ya lo tienen disponible.

Hay que reparar las fallas y regresiones. Luego, **evidenciarlas con pruebas nuevas**. Algo ocurrió porque no había pruebas en un principio.

#### ¿Reparar o prevenir?

- Las pruebas **no alcanzan para garantizar la calidad**: solo evidencian que hay o no problemas. Idealmente, los problemas no deberían surgir nunca.
- Es mejor no encontrar las fallas... porque **las fallas no están**.
- Usualmente se han introducido fallas antes en el tiempo: hay que mejorar la construcción, y **prevenir antes que reparar**.

#### Calidad desde la prevención

Necesitamos un proceso que asegure/obligue a **incorporar la calidad**: surge el Aseguramiento de la calidad (**QA**).

- Las inspecciones y pruebas **generan retrabajo y desperdicio**, y mayores costos.
- Hay que usar procesos que **incorporen la calidad durante el desarrollo**.

#### Costos de la calidad

La calidad, por supuesto, tiene **costos** asociados.

- **Prevención**: prevenir que los clientes reciban algo defectuoso.
	- **Proceso**: definición, entrenamiento, equipamiento/herramientas, tiempo para hacerlo bien
	- **Evaluación**: pruebas e inspecciones
- **Reparación**: ya tenemos un problema y hay que solucionarlo.
	- **Fallas internas** (antes de entregar): corregir, rehacer, posponer liberaciones, retrabajo y desperdicio.
	- **Fallas externas** (luego de liberar): manejo de quejas, problemas legales, pérdida de prestigio y negocios.
