import unittest
import json
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from unittest.mock import Mock, patch
import os
import sys
from flask_jwt_extended import create_access_token
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from app import app


class NegocioTest(unittest.TestCase):

    def setUp(self):
        app.config['TESTING'] = True
        app.config['JWT_SECRET_KEY'] = 'HangOut2026C1-GDSI-ClaveSuperSeguraYExtensaParaNuestroProyecto'
        self.client = app.test_client()
        self.ctx = app.app_context()
        self.ctx.push()

    def tearDown(self):
        self.ctx.pop()

    # Función auxiliar para generar las cabeceras de autenticación requeridas
    def _obtener_headers_autorizados(self, usuario_id):
        """Genera el Bearer Token para un ID de usuario específico"""
        token = create_access_token(identity=str(usuario_id))
        return {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }

    # ============ TESTS PARA OBTENER_NEGOCIO ============

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_04_obtener_negocio_existente(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que obtiene correctamente un negocio existente"""
        # Setup de mocks
        negocio = Mock()
        negocio.id = 1
        negocio.nombre = "Mi Negocio"
        negocio.descripcion = "Un negocio cool"
        negocio.horarios = "Lun-Vie 9-18"
        negocio.ubicacion = "Av. Corrientes 100, CABA"
        negocio.sitio_web = "www.minegocio.com"
        negocio.url_ubicacion = "http://ubicacion.com"
        
        filtro1 = Mock()
        filtro1.id = 1
        filtro1.nombre = "Casual"
        
        filtro2 = Mock()
        filtro2.id = 2
        filtro2.nombre = "Deportes"
        
        mock_repo_negocio.obtener_negocio.return_value = negocio
        mock_repo_filtro.listar_filtros_usuario.return_value = [filtro1, filtro2]
        
        # Ejecutar
        response = self.client.get('/negocios/1', headers=self._obtener_headers_autorizados(1))
        
        # Verificar
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertTrue(data['existe'])
        self.assertEqual(data['negocio']['nombre'], "Mi Negocio")
        self.assertEqual(data['negocio']['descripcion'], "Un negocio cool")
        self.assertEqual(data['negocio']['sitio_web'], "www.minegocio.com")
        self.assertEqual(len(data['negocio']['filtros']), 2)
        mock_repo_negocio.obtener_negocio.assert_called_with(1)

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_05_obtener_negocio_no_existe(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que retorna existe: false cuando el negocio no fue cargado"""
        mock_repo_negocio.obtener_negocio.return_value = None
        mock_repo_filtro.listar_filtros_usuario.return_value = []
        
        response = self.client.get('/negocios/999', headers=self._obtener_headers_autorizados(999))
        
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertFalse(data['existe'])
        self.assertEqual(data['negocio']['id'], 999)
        self.assertEqual(data['negocio']['nombre'], "")
        self.assertEqual(data['negocio']['descripcion'], "")
        self.assertIn("no fue cargado", data['mensaje'])

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_06_obtener_negocio_con_campos_none(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que los campos None se serializan como strings vacíos"""
        negocio = Mock()
        negocio.id = 5
        negocio.nombre = None
        negocio.descripcion = "Descripción"
        negocio.horarios = None
        negocio.ubicacion = "Av. Corrientes 100, CABA"
        negocio.sitio_web = "www.ejemplo.com"
        negocio.url_ubicacion = "http://ubicacion.com"
        
        mock_repo_negocio.obtener_negocio.return_value = negocio
        mock_repo_filtro.listar_filtros_usuario.return_value = []
        
        response = self.client.get('/negocios/5', headers=self._obtener_headers_autorizados(5))
        
        data = response.get_json()
        self.assertEqual(data['negocio']['nombre'], "")
        self.assertEqual(data['negocio']['horarios'], "")
        self.assertEqual(data['negocio']['descripcion'], "Descripción")

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_07_obtener_negocio_con_filtros(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que lista correctamente los filtros del negocio"""
        negocio = Mock()
        negocio.id = 1
        negocio.nombre = "Bar"
        negocio.descripcion = ""
        negocio.horarios = ""
        negocio.ubicacion = "" 
        negocio.sitio_web = ""
        negocio.url_ubicacion = "http://ubicacion.com"
        
        filtros = []
        for i in range(1, 6):
            f = Mock()
            f.id = i
            f.nombre = f"Filtro{i}"
            filtros.append(f)
        
        mock_repo_negocio.obtener_negocio.return_value = negocio
        mock_repo_filtro.listar_filtros_usuario.return_value = filtros
        
        response = self.client.get('/negocios/1', headers=self._obtener_headers_autorizados(1))
        
        data = response.get_json()
        self.assertEqual(len(data['negocio']['filtros']), 5)
        self.assertEqual(data['negocio']['filtros'][0]['nombre'], "Filtro1")
        self.assertEqual(data['negocio']['filtros'][4]['nombre'], "Filtro5")

    # ============ TESTS PARA GUARDAR_NEGOCIO ============

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_08_guardar_negocio_exitoso(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que guarda correctamente un negocio con todos los campos"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "Bar El Desvelo",
            "descripcion": "Cervecería artesanal",
            "horarios": "Lun-Sab 18-02",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.bardesvelo.com",
            "filtros": [1, 2, 3],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("exitosamente", data['mensaje'])
        mock_repo_negocio.guardar_negocio.assert_called_with(
    1, "Bar El Desvelo", "Cervecería artesanal", "Lun-Sab 18-02", "Av. Corrientes 100, CABA", "www.bardesvelo.com", "http://ubicacion.com"
)
        mock_repo_filtro.actualizar_filtros_usuario.assert_called_with(1, [1, 2, 3])

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_09_guardar_negocio_json_invalido(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que rechaza JSON inválido"""
        response = self.client.put('/negocios/1',
            data="No es un JSON",
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 400)
        data = response.get_json()
        self.assertEqual(data['error'], "JSON inválido")

    @patch('negocios.repositorio_negocio')
    def test_10_guardar_negocio_campos_faltantes(self, mock_repo_negocio):
        """Verifica que rechaza cuando faltan campos requeridos"""
        datos_incompletos = {
            "nombre": "Bar",
            "descripcion": "Una cervecería",
            # Faltan horarios, sitio_web y filtros
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos_incompletos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 400)
        data = response.get_json()
        self.assertIn("completar todos los campos", data['error'])

    @patch('negocios.repositorio_negocio')
    def test_11_guardar_negocio_campo_vacio(self, mock_repo_negocio):
        """Verifica que rechaza cuando algún campo está vacío"""
        datos = {
            "nombre": "",  # Vacío
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 400)
        data = response.get_json()
        self.assertIn("completar todos los campos", data['error'])

    @patch('negocios.repositorio_negocio')
    def test_12_guardar_negocio_campo_solo_espacios(self, mock_repo_negocio):
        """Verifica que rechaza cuando un campo tiene solo espacios en blanco"""
        datos = {
            "nombre": "   ",  # Solo espacios
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 400)

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_13_guardar_negocio_exceso_filtros(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que rechaza más de 5 filtros"""
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1, 2, 3, 4, 5, 6],
            "url_ubicacion": "http://ubicacion.com"  # 6 filtros, más del límite
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 400)
        data = response.get_json()
        self.assertIn("Límite de 5 filtros", data['error'])
        mock_repo_negocio.guardar_negocio.assert_not_called()

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_14_guardar_negocio_exactamente_5_filtros(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que acepta exactamente 5 filtros (límite máximo)"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1, 2, 3, 4, 5],
            "url_ubicacion": "http://ubicacion.com"  # Exactamente 5
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        mock_repo_negocio.guardar_negocio.assert_called_once()

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_15_guardar_negocio_error_al_guardar_negocio(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que retorna error si falla al guardar el negocio"""
        mock_repo_negocio.guardar_negocio.return_value = False
        
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1, 2],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 500)
        data = response.get_json()
        self.assertIn("Error al guardar", data['error'])

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_16_guardar_negocio_error_al_guardar_filtros(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que retorna error si falla al guardar los filtros"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = False  # Error en filtros
        
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1, 2],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 500)
        data = response.get_json()
        self.assertIn("Error al guardar los filtros", data['error'])
        self.assertIn("guardada exitosamente", data['error'])

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_17_guardar_negocio_sin_filtros(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que acepta guardar con lista de filtros vacía"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [],
            "url_ubicacion": "http://ubicacion.com"  # Sin filtros
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        mock_repo_filtro.actualizar_filtros_usuario.assert_called_with(1, [])

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_18_guardar_negocio_campos_con_espacios_extras(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que los espacios se trimean correctamente"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "  Bar El Desvelo  ",
            "descripcion": "  Una cervecería  ",
            "horarios": "  Lun-Vie 18-02  ",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "  www.bar.com  ",
            "filtros": [1],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        response = self.client.put('/negocios/5',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(5),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        # Verificar que se pasaron los valores sin espacios
        mock_repo_negocio.guardar_negocio.assert_called_with(
    5, "Bar El Desvelo", "Una cervecería", "Lun-Vie 18-02", "Av. Corrientes 100, CABA", "www.bar.com", "http://ubicacion.com"
)

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_19_guardar_negocio_id_usuario_diferente(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que se guarda con el id_usuario correcto"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "Bar",
            "descripcion": "Descripción",
            "horarios": "Lun-Vie",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.ejemplo.com",
            "filtros": [1],
            "url_ubicacion": "http://ubicacion.com"
        }
        
        # Guardar para usuario 42
        response = self.client.put('/negocios/42',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(42),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        mock_repo_negocio.guardar_negocio.assert_called_with(
    42, "Bar", "Descripción", "Lun-Vie", "Av. Corrientes 100, CABA", "www.ejemplo.com", "http://ubicacion.com"
)

    @patch('negocios.repositorio_filtro')
    @patch('negocios.repositorio_negocio')
    def test_20_guardar_negocio_caracteres_especiales(self, mock_repo_negocio, mock_repo_filtro):
        """Verifica que maneja caracteres especiales correctamente"""
        mock_repo_negocio.guardar_negocio.return_value = True
        mock_repo_filtro.actualizar_filtros_usuario.return_value = True
        
        datos = {
            "nombre": "Bar & Restaurante 'El Desvelo'",
            "descripcion": "¡La mejor cervecería! 🍺",
            "horarios": "Lun-Vie (9:00-18:00)",
            "ubicacion": "Av. Corrientes 100, CABA",
            "sitio_web": "www.bar-desvelo.com?ref=promo",
            "filtros": [1]
        }
        
        response = self.client.put('/negocios/1',
            data=json.dumps(datos),
            headers=self._obtener_headers_autorizados(1),
            content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        mock_repo_negocio.guardar_negocio.assert_called_once()


    @patch('negocios.RepositorioPropuestas')
    @patch('negocios.JuntadaDB')
    def test_21_visualizar_apariciones_negocio_sin_propuestas(self, mock_juntada_db, mock_repo_propuestas):
        """Verifica que devuelve conteos en cero cuando no hay propuestas para el negocio."""
        instancia_repo = mock_repo_propuestas.return_value
        instancia_repo.listar_propuestas_por_negocio.return_value = []

        response = self.client.get('/negocios/apariciones/1', headers=self._obtener_headers_autorizados(1))

        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(data['cantidad_apariciones_total'], 0)
        self.assertEqual(data['cantidad_apariciones_ganadoras'], 0)
        self.assertEqual(data['cantidad_apariciones_no_ganadoras'], 0)
        self.assertEqual(data['cantidad_apariciones_en_votacion'], 0)
        self.assertEqual(data['apariciones'], [])
        self.assertEqual(data['propuestas_ganadoras']['cantidad_total'], 0)
        self.assertEqual(data['propuestas_ganadoras']['cantidad_pasadas'], 0)
        self.assertEqual(data['propuestas_ganadoras']['cantidad_futuras'], 0)

    @patch('negocios.RepositorioPropuestas')
    @patch('negocios.JuntadaDB')
    def test_22_visualizar_apariciones_negocio_con_varios_estados(self, mock_juntada_db, mock_repo_propuestas):
        """Verifica que clasifica correctamente propuestas ganadoras, no ganadoras y en votación."""
        ahora_ba = datetime.now(ZoneInfo("America/Argentina/Buenos_Aires")).replace(tzinfo=None)
        pasada = ahora_ba - timedelta(days=2)
        futura = ahora_ba + timedelta(days=3)

        propuesta_votacion = Mock()
        propuesta_votacion.id = 1
        propuesta_votacion.id_juntada = 10
        propuesta_votacion.fecha_hora_inicio = ahora_ba + timedelta(days=1)
        propuesta_votacion.fecha_hora_fin = None
        propuesta_votacion.es_ganadora = False

        propuesta_no_ganadora = Mock()
        propuesta_no_ganadora.id = 2
        propuesta_no_ganadora.id_juntada = 11
        propuesta_no_ganadora.fecha_hora_inicio = ahora_ba + timedelta(days=2)
        propuesta_no_ganadora.fecha_hora_fin = None
        propuesta_no_ganadora.es_ganadora = False

        propuesta_ganadora_pasada = Mock()
        propuesta_ganadora_pasada.id = 3
        propuesta_ganadora_pasada.id_juntada = 12
        propuesta_ganadora_pasada.fecha_hora_inicio = pasada
        propuesta_ganadora_pasada.fecha_hora_fin = None
        propuesta_ganadora_pasada.es_ganadora = True

        propuesta_ganadora_futura = Mock()
        propuesta_ganadora_futura.id = 4
        propuesta_ganadora_futura.id_juntada = 13
        propuesta_ganadora_futura.fecha_hora_inicio = futura
        propuesta_ganadora_futura.fecha_hora_fin = None
        propuesta_ganadora_futura.es_ganadora = True

        instancia_repo = mock_repo_propuestas.return_value
        instancia_repo.listar_propuestas_por_negocio.return_value = [
            propuesta_votacion,
            propuesta_no_ganadora,
            propuesta_ganadora_pasada,
            propuesta_ganadora_futura,
        ]

        juntada_pendiente = Mock()
        juntada_pendiente.estado = 'PENDIENTE'
        juntada_confirmada = Mock()
        juntada_confirmada.estado = 'CONFIRMADA'

        def get_juntada(id_juntada):
            if id_juntada == 10:
                return juntada_pendiente
            if id_juntada == 11:
                return juntada_confirmada
            return None

        mock_juntada_db.query.get.side_effect = get_juntada

        response = self.client.get('/negocios/apariciones/1', headers=self._obtener_headers_autorizados(1))

        self.assertEqual(response.status_code, 200)
        data = response.get_json()

        self.assertEqual(data['cantidad_apariciones_total'], 4)
        self.assertEqual(data['cantidad_apariciones_en_votacion'], 1)
        self.assertEqual(data['cantidad_apariciones_no_ganadoras'], 1)
        self.assertEqual(data['cantidad_apariciones_ganadoras'], 2)
        self.assertEqual(len(data['apariciones']), 4)
        self.assertEqual(data['propuestas_ganadoras']['cantidad_total'], 2)
        self.assertEqual(data['propuestas_ganadoras']['cantidad_pasadas'], 1)
        self.assertEqual(data['propuestas_ganadoras']['cantidad_futuras'], 1)
        self.assertEqual(len(data['propuestas_ganadoras']['pasadas']), 1)
        self.assertEqual(len(data['propuestas_ganadoras']['futuras']), 1)
        self.assertTrue(any(item['id_propuesta'] == 1 and item['en_votacion'] for item in data['apariciones']))
        self.assertTrue(any(item['id_propuesta'] == 2 and not item['en_votacion'] for item in data['apariciones']))

    def test_23_visualizar_apariciones_token_invalido(self):
        response = self.client.get('/negocios/apariciones/1', headers={"Authorization": "Bearer invalid_token"})
        self.assertEqual(response.status_code, 422)

    def test_24_visualizar_apariciones_usuario_no_autorizado(self):
        response = self.client.get('/negocios/apariciones/2', headers=self._obtener_headers_autorizados(1))
        self.assertEqual(response.status_code, 403)


if __name__ == "__main__":
    unittest.main()