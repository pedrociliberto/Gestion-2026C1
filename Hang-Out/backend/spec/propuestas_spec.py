import unittest
import json
from datetime import datetime, timedelta
from unittest.mock import patch, Mock
import os
import sys
from zoneinfo import ZoneInfo
from flask_jwt_extended import create_access_token

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from app import app


class PropuestasTest(unittest.TestCase):

	def setUp(self):
		app.config['TESTING'] = True
		app.config['JWT_SECRET_KEY'] = 'HangOut2026C1-GDSI-ClaveSuperSeguraYExtensaParaNuestroProyecto'
		self.client = app.test_client()
		self.ctx = app.app_context()
		self.ctx.push()

	def tearDown(self):
		self.ctx.pop()

	def _obtener_headers_autorizados(self, usuario_id):
		token = create_access_token(identity=str(usuario_id))
		return {
			"Authorization": f"Bearer {token}",
			"Content-Type": "application/json"
		}

	def _payload_base(self):
		inicio = datetime.now() + timedelta(hours=2)
		fin = inicio + timedelta(hours=2)
		return {
			"id": 1,
			"id_negocio": 10,
			"fecha_hora_inicio": inicio.isoformat(timespec='seconds'),
			"fecha_hora_fin": fin.isoformat(timespec='seconds')
		}

	@patch('propuestas.repositorio_propuestas')
	def test_01_postular_lugar_exitoso_con_lugar_personalizado(self, mock_repositorio):
		inicio = datetime.now() + timedelta(hours=2)
		fin = inicio + timedelta(hours=1)
		payload = {
			"lugar_personalizado": "Plaza San Martin",
			"fecha_hora_inicio": inicio.isoformat(timespec='seconds'),
			"fecha_hora_fin": fin.isoformat(timespec='seconds')
		}
		mock_repositorio.contar_postulaciones_usuario.return_value = 0
		mock_repositorio.existe_propuesta_identica.return_value = False
		mock_repositorio.guardar_propuesta.return_value = object()

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 201)
		called_args = mock_repositorio.guardar_propuesta.call_args[0]
		self.assertIsNone(called_args[4])
		self.assertEqual(called_args[5], 'Plaza San Martin')

	@patch('propuestas.repositorio_propuestas')
	def test_02_postular_lugar_json_invalido(self, mock_repositorio):
		response = self.client.post(
			'/postular/1/99',
			data='no es json',
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertEqual(response.get_json()['error'], 'JSON inválido')
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_03_postular_lugar_campos_obligatorios_faltantes(self, mock_repositorio):
		payload = {
			"id_negocio": 10
		}

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('Faltan campos obligatorios', response.get_json()['error'])
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_04_postular_lugar_formato_fecha_invalido(self, mock_repositorio):
		payload = {
			"id_negocio": 10,
			"fecha_hora_inicio": "19-05-2026 20:00"
		}

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertEqual(response.get_json()['error'], 'Formato de fecha inválido.')
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_05_postular_lugar_rechaza_negocio_y_personalizado_juntos(self, mock_repositorio):
		payload = self._payload_base()
		payload['lugar_personalizado'] = 'Plaza San Martin'

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('exactamente un lugar', response.get_json()['error'])
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_06_postular_lugar_rechaza_inicio_en_pasado(self, mock_repositorio):
		mock_repositorio.contar_postulaciones_usuario.return_value = 0
		mock_repositorio.existe_propuesta_identica.return_value = False
		mock_repositorio.guardar_propuesta.return_value = object()

		ahora_arg = datetime.now(ZoneInfo("America/Argentina/Buenos_Aires")).replace(tzinfo=None)
		inicio = ahora_arg - timedelta(hours=1)
		fin = ahora_arg + timedelta(hours=1)
		
		payload = {
			"id": 1,
			"id_negocio": 10,
			"fecha_hora_inicio": inicio.isoformat(timespec='seconds'),
			"fecha_hora_fin": fin.isoformat(timespec='seconds')
		}

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('no pueden ser anteriores', response.get_json()['error'])
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_07_postular_lugar_rechaza_fin_no_posterior(self, mock_repositorio):
		inicio = datetime.now() + timedelta(hours=2)
		payload = {
			"id_negocio": 10,
			"fecha_hora_inicio": inicio.isoformat(timespec='seconds'),
			"fecha_hora_fin": inicio.isoformat(timespec='seconds')
		}

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('debe ser posterior', response.get_json()['error'])
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_08_postular_lugar_rechaza_limite_3_postulaciones(self, mock_repositorio):
		payload = self._payload_base()
		mock_repositorio.contar_postulaciones_usuario.return_value = 3

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('límite máximo de 3 postulaciones', response.get_json()['error'])
		mock_repositorio.contar_postulaciones_usuario.assert_called_once_with(99, 1)
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_09_postular_lugar_rechaza_propuesta_duplicada(self, mock_repositorio):
		payload = self._payload_base()
		mock_repositorio.contar_postulaciones_usuario.return_value = 2
		mock_repositorio.existe_propuesta_identica.return_value = True

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 400)
		self.assertIn('propuesta idéntica', response.get_json()['error'])
		mock_repositorio.guardar_propuesta.assert_not_called()

	@patch('propuestas.repositorio_propuestas')
	def test_10_postular_lugar_exitoso(self, mock_repositorio):
		payload = self._payload_base()
		mock_repositorio.contar_postulaciones_usuario.return_value = 1
		mock_repositorio.existe_propuesta_identica.return_value = False
		mock_repositorio.guardar_propuesta.return_value = object()

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 201)
		self.assertIn('Propuesta almacenada con éxito', response.get_json()['message'])
		mock_repositorio.contar_postulaciones_usuario.assert_called_once_with(99, 1)
		mock_repositorio.existe_propuesta_identica.assert_called_once()

		called_args = mock_repositorio.guardar_propuesta.call_args[0]
		self.assertEqual(called_args[0], 99)
		self.assertEqual(called_args[1], 1)
		self.assertIsInstance(called_args[2], datetime)
		self.assertIsInstance(called_args[3], datetime)
		self.assertEqual(called_args[4], 10)
		self.assertIsNone(called_args[5])

	@patch('propuestas.repositorio_propuestas')
	def test_11_postular_lugar_error_interno_al_guardar(self, mock_repositorio):
		payload = self._payload_base()
		mock_repositorio.contar_postulaciones_usuario.return_value = 1
		mock_repositorio.existe_propuesta_identica.return_value = False
		mock_repositorio.guardar_propuesta.side_effect = Exception('db down')

		response = self.client.post(
			'/postular/1/99',
			data=json.dumps(payload),
			headers=self._obtener_headers_autorizados(1),
			content_type='application/json'
		)

		self.assertEqual(response.status_code, 500)
		self.assertIn('Error interno al guardar la propuesta', response.get_json()['error'])

	@patch('propuestas.repositorio_propuestas')
	def test_12_visualizar_propuestas_vacia(self, mock_repositorio):
		mock_repositorio.listar_propuestas_por_juntada.return_value = [], {}

		response = self.client.get(
			'/postular/1/99',
			headers=self._obtener_headers_autorizados(1)
		)

		self.assertEqual(response.status_code, 200)
		data = response.get_json()
		self.assertIn('propuestas', data)
		self.assertEqual(len(data['propuestas']), 0)

	@patch('propuestas.repositorio_negocio')
	@patch('propuestas.repositorio_propuestas')
	@patch('propuestas.repositorio_usuario')
	@patch('propuestas.repositorio_votacion')
	def test_13_visualizar_propuestas_con_elementos(self, mock_repositorio_votacion, mock_repositorio_usuario, mock_repositorio_propuestas, mock_repositorio_negocio):
		ahora = datetime.now()
		p1 = Mock()
		p1.id = 5
		p1.id_usuario = 2
		p1.id_negocio = 10
		p1.lugar_personalizado = None
		p1.fecha_hora_inicio = ahora + timedelta(hours=2)
		p1.fecha_hora_fin = None

		p2 = Mock()
		p2.id = 6
		p2.id_usuario = 3
		p2.id_negocio = None
		p2.lugar_personalizado = 'Parque Centenario'
		p2.fecha_hora_inicio = ahora + timedelta(days=1)
		p2.fecha_hora_fin = ahora + timedelta(days=1, hours=2)

		# Ver documentacion de listar_propuestas_por_juntada para
		# entender porque este return value
		mock_repositorio_propuestas.listar_propuestas_por_juntada.return_value = ([p1, p2], {p1.id: 0, p2.id: 0})
		mock_repositorio_votacion.existe_votacion_usuario.return_value = False

		mock_negocio = Mock()
		mock_negocio.nombre = "Bar de Moe"
		mock_repositorio_negocio.obtener_negocio.return_value = mock_negocio

		mock_usuario = Mock()
		mock_usuario.nombre_completo = "Usuario de Prueba"
		mock_repositorio_usuario.buscar_por_id.return_value = mock_usuario

		response = self.client.get(
			'/postular/1/99',
			headers=self._obtener_headers_autorizados(1)
		)

		self.assertEqual(response.status_code, 200)
		data = response.get_json()
		propuestas = data['propuestas']
		self.assertEqual(len(propuestas), 2)

		self.assertEqual(propuestas[0]['id'], 5)
		self.assertEqual(propuestas[0]['id_usuario'], 2)
		self.assertEqual(propuestas[0]['id_negocio'], 10)
		self.assertIsNone(propuestas[0]['lugar_personalizado'])
		self.assertIsInstance(propuestas[0]['fecha_hora_inicio'], str)
		self.assertIsNone(propuestas[0]['fecha_hora_fin'])

		self.assertEqual(propuestas[1]['id'], 6)
		self.assertEqual(propuestas[1]['id_usuario'], 3)
		self.assertIsNone(propuestas[1]['id_negocio'])
		self.assertEqual(propuestas[1]['lugar_personalizado'], 'Parque Centenario')
		self.assertIsInstance(propuestas[1]['fecha_hora_inicio'], str)
		self.assertIsInstance(propuestas[1]['fecha_hora_fin'], str)


if __name__ == '__main__':
	unittest.main()
