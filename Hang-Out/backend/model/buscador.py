from model.excepciones import ExcepcionSistema
from repositorios.repositorio_filtro import RepositorioFiltro

REEMPLAZO_ACENTOS = {
            "á": "a",
            "é": "e", 
            "í": "i", 
            "ó": "o", 
            "ú": "u"
        }

class Buscador:
    def __init__(self, repositorio_negocio):
        self.repositorio_negocio = repositorio_negocio
    def buscar_negocio(self, busqueda, filtros_usuario=[], filtros_seleccionados=[], filtros_dia=None, filtro_hora_desde=None, filtro_hora_hasta=None, usar_filtros_usuario=False):
        if not busqueda or not str(busqueda).strip():
            resultado = self.repositorio_negocio.listar_todos()
        else:
            resultado = self.buscar_negocio_por_nombre(busqueda)

        # Filtro por caracteristicas si es que el usuario selecciono
        if filtros_seleccionados:
            resultado = self._filtrar_por_filtros(resultado, filtros_seleccionados)

        # Filtro por dia(s): ahora soporta lista de días (OR entre ellos)
        if filtros_dia:
            resultado = self._filtrar_por_dias(resultado, filtros_dia)
        
        # Solo filtra si el usuario puso al menos uno de los dos horarios
        if filtro_hora_desde is not None or filtro_hora_hasta is not None:
            resultado = self._filtrar_por_horario(resultado, filtro_hora_desde, filtro_hora_hasta)

        # Filtra los negocios que tengan al menos un filtro en común con el usuario, si el usuario eligió usar sus filtros
        if usar_filtros_usuario:
            resultado = self._filtrar_por_filtros(resultado, [filtro.nombre for filtro in filtros_usuario])

        resultado_ordenado = self._ordenar_negocios_por_filtros(resultado, filtros_usuario)
        
        #Se aplica un segundo ordenamiento que pone primero a los que tienen beneficio de posicionamiento
        resultado_ordenado = self._ordenar_por_beneficio_posicionamiento(resultado_ordenado)


        return resultado_ordenado

    def buscar_negocio_por_nombre(self, busqueda):
        texto_normalizado = self._obtener_texto_normalizado(busqueda)
        resultado = self.repositorio_negocio.buscar_por_nombre_en_minusculas(texto_normalizado)

        resultado = [] if not resultado else resultado

        for palabra in texto_normalizado.split(" "):
            coincidencias_palabra = self.repositorio_negocio.buscar_por_contencion_de_palabra(palabra)
            coincidencias_palabra = [] if not coincidencias_palabra else coincidencias_palabra
            resultado += coincidencias_palabra

        resultado = list(set(resultado))
        return resultado


    def _obtener_texto_normalizado(self, texto):
        texto = texto.lower()
        
        caracteres = []
        for caracter in texto:
            if caracter in REEMPLAZO_ACENTOS:
                caracter = REEMPLAZO_ACENTOS[caracter]
            caracteres.append(caracter)
        
        return "".join(caracteres)
    
    def _ordenar_negocios_por_filtros(self, negocios, filtros_usuario):
        resultado_ordenado = []
        
        nombres_filtro_usuario = [filtro.nombre for filtro in filtros_usuario]
        for negocio in negocios:
            nombres_filtro_negocio = [filtro.nombre for filtro in negocio.filtros]
            filtro_en_comun = False

            for nombre in nombres_filtro_negocio:
                if nombre in nombres_filtro_usuario:
                    filtro_en_comun = True
            
            if filtro_en_comun:
                resultado_ordenado.insert(0, negocio)
            else:
                resultado_ordenado.append(negocio)
        
        return resultado_ordenado

    def _ordenar_por_beneficio_posicionamiento(self, negocios):
        """
        Pone primero los negocios que tienen activo el beneficio
        de 'Mejor posicionamiento', respetando el orden relativo del resto
        """
        con_beneficio = []
        sin_beneficio = []

        for negocio in negocios:
             valor = getattr(negocio, 'tiene_posicionamiento', False)
             if valor:
                con_beneficio.append(negocio)
             else:
                sin_beneficio.append(negocio)

        return con_beneficio + sin_beneficio

    
    def _filtrar_por_filtros(self, negocios, filtros_seleccionados):
        set_filtros = set(filtros_seleccionados)
        
        resultado = []
        for negocio in negocios:
            nombres_negocio = {filtro.nombre for filtro in negocio.filtros}
            if set_filtros.issubset(nombres_negocio):
                resultado.append(negocio)

        return resultado

    def _filtrar_por_dias(self, negocios, dias_buscados):
        """
        Filtra negocios que tengan horario en AL MENOS UNO de los días buscados (OR).
        dias_buscados puede ser una lista ["LU", "MA"] o un string "LU" (por compatibilidad).
        """
        if isinstance(dias_buscados, str):
            dias_buscados = [dias_buscados]

        if not dias_buscados:
            return negocios

        resultado = []
        for negocio in negocios:
            if negocio.horarios:
                bloques = negocio.horarios.split(",")
                for bloque in bloques:
                    bloque_strip = bloque.strip()
                    # El bloque comienza con la sigla del día (2 chars)
                    if any(bloque_strip.startswith(dia) for dia in dias_buscados):
                        resultado.append(negocio)
                        break
        return resultado

    # Mantenemos el método original como alias por compatibilidad
    def _filtrar_por_dia(self, negocios, dia_buscado):
        return self._filtrar_por_dias(negocios, [dia_buscado])

    def _filtrar_por_horario(self, negocios, hora_desde_buscada=None, hora_hasta_buscada=None):
        resultado = []
        for negocio in negocios:
            if self._negocio_cubre_rango(negocio.horarios, hora_desde_buscada, hora_hasta_buscada):
                resultado.append(negocio)
        return resultado
    
    def _negocio_cubre_rango(self, horarios_str, hora_desde_buscada=None, hora_hasta_buscada=None):
        if not horarios_str:
            return False

        try:
            pedida_desde_min = None
            pedida_hasta_min = None

            if hora_desde_buscada:
                h, m = map(int, hora_desde_buscada.split(":"))
                pedida_desde_min = h * 60 + m

            if hora_hasta_buscada:
                h, m = map(int, hora_hasta_buscada.split(":"))
                pedida_hasta_min = h * 60 + m

            bloques = horarios_str.split(",")
            for bloque in bloques:
                bloque = bloque.strip()
                if len(bloque) < 13:
                    continue

                desde_str = bloque[2:7]
                hasta_str = bloque[8:13]

                negocio_desde_min = int(desde_str[0:2]) * 60 + int(desde_str[3:5])
                negocio_hasta_min = int(hasta_str[0:2]) * 60 + int(hasta_str[3:5])

                cumple = True

                if pedida_desde_min is not None:
                    cumple = cumple and (
                        negocio_desde_min <= pedida_desde_min and
                        negocio_hasta_min >= pedida_desde_min
                    )

                if pedida_hasta_min is not None:
                    cumple = cumple and (
                        negocio_desde_min <= pedida_hasta_min and
                        negocio_hasta_min >= pedida_hasta_min
                    )

                if cumple:
                    return True

            return False
        
        except Exception:
            return False