import unittest
import sys
sys.path.append("/backend")
from model.resenia import Resenia
from model.excepciones import ExcepcionSistema

class ReseniaTests(unittest.TestCase):
    def test_01_resenia_no_se_crea_si_la_valoracion_no_es_un_numero_entre_1_y_5(self):
        with self.assertRaises(ExcepcionSistema) as resultado:
            resenia = Resenia(
                1,
                1,
                1,
                7,
                "Hermoso lugar!"
            )

        self.assertEqual(resultado.exception.mensaje, "La valoración debe estar entre 1 y 5 (inclusive)")
    
    def test_02_resenia_no_se_crea_sin_valoracion_numerica(self):
        with self.assertRaises(ExcepcionSistema) as resultado:
            resenia = Resenia(
                id_usuario=1,
                id_juntada=1,
                id_negocio=1,
                valoracion=None,
                comentario="Hermoso lugar!"
            )

        self.assertEqual(resultado.exception.mensaje, "Se debe especificar una valoración numerica")

    def test_03_comentario_resenia_no_puede_tener_solo_espacios_en_blanco(self):
        with self.assertRaises(ExcepcionSistema) as resultado:
            resenia = Resenia(
                id_usuario=1,
                id_juntada=1,
                id_negocio=1,
                valoracion=5,
                comentario="   "
            )

        self.assertEqual(resultado.exception.mensaje, "El comentario no pueden ser solo espacios")
    
    def test_04_comentario_resenia_no_puede_tener_mas_de_500_caracteres(self):
        with self.assertRaises(ExcepcionSistema) as resultado:
            resenia = Resenia(
                id_usuario=1,
                id_juntada=1,
                id_negocio=1,
                valoracion=5,
                comentario="A"*501
            )

        self.assertEqual(resultado.exception.mensaje, "El largo maximo del comentario es 500 caracteres")

    def test_05_resenia_devuelve_las_imagenes_que_se_le_agregaron(self):
        resenia = Resenia(1,1,1,5,"Gran lugar!")

        resenia.agregar_imagen("ABCD")

        self.assertEqual(resenia.imagenes(), ["ABCD"])

    def test_06_resenia_borra_todas_sus_imagenes_al_recibir_borrar_imagenes(self):
        resenia = Resenia(1,1,1,5,"Gran lugar!")

        resenia.agregar_imagen("ABCD")
        resenia.agregar_imagen("ABCD")
        resenia.borrar_imagenes()

        self.assertEqual(resenia.imagenes(), [])

if __name__ == "__main__":
    unittest.main()