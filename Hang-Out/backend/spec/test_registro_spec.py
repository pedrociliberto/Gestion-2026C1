import json
import unittest
from unittest.mock import Mock, patch
from pathlib import Path

import sys
sys.path.append("/backend")

from app import app


class RegistroTest(unittest.TestCase):

    def setUp(self):
        app.config['TESTING'] = True
        self.client = app.test_client()

        self.ctx = app.app_context()
        self.ctx.push()

        self.payload_base = {
            "nombre_completo": "Maria Florencia Landrini",
            "usuario": "maria_dev",
            "email": "maria@hangout.com",
            "password": "Secret123!",
            "password_confirm": "Secret123!",
            "es_cuenta_personal": True
        }

    def tearDown(self):
        self.ctx.pop()

    def _configurar_queries(self, mock_query, email_exists=False, usuario_exists=False):
        query_por_email = Mock()
        query_por_email.first.return_value = object() if email_exists else None

        query_por_usuario = Mock()
        query_por_usuario.first.return_value = object() if usuario_exists else None

        mock_query.filter_by.side_effect = [query_por_email, query_por_usuario]

    @patch('models.db.session.add')
    @patch('models.db.session.commit')
    @patch('models.UsuarioDB.query')
    def test01_registro_exitoso(self, mock_query, mock_commit, mock_add):
        self._configurar_queries(mock_query)

        response = self.client.post(
            '/registro',
            data=json.dumps(self.payload_base),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 201)
        self.assertEqual("Usuario creado correctamente.", response.get_json()["message"])
        mock_add.assert_called_once()
        mock_commit.assert_called_once()

    @patch('models.UsuarioDB.query')
    def test02_registro_fallido_campos_vacios(self, mock_query):
        payload = self.payload_base.copy()
        payload["email"] = ""

        response = self.client.post(
            '/registro',
            data=json.dumps(payload),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual(
            "Debe completar todos los campos para poder registrarse",
            response.get_json()["error"]
        )
        mock_query.filter_by.assert_not_called()

    @patch('models.UsuarioDB.query')
    def test03_registro_fallido_email_invalido(self, mock_query):
        payload = self.payload_base.copy()
        payload["email"] = "maria-at-hangout.com"

        response = self.client.post(
            '/registro',
            data=json.dumps(payload),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual("Correo electrónico inválido.", response.get_json()["error"])
        mock_query.filter_by.assert_not_called()

    @patch('models.UsuarioDB.query')
    def test04_registro_fallido_email_ya_registrado(self, mock_query):
        self._configurar_queries(mock_query, email_exists=True)

        response = self.client.post(
            '/registro',
            data=json.dumps(self.payload_base),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual("El correo electrónico ya está registrado.", response.get_json()["error"])

    @patch('models.UsuarioDB.query')
    def test05_registro_fallido_usuario_ya_en_uso(self, mock_query):
        self._configurar_queries(mock_query, usuario_exists=True)

        response = self.client.post(
            '/registro',
            data=json.dumps(self.payload_base),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual("El nombre de usuario ya está en uso.", response.get_json()["error"])

    @patch('models.UsuarioDB.query')
    def test06_registro_fallido_confirmacion_password(self, mock_query):
        payload = self.payload_base.copy()
        payload["password_confirm"] = "Secret1234!"
        self._configurar_queries(mock_query)

        response = self.client.post(
            '/registro',
            data=json.dumps(payload),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual("La confirmación de la contraseña no coincide.", response.get_json()["error"])

    @patch('models.UsuarioDB.query')
    def test07_registro_fallido_password_debil(self, mock_query):
        payload = self.payload_base.copy()
        payload["password"] = "Secret12"
        payload["password_confirm"] = "Secret12"
        self._configurar_queries(mock_query)

        response = self.client.post(
            '/registro',
            data=json.dumps(payload),
            content_type='application/json'
        )

        self.assertEqual(response.status_code, 400)
        self.assertEqual("La contraseña debe tener al menos un carácter especial.", response.get_json()["error"])


if __name__ == "__main__":
    unittest.main()