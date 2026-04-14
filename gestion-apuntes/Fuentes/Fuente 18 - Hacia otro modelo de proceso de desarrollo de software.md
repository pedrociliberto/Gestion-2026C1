## Hacia otro modelo de proceso de desarrollo de software

### 1. Resumen / Abstract

El artículo explora la evolución del desarrollo de software, desde el abandono del modelo manufacturero (pasos sucesivos y roles contrapuestos) hacia un modelo de **actividades simultáneas y equipos plurifuncionales**. Se analiza el impacto de la entrega incremental y continua, donde clientes y desarrolladores colaboran estrechamente.

### 2. Introducción

#### 2.1. Ingeniería y desarrollo de software

- **Definición:** La Ingeniería de Software ha evolucionado de una visión mecánica de codificación a un enfoque empírico y científico para soluciones eficientes. 
- **Alcance:** El "desarrollo" abarca desde la concepción del producto hasta su despliegue y operación, asegurando que agregue valor al usuario.
#### 2.2. El cambio en el proceso de desarrollo

- Los procesos cambian no solo por la tecnología, sino por la demanda de los negocios que el software soporta.    
- Existe una necesidad definitiva de mayor seguridad, usabilidad y resiliencia en un mundo donde el software es ubicuo.
### 3. Objetivos

- Establecer las razones de la evolución continua del software.
- Explicar las características del modelo de proceso de la última década.
- Mostrar evidencia empírica de sus ventajas y reconocer su coexistencia con modelos anteriores.
### 4. Breve reseña de enfoques metodológicos

#### 4.1. Familias de métodos en el tiempo

El autor clasifica la evolución en etapas:

1. **Sin metodología (hasta 1960s):** Programación y pruebas artesanales.
2. **Cascada (1960s-1970s):** Etapas sucesivas disjuntas copiadas de la industria manufacturera.
3. **Cascada flexibilizada (1970s-1990s):** Admite vueltas atrás parciales.
4. **Primeros métodos iterativos (1995-2005):** Como el Proceso Unificado; se construye por iteraciones con varias actividades en cada una.
5. **Ágiles de primera generación (2000-actualidad):** Foco en personas, valor al cliente y entrega incremental.
6. **De flujo continuo (2014-actualidad):** Modelo "Lean-Continuo" donde el producto puede entregarse en cualquier momento sin esperar al fin de una iteración.
#### 4.2. Disciplinas del desarrollo de software en el tiempo

- Se observa una transición de **roles por actividad** hacia equipos **DevOps/DevSecOps**.
- El control de calidad pasa de ser una etapa final a estar integrado mediante **pruebas automatizadas** y monitoreo en producción.
### 5. Razones del cambio

#### 5.1. El software en el centro del negocio

- "Toda compañía es una compañía de software". El software es el activo más importante en empresas como Netflix, Tesla o Amazon.
- El modelo de proyectos a largo plazo ya no es funcional; se requieren ciclos cortos para mantener la competitividad.
#### 5.2. El software debe poder cambiar cada vez más rápido

- La maleabilidad y extensibilidad son propiedades únicas que permiten el cambio frecuente.
- Las empresas han pasado de liberar software mensualmente a hacerlo en horas para obtener feedback inmediato.    
#### 5.3. Los nuevos enfoques funcionan

- La adaptación de procesos es necesaria porque los nuevos modelos han demostrado éxito en la industria y la academia.
### 6. Un enfoque moderno y con lógica interna

#### 6.1. El proceso en pocas palabras

- El modelo se basa en trabajar pequeños conjuntos de características con valor para el cliente.
- Se utiliza una línea de producción (_pipeline_) que lleva cada característica desde el _backlog_ hasta el despliegue productivo.
### 7. Diversidad de nombres y conceptos

- El autor analiza términos como **Entrega Continua (CD)**, **Digital Continuo**, **Lean Software Development** y **DevOps**.
- **DevOps** se define como un enfoque organizacional que enfatiza la empatía y la colaboración para acelerar la entrega de cambios.
### 8. Novedades del modelo propuesto

- **Gestión:** Se basa en _Lean Management_ y _Lean Startup_: limitar el trabajo en curso (WIP), gestión visual y experimentación continua.    
- **Especifiación:** Evoluciona hacia **Specification by Example (SBE)**, usando ejemplos reales como guías de desarrollo y pruebas de aceptación.
- **Arquitectura:** Requiere componentes desacoplados (como **microservicios**) que permitan despliegues independientes y autónomos.
- **Equipos:** Se prefieren equipos **plurifuncionales** (donde el equipo como conjunto posee todas las habilidades) sobre el mito del "desarrollador full-stack" individual.
#### 8.1. Del modelo de proyecto al modelo de producto

- Se abandona la noción de "proyecto" (algo con fin determinado) por la de "producto" (algo que evoluciona mientras el negocio exista).
- Esto elimina la resistencia al cambio propia de los planes rígidos y se enfoca en la actividad continua.
### 9. Evidencia empírica: el modelo funciona

- Basado en investigaciones de Forsgren, Humble y Kim, se demuestra que las organizaciones de alto desempeño logran:
    - Entregas más rápidas y frecuentes.
    - Menores niveles de agotamiento (_burnout_) en el personal.
    - Mayor satisfacción de los clientes.
### 10. Sistemas ciberfísicos

- Sistemas complejos donde el software controla dispositivos físicos.
- El enfoque Lean-Continuo enfrenta desafíos aquí: las pruebas en producción pueden ser peligrosas y las regulaciones de seguridad son más estrictas.
### 11. Discusión y Conclusiones

- El modelo no es necesariamente definitivo; la industria sigue evolucionando.
- No todos los escenarios son aptos para microservicios (por costo) o para abandonar el modelo de proyectos inmediatamente.
- La conclusión principal es que el software ha pasado de ser un proceso manufacturero a un **proceso continuo de generación de valor y conocimiento**.