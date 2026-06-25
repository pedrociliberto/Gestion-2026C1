import sys
import unittest
sys.path.append("/backend")

from model.resenia import Resenia
from model.imagen import Imagen

class TestImagen(unittest.TestCase):
    def test_01_imagen_conoce_resenia_para_la_que_fue_creada(self):
        resenia = Resenia(1,1,1,5,"Gran lugar!")
        imagen = Imagen(resenia, "ABCD", "imagen.jpg")

        self.assertEqual(imagen.resenia, resenia)

    def test_02_imagen_conoce_su_contenido(self):
        resenia = Resenia(1,1,1,5,"Gran lugar!")
        imagen = Imagen(resenia, "ABCD", "imagen.jpg")

        self.assertEqual(imagen.contenido, "ABCD")

    def test_03_imagen_conoce_su_nombre(self):
        resenia = Resenia(1,1,1,5,"Gran lugar!")
        imagen = Imagen(resenia, "ABCD", "imagen.jpg")

        self.assertEqual(imagen.nombre, "imagen.jpg")

if __name__ == "__main__":
    unittest.main()