# HangOut

Este es el repositorio del trabajo práctico del grupo 4 de la materia de Gestión del Desarrollo de Sistemas Informáticos

## IDEs y Agentes de IA usados:
Utilizamos los IDEs de VSCode con su agente de Copilot y usamos AndroidStudio con su agente de IA Gemini.

## Grupo 4 - Alumnos

* **(111859)** Martina Riccardi
* **(111918)** Pedro Tomás Ciliberto
* **(111960)** Mirko Saenz Valiente
* **(111848)** Melanie Belén García Lapegna
* **(112201)** Lucas Ariel Conde Cardó
* **(111750)** Marina Belén Blanco

## Instrucciones de uso
### Para usar el servidor
El codigo de la Web API de nuestra aplicación se encuentra en la carpeta `backend`.

Para ejecutar el proceso en modo desarrollo, primero se debe abrir una *shell* dentro del container
que va a ejecutar la aplicación. Para ello ingresar a la carpeta de backend y en ella ejecutar:

```
./iniciar_shell_dev.sh
```

Estando dentro del container, se puede ejecutar la aplicación con el siguiente comando:

```
./abrir_api_dev.sh
```
### Para usar la App

1. Abrir el proyecto en AndroidStudio en la carpeta `/Hang-Out/frontend/HangOut`.
2. Asegurarse de tener la ip y puerto del equipo que se esté utilizando para correr el servidor en el archivo `frontend/HangOut/app/src/main/java/com/grupo4/hangout/Config.kt` en la constante `BASE_URL`.
   En caso de usar el emulador, usar `10.0.2.2`. En caso contrario, se puede saber la direccion a usar con el siguiente comando en terminal (usar el primer resultado):
```
hostname -I
```  
3. Luego usar un emulador o un celular con Android para correr la app con el botón `Run`.

## Notas sobre el desarrollo dentro del container

Una vez que se tiene el servidor (sus contenedores) en marcha, se puede abrir el proyecto en modo desarrollo. Si se instala la extensión *Dev Containers* y se abre *VSCode* en la carpeta `/backend`, va a aparecer abajo a la derecha una alerta con el titulo *Reopen in container*. Si se hace click, el proyecto se abre dentro del container. Tambien se van a poder usar las extensiones de VSCode del proyecto sin instalarlas en local.

<img width="441" height="98" alt="image" src="https://github.com/user-attachments/assets/9a20f002-26a9-4ee9-bbd9-efd75cca0b0b" />
<br/>

Otra forma de abrir dentro del container:

1. Apretar el simbolito de las flechitas (abajo a la derecha) :p

<img width="436" height="35" alt="image-3" src="https://github.com/user-attachments/assets/13d5c952-2b13-4344-a44e-adfa3250d60a" />


2. Click en *Reopen in Container* (opción 5)

<img width="495" height="255" alt="image-4" src="https://github.com/user-attachments/assets/77f8dc47-1409-4f5b-9ae3-a6bae57c80bb" />

### Pruebas de aceptación

Para ejecutar las pruebas de aceptación, se debe usar el siguiente comando (habiendo ejecutado `./iniciar_shell_dev.sh` antes):

```
python3 tests_aceptacion.py
```

Las pruebas se ejecutan con una base de datos especifica para tests. **No se van a modificar los datos de la BDD usada para desarrollar.**

### Pruebas unitarias

Se pueden ejecutar las pruebas unitarias con el siguiente script (habiendo ejecutado `./iniciar_shell_dev.sh` antes).

```
./tests.sh
```

