import unittest
from unittest.mock import Mock, patch
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from app import app

class FiltroTest(unittest.TestCase):

    def setUp(self):
        app.config['TESTING'] = True
        self.client = app.test_client()
        self.ctx = app.app_context()
        self.ctx.push()

    def tearDown(self):
        self.ctx.pop()

    # ============ TESTS PARA LISTAR_FILTROS_DISPONIBLES ============

    @patch('filtros.repositorio_filtro')
    def test_01_listar_filtros_disponibles_exitoso(self, mock_repo_filtro):
        """Verifica que listar filtros retorna correctamente los filtros disponibles"""
        # Setup de mocks
        filtro1 = Mock()
        filtro1.id = 1
        filtro1.nombre = "Casual"
        
        filtro2 = Mock()
        filtro2.id = 2
        filtro2.nombre = "Deportes"
        
        mock_repo_filtro.listar_filtros.return_value = [filtro1, filtro2]
        
        # Ejecutar
        response = self.client.get('/filtros/listar')
        
        # Verificar
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(len(data['filtros']), 2)
        self.assertEqual(data['filtros'][0]['nombre'], "Casual")
        self.assertEqual(data['filtros'][1]['nombre'], "Deportes")
        mock_repo_filtro.listar_filtros.assert_called_once()

    @patch('filtros.repositorio_filtro')
    def test_02_listar_filtros_lista_vacia(self, mock_repo_filtro):
        """Verifica que retorna lista vacía cuando no hay filtros"""
        mock_repo_filtro.listar_filtros.return_value = []
        
        response = self.client.get('/filtros/listar')
        
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(len(data['filtros']), 0)

    @patch('filtros.repositorio_filtro')
    def test_03_listar_filtros_muchos_filtros(self, mock_repo_filtro):
        """Verifica que maneja correctamente una lista grande de filtros"""
        filtros = []
        nombres = ["Casual", "Deportes", "Cine", "Música", "Gastronómico", "Viajes"]
        for i, nombre in enumerate(nombres, 1):
            f = Mock()
            f.id = i
            f.nombre = nombre
            filtros.append(f)
        
        mock_repo_filtro.listar_filtros.return_value = filtros
        
        response = self.client.get('/filtros/listar')
        
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(len(data['filtros']), 6)

if __name__ == "__main__":
    unittest.main()