## 11 - Software Estimation: What Is An "Estimate"?

### Definiciones

**Estimate**: como una predicción de cuánto tiempo va a tardar en completarse un proyecto, o cuánto va a costar.

**Target**: la sentencia de un objetivo deseado. 

**Commitment**: promesa de entregar funcionalidad definida a un cierto nivel o calidad, para una fecha específica.

- Aunque los *targets* sean deseables o incluso obligatorios, **no necesariamente significa que sean alcanzables**.
- Se recomienda **distinguir** las definiciones de estimaciones, *targets* y *commitments*.
- Un *commitment* puede llegar a ser más "agresivo" o "conservador" que un *estimate*.

### Estimates vs. Plans

- Los **planes** deben ser procesos dedicados a buscar y cumplir los objetivos, centrándose en el proyecto de manera **subjetiva**.
- Los **estimates** deben ser **objetivos**, ya que buscan ser precisos sin buscar un resultado específico.
- Los estimates son la base para formular los planes: 
	- Si son muy diferentes a los *targets*, los planes tendrán que amoldarse a ello y reconocer que habrá que tomar riesgos.
	- Si los estimates están cerca de los *targets*, entonces los planes podrán asumir menos riesgos.

### Probabilidad estimada

- Estaría mal pensar que la probabilidad de que el proyecto termine en la fecha planeada es del 100%. 
- También sería erróneo pensar que la curva de probabilidad sigue una distribución normal, ya que la simetría a ambos lados es bastante ideal y poco realista.
- Usualmente la curva arranca **truncada, y más adelante en el tiempo (del lado izquierdo)**. Al otro lado del gráfico, el proyecto puede durar mucho más tiempo y terminar bastante más tarde, por lo que **el final de la curva se extiende ampliamente**.

![[Probability Estimates.png]]

La parte clave es que **toda estimación incluye probabilidades**, y que ninguna fecha particular se obtiene para sí misma el 100% de chances.

Se recomienda estimar **por rango** (*tardaremos de X a Y semanas*), por **mejor y peor fecha** (en el *mejor escenario, solo X semanas, en el peor, Y semanas*), o por **porcentaje de confianza** (*estamos 80% seguros que terminaremos en Z semanas*).

### Estimates & Project Control

- Las estimaciones deben estar respaldadas por un buen **manejo y control del proyecto**.
- Luego de estimar y tomar compromisos para entregar algo en una fecha determinada, se **controla el proyecto** para alcanzar el *target*.
- Entre estos controles se incluyen: quitar requerimientos no críticos, redefinir requerimientos, reemplazar *staff* menos experimentado por otros más experimentados, etc.
- Siempre ocurren cambios a lo largo del desarrollo: deben resolverse estas cuestiones controlando el proyecto para intentar alcanzar el *target*, aunque el proceso haya cambiado en el medio.

### Propósitos de las estimaciones

- Predecir el resultado de un proyecto no es lo más importante.
- Hay que determinar si los *targets* son lo suficientemente realistas para **permitir que el proyecto sea controlado para alcanzarlos**.
- No deben ser totalmente precisos, sino **útiles**. 
- Una buena estimación es la que provee una visualización clara de la **realidad del proyecto** para permitir a los líderes tomar buenas decisiones de **cómo controlar el proyecto para llegar a sus *targets***.

## 12 - Estimaciones (video de cátedra)

- Toda estimación debería ser un **input para tomar decisiones**.
- Es una herramienta que sirve para **seguir hablando**.

La estimación **predice la duración o costo de la actividad**.
Los objetivos son las enunciaciones de los **logros deseables del negocio**.

- Sería ideal que los tiempos de los objetivos se alíneen con la estimación inicial.
- La existencia de los objetivos **no cambia la estimación**.

Las estimaciones son **rangos, no valores únicos**. Son **afirmaciones probabilísticas**.

- Hay un **tiempo (y costo) mínimo**: se tiene una cota inferior en la función de probabilidad.
- Todo proyecto **implica incertidumbre**: hay riesgos, desconocimiento, y deslizamiento de alcance.
- Hay que **comunicar la incertidumbre**: expresar con rangos/probabilidades. 
- Convencer a otros de la **naturaleza probabilística** de los proyectos.

Se puede hablar de la **precisión**:

- Suele depender de la **experiencia del estimador** y del nivel de refinamiento de la definición.
- **No depende del tiempo que se dé para estimar**.
- A medida que el proyecto avanza, se puede ir midiendo. El cliente conoce mejor lo que necesita y lo lograble. Los riesgos se materializan (o no).
- **La información va mejorando con el tiempo** (y la estimación más precisa).