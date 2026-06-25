import os
import string
import random

LONGITUD_CODIGO = 4

class ProveedorCodigos:
    def __init__(self):
        self.ya_mostro_codigo = False

    """
    ProveedorCodigos representa una clase de objetos capaces de generar
    un codigo para una juntada.

    La intencion de esta clase es poder testear el comportamiento cuando la aplicación
    genera un codigo determinado (no se puede de otra manera, porque el codigo 
    se genera aleatoriamente).
    """
    def generar_codigo(self):
        """
        PRE: Nada.

        POST:

        - Si la variable de entorno `APP_ENV` es `prod`: genera un codigo aleatorio 
          con la longitud `LONGITUD_CODIGO` y lo devuelve
        
        - Si la variable de entorno `APP_ENV` es `test` y se definio la variable de entorno
          `CODIGO_A_GENERAR`, entonces se devuelve el valor de esa variable.

        - Si la variable de entorno `APP_ENV` es `test` y no se definio la variable de entorno
          `CODIGO_A_GENERAR`, entonces se genera un codigo aleatorio con la longitud 
          `LONGITUD_CODIGO` y lo devuelve
        """

        if os.environ.get("APP_ENV", "prod") == "test": 
            return self._generar_codigo_test()
        else:
            return self._generar_codigo_produccion()
    
    def _generar_codigo_test(self):
        if "CODIGO_A_GENERAR" in os.environ and not self.ya_mostro_codigo:
            self.ya_mostro_codigo = True
            return os.environ["CODIGO_A_GENERAR"]
        
        return self._generar_codigo_produccion()

    def _generar_codigo_produccion(self):
        caracteres_validos = string.ascii_letters + string.digits
        caracteres_codigo = []

        for _ in range(LONGITUD_CODIGO):
            caracteres_codigo.append(random.choice(caracteres_validos))
        
        return "".join(caracteres_codigo)