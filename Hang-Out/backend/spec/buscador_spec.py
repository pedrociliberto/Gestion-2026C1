import unittest
import sys
from unittest.mock import Mock

sys.path.append("/backend")
from model.negocio import Negocio
from model.buscador import Buscador
from model.excepciones import ExcepcionSistema

# Nota: los anteriores tests de Buscador estan en los tests de Sistema,
# porque esta clase surgio de un refactor donde movi la responsabilidad
# de hacer la busqueda desde sistema.
class TestBuscador(unittest.TestCase):
    def setUp(self):
        self.repositorio_negocio = Mock()
        self.buscador = Buscador(self.repositorio_negocio)

    def test_01_buscador_encuentra_resultado_con_coincidencia_parcial(self):
        negocio = Negocio(
            "Cafe Tortoni",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [],
            1
        )
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocio.buscar_por_contencion_de_palabra.side_effect = lambda x: [negocio] if x == "cafe" else None

        resultado = self.buscador.buscar_negocio("Café Martínez")

        self.assertEqual(resultado, [negocio])

    def test_02_buscador_con_busqueda_vacia_devuelve_todos_los_negocios(self):
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocio.buscar_por_contencion_de_palabra.side_effect = None
        negocio1 = Negocio("Negocio1", "", "", "", "", [], 1)
        negocio2 = Negocio("Negocio2", "", "", "", "", [], 2)
        self.repositorio_negocio.listar_todos.return_value = [negocio1, negocio2]

        resultado = self.buscador.buscar_negocio("")

        self.assertEqual(resultado, [negocio1, negocio2])
    
    def test_03_buscador_pone_primero_los_resultados_con_filtros_en_comun(self):
        filtro_cafeteria = Mock() # No esta modelado, uso un mock para testearlo
        filtro_cafeteria.nombre.return_value = "Cafeteria"
        negocio_interes = Negocio(
            "Cafe",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [filtro_cafeteria],
            1
        )
        negocio_fuera_de_interes = Negocio(
            "Cafe Bar",
            "Bar",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "bar@example.com",
            [],
            2
        )
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = [negocio_fuera_de_interes, negocio_interes]
        self.repositorio_negocio.buscar_por_contencion_de_palabra.return_value = None

        resultado = self.buscador.buscar_negocio("caFe", [filtro_cafeteria])

        self.assertEqual(resultado, [negocio_interes, negocio_fuera_de_interes])

    def test_04_buscador_no_agrega_resultado_dos_veces_si_coincide_en_palabras_y_en_minusculas(self):
        filtro_cafeteria = Mock() # No esta modelado, uso un mock para testearlo
        filtro_cafeteria.nombre.return_value = "Cafeteria"
        negocio = Negocio(
            "Cafe Tortoni",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [filtro_cafeteria],
            1
        )
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = [negocio]
        self.repositorio_negocio.buscar_por_contencion_de_palabra.return_value = [negocio]

        resultado = self.buscador.buscar_negocio("caFe", [filtro_cafeteria])

        self.assertEqual(len(resultado), 1)

    def _negocio_con_horarios(self, horarios):
        negocio = Mock()
        negocio.horarios = horarios
        negocio.filtros = []
        return negocio
 
    def test_05_filtrar_por_dias_retorna_negocio_que_abre_el_dia_buscado(self):
        negocio = self._negocio_con_horarios("LU08:00-20:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio], ["LU"])
 
        self.assertIn(negocio, resultado)
 
    def test_06_filtrar_por_dias_excluye_negocio_que_no_abre_el_dia_buscado(self):
        negocio = self._negocio_con_horarios("MA08:00-20:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio], ["LU"])
 
        self.assertNotIn(negocio, resultado)
 
    def test_07_filtrar_por_dias_acepta_multiples_dias_con_logica_OR(self):
        negocio_lu = self._negocio_con_horarios("LU08:00-20:00")
        negocio_mi = self._negocio_con_horarios("MI08:00-20:00")
        negocio_vi = self._negocio_con_horarios("VI08:00-20:00")
 
        resultado = self.buscador._filtrar_por_dias(
            [negocio_lu, negocio_mi, negocio_vi], ["LU", "MI"]
        )
 
        self.assertIn(negocio_lu, resultado)
        self.assertIn(negocio_mi, resultado)
        self.assertNotIn(negocio_vi, resultado)
 
    def test_08_filtrar_por_dias_retorna_negocio_con_varios_horarios_si_alguno_coincide(self):
        negocio = self._negocio_con_horarios("LU08:00-20:00,MI10:00-18:00,VI09:00-22:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio], ["MI"])
 
        self.assertIn(negocio, resultado)
 
    def test_09_filtrar_por_dias_no_duplica_negocio_que_aparece_en_varios_dias_buscados(self):
        negocio = self._negocio_con_horarios("LU08:00-20:00,MI10:00-18:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio], ["LU", "MI"])
 
        self.assertEqual(resultado.count(negocio), 1)
 
    def test_10_filtrar_por_dias_lista_vacia_de_dias_devuelve_todos_los_negocios(self):
        negocio_lu = self._negocio_con_horarios("LU08:00-20:00")
        negocio_ma = self._negocio_con_horarios("MA08:00-20:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio_lu, negocio_ma], [])
 
        self.assertEqual(resultado, [negocio_lu, negocio_ma])
 
    def test_11_filtrar_por_dias_excluye_negocio_sin_horarios_cargados(self):
        negocio = self._negocio_con_horarios(None)
 
        resultado = self.buscador._filtrar_por_dias([negocio], ["LU"])
 
        self.assertNotIn(negocio, resultado)
 
    def test_12_filtrar_por_dias_acepta_string_suelto_por_compatibilidad(self):
        negocio = self._negocio_con_horarios("JU08:00-20:00")
 
        resultado = self.buscador._filtrar_por_dias([negocio], "JU")
 
        self.assertIn(negocio, resultado)
 
    def test_13_buscar_negocio_aplica_filtro_de_dias_multiples_sobre_resultados(self):
        negocio_lu = self._negocio_con_horarios("LU08:00-20:00")
        negocio_vi = self._negocio_con_horarios("VI08:00-20:00")
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = [negocio_lu, negocio_vi]
        self.repositorio_negocio.buscar_por_contencion_de_palabra.return_value = None
 
        resultado = self.buscador.buscar_negocio("bar", filtros_dia=["LU", "MI"])
 
        self.assertIn(negocio_lu, resultado)
        self.assertNotIn(negocio_vi, resultado)
 
    def test_14_buscar_negocio_sin_filtro_de_dias_devuelve_todos(self):
        negocio_lu = self._negocio_con_horarios("LU08:00-20:00")
        negocio_sa = self._negocio_con_horarios("SA10:00-22:00")
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = [negocio_lu, negocio_sa]
        self.repositorio_negocio.buscar_por_contencion_de_palabra.return_value = None
 
        resultado = self.buscador.buscar_negocio("bar", filtros_dia=None)
 
        self.assertIn(negocio_lu, resultado)
        self.assertIn(negocio_sa, resultado)

    def test_15_buscar_negocio_con_filtros_personales_exitoso(self):
        filtro_cafeteria = Mock()
        filtro_cafeteria.nombre.return_value = "Cafeteria"
        negocio = Negocio(
            "Cafe Tortoni",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [filtro_cafeteria],
            1
        )
        self.repositorio_negocio.buscar_por_nombre_en_minusculas.return_value = [negocio]
        self.repositorio_negocio.buscar_por_contencion_de_palabra.return_value = None
 
        resultado = self.buscador.buscar_negocio("tortoni", filtros_usuario=[filtro_cafeteria], usar_filtros_usuario=True)
 
        self.assertIn(negocio, resultado)    

if __name__ == "__main__":
    unittest.main()