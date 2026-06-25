import subprocess
from subprocess import PIPE, STDOUT
import sys
import os
import time

def main():
    entorno_servidor = dict(os.environ)
    entorno_servidor["APP_ENV"] = "test"
    # Creo proceso del servidor con el que se van a ejecutar los tests
    proceso_servidor = subprocess.Popen([sys.executable, "app.py"], env=entorno_servidor, text=True, stdout=PIPE, stderr=STDOUT)

    print("Abriendo servidor...")
    time.sleep(5) # Esperar que abra el servidor
    
    # Creo proceso de tests
    proceso_tests = subprocess.Popen(["/bin/bash", "tests_aceptacion.sh"], text=True, stdout=PIPE, stderr=STDOUT) # Esto ejecuta y espera a que termin
    # Imprimir salida del proceso de tests
    linea_salida = proceso_tests.stdout.readline()
    while linea_salida != "":
        print(linea_salida, end="")
        linea_salida = proceso_tests.stdout.readline()

    codigo_proceso_tests = proceso_tests.wait() # Espero a que cierre el proceso de tests

    # Cierro el proceso del servidor e imprimo el resultado final
    proceso_servidor.send_signal(2)
    if codigo_proceso_tests != 0:
        raise Exception("Fallo la ejecución de los tests!")
    
    print("Ejecución de tests finalizada exitosamente")

main()