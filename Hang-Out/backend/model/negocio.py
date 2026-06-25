from datetime import datetime, time

class Negocio:
    def __init__(self, nombre, descripcion, horarios, ubicacion, sitio_web, filtros, url_ubicacion=None, id=None, tiene_posicionamiento=False):
        self.nombre = nombre
        self.descripcion = descripcion
        self.horarios = horarios
        self.ubicacion = ubicacion
        self.sitio_web = sitio_web
        self.filtros = filtros
        self.id = id # En la documentación del metodo __eq__ se explica la razon de esto
        self.url_ubicacion = url_ubicacion
        self.tiene_posicionamiento = tiene_posicionamiento

    def __eq__(self, otro_negocio):
        """
        **IMPORTANTE**: Para saber si dos negocios son iguales no hay otro remedio que tener un ID.
        
        Es totalmente complejidad accidental y **deberia ser lo unico fuera de la logica de negocio**
        que esta presente en el modelo (entiendo que se acepta el id como unica excepcion, ya que no
        hay otra forma de saber si dos negocios son iguales luego de guardarlo en una DB y 
        recuperarlo mas tarde). Es una consecuencia de tener que persistir objetos en bases de datos.
        """
        return self.id == otro_negocio.id

    def __hash__(self):
        # En general va a estar definido el id
        return self.id if self.id else hash(self.nombre)
    
def analizar_horarios_juntada(self, fecha_inicio: datetime, fecha_fin: datetime) -> str:
    """
    Analiza el horario del negocio con respecto al rango de la juntada.
    Retorna:
    - "OK": Si está totalmente abierto en ese rango.
    - "DIA_CERRADO": El negocio no abre ese día de la semana.
    - "FUERA_HORARIO": Abre ese día, pero la juntada cae 100% fuera de su rango horario.
    - "ARRANCA_ANTES": Abre ese día, pero la juntada empieza antes de la apertura.
    - "TERMINA_DESPUES": Abre ese día, pero la juntada finaliza después del cierre.
    - "MUCHOS_DIAS": La juntada quiere realizarse en varios días y el negocio puede cerrar en el medio o no estar abierto en alguno de esos días.
    """
    if not self.horarios:
        return "OK"

    dias_mapeo = {0: "LU", 1: "MA", 2: "MI", 3: "JU", 4: "VI", 5: "SA", 6: "DO"}
    dia_buscado = dias_mapeo[fecha_inicio.weekday()]
    
    hora_ini_juntada = fecha_inicio.time()
    hora_fin_juntada = fecha_fin.time() if fecha_fin else None

    if fecha_fin and fecha_inicio.date() != fecha_fin.date():
        return "MUCHOS_DIAS"

    bloques_horarios = [h.strip() for h in self.horarios.split(",")]
    
    tiene_este_dia = any(bloque.startswith(dia_buscado) for bloque in bloques_horarios)
    if not tiene_este_dia:
        return "DIA_CERRADO"
    

    for bloque in bloques_horarios:
        if bloque.startswith(dia_buscado):
            try:
                rango_horas = bloque[2:] 
                hora_inicio_str, hora_fin_str = rango_horas.split("-")
                h_inicio = time.fromisoformat(hora_inicio_str)
                h_fin = time.fromisoformat(hora_fin_str)
                
                if h_inicio <= h_fin:
                    if hora_fin_juntada is not None:
                        if hora_fin_juntada < h_inicio or hora_ini_juntada > h_fin:
                            return "FUERA_HORARIO"
                        if hora_fin_juntada > h_fin:
                            return "TERMINA_DESPUES"
                    if hora_ini_juntada < h_inicio:
                        return "ARRANCA_ANTES"
                    return "OK"
                else: 
                    if hora_fin_juntada is not None:
                        if hora_ini_juntada >= h_inicio and hora_fin_juntada <= h_fin:
                            return "OK"
                        if hora_ini_juntada < h_inicio and hora_fin_juntada > h_fin:
                            return "FUERA_HORARIO"
                        if hora_fin_juntada > h_fin:
                            return "TERMINA_DESPUES"
                    if hora_ini_juntada < h_inicio:
                        return "ARRANCA_ANTES"
                        
            except ValueError:
                continue
                
    return "FUERA_HORARIO"
        