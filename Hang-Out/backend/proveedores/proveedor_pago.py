from datetime import date

class ProveedorPago:
    
    def validar_tarjeta(self, numero, mes, anio, cvv, nombre):
        """
        Valida que los datos de la tarjeta sean correctos (formato de juguete).
        Lanza ValueError con el mensaje de error específico si algo falla.
        """
        # numero: solo digitos, entre 13 y 16 caracteres
        numero_limpio = numero.replace(" ", "").replace("-", "")
        if not numero_limpio.isdigit() or not (13 <= len(numero_limpio) <= 16):
            raise ValueError("El número de tarjeta debe contener entre 13 y 16 dígitos.")
        
        # Algoritmo de Luhn (validación estándar de números de tarjeta)
        if not self._luhn(numero_limpio):
            raise ValueError("El número de tarjeta no es válido.")

        # Nombre: no vacío
        if not nombre or not nombre.strip():
            raise ValueError("El nombre del titular es requerido.")
        if not mes:
            raise ValueError("El mes de vencimiento es requerido.")
        if not anio:
            raise ValueError("El año de vencimiento es requerido.")
        
        # CVV: 3 o 4 dígitos
        if not str(cvv).isdigit() or not (3 <= len(str(cvv)) <= 4):
            raise ValueError("El código CVV debe contener 3 o 4 dígitos.")
        
        # Vencimiento: no puede ser en el pasado
        hoy = date.today()
        if not (1 <= mes <= 12):
            raise ValueError("Mes de vencimiento inválido.")
        if anio < hoy.year or (anio == hoy.year and mes < hoy.month):
            raise ValueError("La tarjeta está vencida.")

    def cobrar(self, numero, _mes, _anio, _cvv):
        """
        Simula el cobro. En un sistema real llamarías a una API de pagos.
        Para simular fallos: si el número termina en 0000, el pago falla.
        """
        numero_limpio = numero.replace(" ", "").replace("-", "")
        if numero_limpio.endswith("0000"):
            return False  # Simula fallo de pago
        return True

    def _luhn(self, numero):
        """
        Algoritmo de Luhn para validar números de tarjeta. Verifica que el número de tarjeta cumpla con una fórmula matemática específica para detectar errores comunes de digitación.
        Lo que hace es sumar los dígitos de la tarjeta, multiplicando por 2 cada segundo dígito desde la derecha, y restando 9 a los resultados mayores que 9. Si el total es divisible por 10, el número es válido.
        """
        total = 0
        reverso = numero[::-1]
        for i, digito in enumerate(reverso):
            n = int(digito)
            if i % 2 == 1:
                n *= 2
                if n > 9:
                    n -= 9
            total += n
        return total % 10 == 0
