from datetime import datetime
from zoneinfo import ZoneInfo

class ProveedorHorario:
    def __init__(self):
        self._hora_fue_seteada = False

    def horario_actual(self):
        if self._hora_fue_seteada:
            return self._generar_hora_seteada()
        
        return self._generar_hora_default()

    def setear_hora(self, hora):
        self._hora_fue_seteada = True
        self._hora_seteada = hora

    def _generar_hora_seteada(self):
        return self._hora_seteada
    
    def _generar_hora_default(self):
        tz_ba = ZoneInfo("America/Argentina/Buenos_Aires")
        ahora_ba = datetime.now(tz_ba).replace(tzinfo=None)
        return ahora_ba