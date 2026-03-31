## Gestión del valor ganado (GVG) y su aplicación

Para implementar la GVG, hay que **definir la Línea Base de Medición del Desempeño (PMB)**.

La **PMB** integra:
- **Alcance**: descripción del trabajo a realizar
- **Cronograma**: plazos para su realización
- **Costo**: cálculo de costos y recursos requeridos para su ejecución

### Elementos básicos

#### Valores principales

- **Valor Planificado (PV)**: valor de la PMB hoy (lo planificado).
- **Valor Ganado (EV)**: lo realizado hasta hoy (el trabajo real).
- **Costo Real (AC)**: costo que ha insumido el trabajo hasta hoy.

El **BAC (Budget at Completion)** es el presupuesto final hasta la conclusión del trabajo.

Los valores se pueden expresar en porcentajes:

- **PV%** = PV / BAC
- **EV%** = EV / BAC
- **AC%** = AC / BAC

#### Variaciones

- **Variación del Cronograma (SV)** = EV - PV
- **Variación del Costo (CV)** = EV - AC

#### Indicadores de rendimiento

- **Índice de Rendimiento del Cronograma (SPI)** = EV / PV
- **Índice de Rendimiento del Costo (CPI)** = EV / AC

### Aplicación de la GVG

- Una buena implementación supone la **integración del alcance, cronograma y costo** en la planificación del proyecto.
- **Alcance**: descomponer el trabajo para crear una EDT (Estructura de Desglose del Trabajo).
- **Cronograma**: se puede hacer un cronograma dinámico para observar cualquier cambio y tomar medidas para corregirlos adecuadamente.
- **Recursos y costos**: cada tarea con recursos necesarios asignados (y sus **tarifas**). Se puede manejar con estimaciones si no hay control de recursos.

### Técnicas de medición de valor ganado (EV)

Cada técnica se basa en cómo son los **entregables** (tangibles/intangibles) y en la **duración de la tarea**.

- **Fórmula fija**: entre ellas están la `0/100` (100% de avance al terminar la tarea), o la `50/50` (50% de avance al evidenciar inicio, y la otra mitad al terminarla). Se permiten otras combinaciones.
- **Hitos ponderados**: tareas relativamente largas, establecer hitos intermedios con **resultados parciales**, asignándoles valores ponderados para establecer avance.
- **Porcentaje completado**: avance parcial respecto al porcentaje completado de la tarea.
	- % de Duración = Duración real hoy / Duración total
	- % de Trabajo = Trabajo real hecho / Trabajo total
	- % de Unidades Físicas = Unidades entregadas / Unidades totales
	- % Físico = Evaluar volúmen físico alcanzado

### Umbrales de calidad

- Existen **márgenes de tolerancia aceptables** para el desempeño de proyectos.
- Los umbrales nos dicen si está dentro o fuera de los **límites de control**. 
- Permite dirigir **atención** hacia los proyectos y tareas que tienen problemas.
- Zonas de tolerancia (verde), de alerta (amarillo), y de problemas (rojo).
- Zonas de muy buen rendimiento (azules) también pueden representar problemas.

### Pasos para implementar la GVG

**INICIO**:
- Definir parámetros iniciales y opciones de software a usar.
- Definir umbrales de calidad para monitorear el proyecto.

**PLANIFICACIÓN**:
- Definir la EDT.
- Definir la **técnica de medición del EV** para cada tarea.
- Definir un cronograma dinámico.
- Asignar recursos/costos a todas las tareas, y la distribución del presupuesto a lo largo del tiempo.
- Establecer la PMB (revisando pasos anteriores y actualizando recurrentemente).

**EJECUCIÓN (seguimiento y control)**:
- Definir la fecha de estado.
- Registrar avance de cada tarea de acuerdo a su técnica de EV.
- Actualizar trabajo remanente para cada tarea.
- Calcular/definir pronósticos
- Proponer acciones correctivas y mantener la integridad de la PMB.
- Entregar informes de desempeño.