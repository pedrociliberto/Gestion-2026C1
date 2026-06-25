import unittest
import json
from unittest.mock import patch
from werkzeug.security import generate_password_hash
from flask_jwt_extended import decode_token

import sys
sys.path.append("/backend")
from app import app
from models import UsuarioDB

class LoginTest(unittest.TestCase):

    def setUp(self):
        app.config['TESTING'] = True
        app.config['JWT_SECRET_KEY'] = 'HangOut2026C1-GDSI-ClaveSuperSeguraYExtensaParaNuestroProyecto'
        self.client = app.test_client()

        self.ctx = app.app_context()
        self.ctx.push()

        self.db_mock = user_mock = UsuarioDB(
            id=1,
            usuario="maria_dev",
            email="maria@hangout.com",
            password_hash=generate_password_hash("Secret123!"),
            nombre_completo="Maria Florencia Landrini",
            es_cuenta_personal="True"
        )

        self.datos = {
            "id": 1,
            "nombre": "Maria Florencia Landrini",
            "usuario": "maria_dev",
            "email": "maria@hangout.com",
            "es_cuenta_personal": "True"
        }

    def tearDown(self):
        self.ctx.pop()

    @patch('models.UsuarioDB.query')
    def test01_login_exitoso_con_usuario(self, mock_query): # mock_query simula ser la db
        mock_query.filter.return_value.first.return_value = self.db_mock

        response = self.client.post('/login', 
            data=json.dumps({"usuario": "maria_dev", "password": "Secret123!"}),
            content_type='application/json')

        self.assertEqual(response.status_code, 200)

        json_data = response.get_json()
        self.assertEqual("Inicio de sesión exitoso", json_data["message"])
        self.assertEqual(self.datos, json_data["data"])
        self.assertIn("token", json_data)

        payload_token = decode_token(json_data["token"])
        self.assertEqual(payload_token["sub"], "1")

    @patch('models.UsuarioDB.query')
    def test02_login_exitoso_con_email(self, mock_query):
        
        mock_query.filter.return_value.first.return_value = self.db_mock

        response = self.client.post('/login', 
            data=json.dumps({"usuario": "maria@hangout.com", "password": "Secret123!"}),
            content_type='application/json')

        self.assertEqual(response.status_code, 200)
        
        json_data = response.get_json()
        self.assertEqual("Inicio de sesión exitoso", json_data["message"])
        self.assertEqual(self.datos, json_data["data"])
        self.assertIn("token", json_data)

        payload_token = decode_token(json_data["token"])
        self.assertEqual(payload_token["sub"], "1")

    @patch('models.UsuarioDB.query')
    def test03_login_fallido_usuario_inexistente(self, mock_query):
        mock_query.filter.return_value.first.return_value = None

        response = self.client.post('/login', 
            data=json.dumps({"usuario": "inexistente", "password": "AnyPassword1"}),
            content_type='application/json')

        self.assertEqual(response.status_code, 401)

        json_data = response.get_json()
        self.assertEqual("Las credenciales no corresponden a un usuario registrado", json_data["error"])
        self.assertNotIn("token", json_data)

    @patch('models.UsuarioDB.query')
    def test04_login_fallido_password_incorrecta(self, mock_query):
        # Setup del Mock: usuario existe pero mandaremos password mal
        mock_query.filter.return_value.first.return_value = self.db_mock

        response = self.client.post('/login', 
            data=json.dumps({"usuario": "maria_dev", "password": "WrongPassword"}),
            content_type='application/json')

        self.assertEqual(response.status_code, 401)
        
        json_data = response.get_json()
        self.assertEqual("Contraseña incorrecta", json_data["error"])
        self.assertNotIn("token", json_data)

    @patch('models.UsuarioDB.query')
    def test05_login_fallido_campos_vacios(self, mock_query):
        mock_query.filter.return_value.first.return_value = self.db_mock

        response = self.client.post('/login', 
            data=json.dumps({"usuario": "", "password": ""}),
            content_type='application/json')

        self.assertEqual(response.status_code, 400)
        
        json_data = response.get_json()
        self.assertEqual("Debe completar el usuario/email y la contraseña", json_data["error"])
        self.assertNotIn("token", json_data)

if __name__ == "__main__":
    unittest.main()