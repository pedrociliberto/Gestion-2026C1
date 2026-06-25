import unittest
from unittest.mock import Mock
import sys

sys.path.append("/backend")
from model.juntada import Juntada
from model.excepciones import ExcepcionSistema
from proveedores.proveedor_codigos import ProveedorCodigos

class TestJuntada(unittest.TestCase):
    def setUp(self):
        self.usuario = Mock()
        self.proveedor_codigos = Mock()
        self.repositorio_juntadas = Mock()
        self.juntada = Juntada("Cenita", "ABCD", self.usuario, self.proveedor_codigos, self.repositorio_juntadas)

    def test_01_juntada_conoce_nombre_y_codigo(self):
        juntada = Juntada("Cenita", "ABCD", self.usuario, self.proveedor_codigos, self.repositorio_juntadas)
        
        self.assertEqual(juntada.titulo(), "Cenita")
        self.assertEqual(juntada.codigo(), "ABCD")
    
    def test_02_juntada_conoce_su_organizador(self):
        self.usuario.nombre.return_value = "Juan Perez"
        
        juntada = Juntada("Cenita", "ABCD", self.usuario, self.proveedor_codigos, self.repositorio_juntadas)
        
        self.assertEqual(juntada.titulo(), "Cenita")
        self.assertEqual(juntada.codigo(), "ABCD")
        self.assertEqual(juntada.organizador.nombre(), self.usuario.nombre())
    
    def test_03_juntada_crea_codigo_de_4_si_no_se_especifica(self):
        self.usuario.nombre.return_value = "Juan Perez"
        self.proveedor_codigos.generar_codigo.return_value = "ABCD"
        self.repositorio_juntadas.buscar_por_codigo.return_value = None
        
        juntada = Juntada("Cenita", None, self.usuario, self.proveedor_codigos, self.repositorio_juntadas)
        
        self.assertEqual(len(juntada.codigo()), 4)
    
    def test_04_creacion_juntada_falla_al_crearse_sin_titulo(self):
        self.usuario.nombre.return_value = "Juan Perez"
        
        with self.assertRaises(ExcepcionSistema) as res_excepcion:
            Juntada(None, "ABCD", self.usuario, self.proveedor_codigos, self.repositorio_juntadas)
        
        self.assertEqual(res_excepcion.exception.mensaje, "No se especifico titulo")

    def test_05_creacion_juntada_falla_si_codigo_no_es_de_4(self):
        self.usuario.nombre.return_value = "Juan Perez"
        
        with self.assertRaises(ExcepcionSistema) as res_excepcion:
            Juntada("Juntada", "ABCDE", self.usuario, self.proveedor_codigos, self.repositorio_juntadas)
        
        self.assertEqual(res_excepcion.exception.mensaje, "El codigo debe tener longitud cuatro")

    def test_06_juntada_no_genera_codigo_ya_usado(self):
        # side_effects retorna el siguiente elemento de la lista
        # cada vez que se ejecuta ese metodo (similar a return_value)
        self.proveedor_codigos.generar_codigo.side_effect = ["ABCD", "EFGH"]
        self.repositorio_juntadas.buscar_por_codigo.side_effect = [self.juntada, None]

        juntada = Juntada("Titulo", None, self.usuario, self.proveedor_codigos, self.repositorio_juntadas)

        self.assertNotEqual(juntada.codigo(), "ABCD")

if __name__ == "__main__":
    unittest.main()