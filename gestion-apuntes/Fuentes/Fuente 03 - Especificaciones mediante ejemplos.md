## Migración de sistemas heredados hacia microservicios con el soporte de especificaciones mediante ejemplos

El trabajo de *Carlos Fontela* propone una metodología para recuperar sistemas monolíticos heredados (o *legacy*), **migrando hacia una arquitectura de microservicios**. El proceso se realiza **especificando mediante ejemplos**.

### Software heredado (*legacy*)

Aquél que ha sido relativamente exitoso, pero que cuesta mantenerlo por la falta de conocimiento que hay para poder hacerlo. Podría haberse convertido en heredado porque:

- Los desarrolladores originales ya no trabajan en él.
- No hay formas que permitan probar el código o entender cómo funciona (pruebas de aceptación).
- Hay poca documentación, ya sea técnica o para usuarios.
- Las funcionalidades son poco conocidas por los desarrolladores y usuarios.

### Especificaciones mediante ejemplos

- Trabajar de forma **incremental**, tomando una historia de usuario a la vez.
- Se definen criterios y pruebas de aceptación para cada historia.
- Se parte **de afuera hacia adentro con las funcionalidades del sistema**, mediante varios ciclos de desarrollo con pruebas unitarias y componentes, las cuales llevan al código.
- Usar **ejemplos** que sirvan para probar el sistema, y que todos los roles trabajen con los mismos ejemplos.
- Se construyen en talleres multidisciplinarios.

### Microservicios

Se plantea una aplicación como **conjunto de pequeños componentes**. Cada uno de ellos:

- Corre en su propio proceso y se comunica mediante mecanismos livianos.
- Se desplega independientemente de forma automatizada.
- Puede ser escrito en un lenguaje de programación distinto.

Esta arquitectura surge de la **necesidad de aumentar la velocidad de liberación de productos de software**.

- Los servicios se organizan sobre **capacidades individuales de negocio**.
- Los microservicios incluyen todas las capas del sistema.
- Los servicios se despliegan de forma y en plataformas **independientes**, 
- Hay un límite bien marcado entre microservicios.
- No se controlan centralmente, sino que cada uno posee la lógica de control.

Entre las **ventajas** que surgen de aplicar esta arquitectura están:

- Cada cambio en un microservicio se hace independientemente del resto del sistema.
- Reduce el tiempo de liberación para cada funcionalidad.
- Cada uno puede usar las tecnologías más adecuadas para su propósito.
- El sistema será más escalable y tolerable a fallas.
- Ante un problema, se aisla el microservicio y se lo corrije sin afectar a todo el sistema.
- Permite reemplazar partes del antiguo sistema sin atarse a sus tecnologías o tener que desecharlo.

### Trabajos relacionados

Se observan puntos interesantes de los trabajos mencionados para tratar el mantenimiento de software heredado:

- Se hace foco en el **comportamiento observable** de los módulos por sobre el funcionamiento interno.
- Se plantea envolver comportamiento con capas que expongan nuevas interfaces.
- Se necesita mejorar la mantenibilidad **al mismo tiempo** que se mantiene la funcionalidad heredada.
- Se desea proceder de forma **incremental** para reemplaza software heredado por el nuevo.
#### Enfoques errados

Muchas de las propuestas mencionadas pueden traer problemas y no son totalmente satisfactorias:

- Tirar y reescribir código trae problemas en cuanto a **costo y tiempo de desarrollo**. Los clientes no pueden esperar al desarrollo completo del nuevo sistema sin pedir cambios constantes al anterior.
- Recurrir a manuales de documentación originales es riesgoso ya que usualmente **se encuentran desactualizados o fuera de sincronía con el código**. Lo que funciona es el código, y puede no estar directamente ligado a los documentos.
- El enfoque de pruebas unitarias no nos sirve para entender profundamente **lo que el usuario espera del sistema**. Hay que evidenciar el comportamiento desde la experiencia real de usuarios, y las mejores pruebas son las de **aceptación**.

### Migración hacia microservicios mediante ejemplos

La principal motivación es reconstruir el poco conocimiento que se tiene del sistema heredado. Por eso la práctica de **especificar con ejemplos** (colaborativa y de descubrimiento) se presenta como el **mejor enfoque para recuperar conocimiento**.

- Las **pruebas de aceptación** documentan cómo funciona realmente el sistema.
- Acotar el sistema actual por funcionalidades, **especificándolas con ejemplos** que sirvan como casos de aceptación.
- Cada grupo de funcionalidades se analiza en talleres multidisciplinarios, y se construyen ejemplos que **sirven de documentación para refactorizar, cambiar el sistema, o probar la aplicación**.
- Realizar pruebas de regresión a menudo, y automatizar las pruebas de aceptación.
- Aplicar el método **solo por demanda**, cuando el cliente lo requiera. Hay mayor compromiso de los usuarios, y se invierte tiempo y dinero solo en lo necesario.
- Resulta que las partes del sistema sin tratar no deben ser modificadas necesariamente.
- La migración debe ser **incremental**.