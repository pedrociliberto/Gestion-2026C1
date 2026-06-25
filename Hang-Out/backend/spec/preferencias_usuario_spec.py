import unittest
from unittest.mock import Mock, patch
import sys

sys.path.append("/backend")
from app import app

class TestPreferenciasUsuario(unittest.TestCase):
    def setUp(self):
        self.usuario = Mock()
        self.id_usuario = 123
        self.repositorio_filtros = Mock()
        self.client = app.test_client()

        filtro1 = Mock()
        filtro1.id = 1
        filtro1.nombre = "Vegano"

        filtro2 = Mock()
        filtro2.id = 2
        filtro2.nombre = "Familiar"

        self.filtro1 = filtro1
        self.filtro2 = filtro2

    def test_01_el_usuario_comienza_sin_preferencias(self):
        self.repositorio_filtros.listar_filtros_usuario.return_value = []
        
        filtros_usuario = self.repositorio_filtros.listar_filtros_usuario(self.id_usuario)
        
        self.assertEqual(filtros_usuario, [])

    @patch('filtros.repositorio_filtro')
    def test_02_el_usuario_agrega_una_preferencia(self, mock_repo_filtro):
        """Verifica que se agregue un filtro exitosamente"""
      
        mock_repo_filtro.listar_filtros_usuario.return_value = [self.filtro1]

        payload = {"filtros": [self.filtro1.id]}
        
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True

        response = self.client.put(f'/filtros/agregar/{self.id_usuario}', json=payload)

        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(data['mensaje'], "Filtros actualizados al usuario exitosamente")
        
       
        mock_repo_filtro.actualizar_filtros_usuario.assert_called_once_with(self.id_usuario, [self.filtro1.id])

        response= self.client.get(f'/filtros/listar/{self.id_usuario}')
        filtros_usuario = response.get_json()
        self.assertEqual(len(filtros_usuario['filtros']), 1)
        self.assertEqual(filtros_usuario['filtros'][0]['nombre'], "Vegano")

    @patch('filtros.repositorio_filtro')
    def test_03_el_usuario_puede_agregar_mas_de_una_preferencia(self, mock_repo_filtro):
        """Verifica que se agregue un filtro exitosamente"""
        mock_repo_filtro.listar_filtros_usuario.return_value = [self.filtro1, self.filtro2]

        payload = {"filtros": [self.filtro1.id, self.filtro2.id]}
        
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True

        response = self.client.put(f'/filtros/agregar/{self.id_usuario}', json=payload)

        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(data['mensaje'], "Filtros actualizados al usuario exitosamente")
        
       
        mock_repo_filtro.actualizar_filtros_usuario.assert_called_once_with(self.id_usuario, [self.filtro1.id, self.filtro2.id])

        response= self.client.get(f'/filtros/listar/{self.id_usuario}')
        filtros_usuario = response.get_json()
        self.assertEqual(len(filtros_usuario['filtros'] ), 2)
        self.assertEqual(filtros_usuario['filtros'][0]['nombre'], "Vegano")
        self.assertEqual(filtros_usuario['filtros'][1]['nombre'], "Familiar")

    @patch('filtros.repositorio_filtro')
    def test_04_el_usuario_puede_eliminar_una_preferencia(self, mock_repo_filtro):
        """Verifica que se elimine un filtro exitosamente"""
        mock_repo_filtro.listar_filtros_usuario.return_value = [self.filtro1]
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True

        payload = {"filtros": [self.filtro1.id]}  

        response = self.client.put(f'/filtros/agregar/{self.id_usuario}', json=payload)

        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(data['mensaje'], "Filtros actualizados al usuario exitosamente")

        mock_repo_filtro.actualizar_filtros_usuario.assert_called_once_with(self.id_usuario, [self.filtro1.id])
        
        response= self.client.get(f'/filtros/listar/{self.id_usuario}')
        filtros_usuario = response.get_json()
        self.assertEqual(len(filtros_usuario['filtros']), 1)
        self.assertEqual(filtros_usuario['filtros'][0]['nombre'], "Vegano")
    
 
if __name__ == "__main__":
    unittest.main()