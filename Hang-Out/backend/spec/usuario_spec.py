import unittest

import sys
sys.path.append("/backend")
from model.usuario import Usuario

class UsuarioTest(unittest.TestCase):
    def test01_usuario_conoce_su_nombre(self):
        usuario = Usuario("Juan")
        self.assertEqual(usuario.nombre, "Juan")

if __name__ == "__main__":
    unittest.main()