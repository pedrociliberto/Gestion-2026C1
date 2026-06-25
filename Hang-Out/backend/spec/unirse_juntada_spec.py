#Se va a testear el metodo que permite unirse a una juntada dentro del sistema

import unittest
import sys
from unittest.mock import Mock

sys.path.append("/backend")
from model.sistema import Sistema
from model.excepciones import ExcepcionSistema
from model.juntada import Juntada
from proveedores.proveedor_codigos import ProveedorCodigos

class TestUnirseAJuntada(unittest.TestCase):
        def setUp(self): #es como el de smalltalk xd
            self.repo_juntada = Mock()
            self.repo_usuario = Mock()
            self.repo_participante = Mock()
            self.repo_negocio = Mock()
            self.repo_filtro = Mock()
            self.proveedor_codigos = Mock()
            self.repo_resenia = Mock()
            self.repo_propuestas = Mock()

            self.sistema = Sistema(self.repo_juntada,self.repo_usuario,self.repo_participante, self.repo_negocio, self.repo_filtro, self.repo_resenia, self.repo_propuestas, self.proveedor_codigos)

            #Se hace un usuario y juntada que se van a utilizar en los test
            self.usuario_mock= Mock()
            self.usuario_mock.usuario = "melanie24"

            self.juntada_mock = Juntada("Cenita","MBGL",self.usuario_mock, ProveedorCodigos(), Mock())
            self.juntada_mock.id = 24 #le pongo uno random como si me lo diera la bd

        def test_01_unirse_exitosamente_devuelve_juntada(self):
            #caso en el que:
                #juntada existe
                #usuario no es participante
            self.repo_juntada.buscar_por_codigo.return_value = self.juntada_mock
            self.repo_participante.ya_es_participante.return_value = False

            juntada = self.sistema.unirse_a_juntada("MBGL",4)

            self.assertEqual(juntada.titulo(),"Cenita")
            self.assertEqual(juntada.codigo(),"MBGL")
            
        def test_02_unirse_exitosamente_llama_a_guardar(self):
             #verifica que cuando se da todo correctamente se persisten los datos llamando al metodo guardar() 
             self.repo_juntada.buscar_por_codigo.return_value = self.juntada_mock
             self.repo_participante.ya_es_participante.return_value = False

             self.sistema.unirse_a_juntada("MBGL",4)

             #Hay que verificar que efectivamente se guarde la combinacion de id_juntada y id_usuario
             self.repo_participante.guardar.assert_called_once_with(self.juntada_mock.id,4)

        def test_03_codigo_invalido_lanza_excepcion(self):
             #simula que la juntada no existe y por lo tanto el codigo devuelve None
             self.repo_juntada.buscar_por_codigo.return_value = None
             
             with self.assertRaises(ExcepcionSistema) as resultado:
                  self.sistema.unirse_a_juntada("ABCD",4)
            
             self.assertEqual(resultado.exception.mensaje,"Codigo invalido o juntada inexistente")

        def test_04_codigo_invalido_no_llama_a_guardar(self):
             #se verifica que cuando el codigo es invalido nunca se llama a guardar
             self.repo_juntada.buscar_por_codigo.return_value = None

             with self.assertRaises(ExcepcionSistema):
                  self.sistema.unirse_a_juntada("ABCD",4)
                  
             self.repo_participante.guardar.assert_not_called()

        def test_05_usuario_ya_participante_lanza_excepcion(self):
             #simula que la juntada existe y el usuario ya es participante de la misma
             self.repo_juntada.buscar_por_codigo.return_value = self.juntada_mock
             self.repo_participante.ya_es_participante.return_value = True

             with self.assertRaises(ExcepcionSistema) as resultado:
                  self.sistema.unirse_a_juntada("MBGL",4)
            
             self.assertEqual(resultado.exception.mensaje,"Ya formas parte de esta juntada")

        def test_06_usuario_ya_participante_no_llama_a_guardar(self):
             self.repo_juntada.buscar_por_codigo.return_value = self.juntada_mock
             self.repo_participante.ya_es_participante.return_value = True

             with self.assertRaises(ExcepcionSistema):
                  self.sistema.unirse_a_juntada("MBGL",4)
            
             self.repo_participante.guardar.assert_not_called()

if __name__ == "__main__":
    unittest.main()