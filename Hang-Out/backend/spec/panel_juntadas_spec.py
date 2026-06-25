import unittest
import sys
from unittest.mock import Mock

sys.path.append("/backend")
from model.sistema import Sistema
from proveedores.proveedor_codigos import ProveedorCodigos

class TestSistema(unittest.TestCase):
    def setUp(self):
        self.repo_juntada = Mock()
        self.repo_usuario = Mock()
        self.repo_participante = Mock()
        self.repo_negocio = Mock()
        self.repositorio_filtro = Mock()
        self.repo_propuestas = Mock()
        self.repo_resenia = Mock()
        self.proveedor_codigos = ProveedorCodigos()
        self.sistema = Sistema(self.repo_juntada, self.repo_usuario, self.repo_participante, self.repo_negocio, self.repositorio_filtro, self.repo_resenia, self.repo_propuestas, self.proveedor_codigos)

    def test_01_listar_juntadas_vacias(self):
        """Si el usuario no tiene nada, debe devolver una lista vacía"""
        self.repo_juntada.buscar_por_usuario.return_value = []
        
        resultado = self.sistema.listar_juntadas_usuario(1, self.repo_propuestas)
        
        self.assertEqual(resultado, [])
        self.repo_juntada.buscar_por_usuario.assert_called_with(1, self.repo_propuestas)

    def test_02_listar_juntadas_como_creador(self):
        """Verifica que las juntadas creadas tengan el rol 'Creador'"""
        juntada_mock = {
            "id": 10,
            "titulo": "Asado",
            "codigo": "A1B2",
            "rol": "Creador",
        }
        
        self.repo_juntada.buscar_por_usuario.return_value = [juntada_mock]
        
        resultado = self.sistema.listar_juntadas_usuario(1, self.repo_propuestas)
        
        self.assertEqual(len(resultado), 1)
        self.assertEqual(resultado[0]["rol"], "Creador")
        self.assertEqual(resultado[0]["titulo"], "Asado")

    def test_03_listar_juntadas_como_invitado(self):
        """Verifica que las juntadas donde es invitado tengan el rol 'Invitado'"""
        juntada_invitado = {
            "id": 20,
            "titulo": "Fútbol",
            "codigo": "F7F7",
            "rol": "Invitado",
        }

        self.repo_juntada.buscar_por_usuario.return_value = [juntada_invitado]
        
        resultado = self.sistema.listar_juntadas_usuario(1, self.repo_propuestas)
        
        self.assertEqual(len(resultado), 1)
        self.assertEqual(resultado[0]["rol"], "Invitado")
        self.assertEqual(resultado[0]["id"], 20)

    def test_04_combinacion_de_roles(self):
        """Verifica que el panel muestre ambos tipos de juntadas al mismo tiempo"""
        j_creada = {
            "id": 1,
            "titulo": "Cena",
            "codigo": "C111",
            "rol": "Creador"
        }
        j_invitado = {
            "id": 2,
            "titulo": "Cine",
            "codigo": "C222",
            "rol": "Invitado"
        }
        
        self.repo_juntada.buscar_por_usuario.return_value = [j_creada, j_invitado]  
        
        resultado = self.sistema.listar_juntadas_usuario(1, self.repo_propuestas)
        
        self.assertEqual(len(resultado), 2)
        roles = [r["rol"] for r in resultado]
        self.assertIn("Creador", roles)
        self.assertIn("Invitado", roles)

if __name__ == "__main__":
    unittest.main()