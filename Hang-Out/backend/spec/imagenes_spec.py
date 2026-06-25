import os
import sys
import unittest
from unittest.mock import patch

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from model.excepciones import ExcepcionSistema
from model.imagenes import (
    MAX_IMAGENES_POR_NEGOCIO,
    agregar_imagen_negocio,
    eliminar_imagen_negocio,
    obtener_imagen_por_id,
    obtener_imagenes_negocio,
)


class ImagenesModelTest(unittest.TestCase):
    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_01_agregar_imagen_exitoso(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.obtener_imagenes_negocio.return_value = []
        mock_repo_imagenes.guardar_imagen_negocio.return_value = True

        agregar_imagen_negocio(1, "foto_1.jpg")

        mock_repo_imagenes.guardar_imagen_negocio.assert_called_once_with(1, "foto_1.jpg")

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_02_agregar_imagen_negocio_inexistente(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = None

        with self.assertRaises(ExcepcionSistema) as error:
            agregar_imagen_negocio(999, "foto_1.jpg")

        self.assertEqual(error.exception.mensaje, "El negocio indicado no existe")
        mock_repo_imagenes.guardar_imagen_negocio.assert_not_called()

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_03_agregar_imagen_rechaza_por_limite(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.obtener_imagenes_negocio.return_value = [
            {"id": i, "url_imagen": f"foto_{i}.jpg"} for i in range(MAX_IMAGENES_POR_NEGOCIO)
        ]

        with self.assertRaises(ExcepcionSistema) as error:
            agregar_imagen_negocio(1, "nueva_foto.jpg")

        self.assertEqual(
            error.exception.mensaje,
            f"No se pueden agregar más de {MAX_IMAGENES_POR_NEGOCIO} imágenes a un negocio",
        )
        mock_repo_imagenes.guardar_imagen_negocio.assert_not_called()

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_04_agregar_imagen_falla_en_repositorio(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.obtener_imagenes_negocio.return_value = []
        mock_repo_imagenes.guardar_imagen_negocio.return_value = False

        with self.assertRaises(ExcepcionSistema) as error:
            agregar_imagen_negocio(1, "foto_1.jpg")

        self.assertEqual(error.exception.mensaje, "Error al guardar la imagen del negocio")

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_05_obtener_imagenes_exitoso(self, mock_repo_negocio, mock_repo_imagenes):
        imagenes = [
            {"id": 10, "url_imagen": "foto_10.jpg"},
            {"id": 11, "url_imagen": "foto_11.jpg"},
        ]
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.obtener_imagenes_negocio.return_value = imagenes

        resultado = obtener_imagenes_negocio(7)

        self.assertEqual(resultado, imagenes)
        mock_repo_imagenes.obtener_imagenes_negocio.assert_called_once_with(7)

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_06_obtener_imagenes_negocio_inexistente(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = None

        with self.assertRaises(ExcepcionSistema) as error:
            obtener_imagenes_negocio(404)

        self.assertEqual(error.exception.mensaje, "El negocio indicado no existe")
        mock_repo_imagenes.obtener_imagenes_negocio.assert_not_called()

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_07_obtener_imagen_por_id_exitoso(self, mock_repo_negocio, mock_repo_imagenes):
        imagen = {"id": 2, "url_imagen": "foto_2.jpg"}
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.obtener_imagen_por_id.return_value = imagen

        resultado = obtener_imagen_por_id(1, 2)

        self.assertEqual(resultado, imagen)
        mock_repo_imagenes.obtener_imagen_por_id.assert_called_once_with(1, 2)

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_08_eliminar_imagen_exitoso(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.eliminar_imagen_negocio.return_value = True

        eliminar_imagen_negocio(1, 3)

        mock_repo_imagenes.eliminar_imagen_negocio.assert_called_once_with(1, 3)

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_09_eliminar_imagen_negocio_inexistente(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = None

        with self.assertRaises(ExcepcionSistema) as error:
            eliminar_imagen_negocio(999, 1)

        self.assertEqual(error.exception.mensaje, "El negocio indicado no existe")
        mock_repo_imagenes.eliminar_imagen_negocio.assert_not_called()

    @patch("model.imagenes.repositorio_imagenes")
    @patch("model.imagenes.repositorio_negocio")
    def test_10_eliminar_imagen_falla_en_repositorio(self, mock_repo_negocio, mock_repo_imagenes):
        mock_repo_negocio.obtener_negocio.return_value = object()
        mock_repo_imagenes.eliminar_imagen_negocio.return_value = False

        with self.assertRaises(ExcepcionSistema) as error:
            eliminar_imagen_negocio(1, 3)

        self.assertEqual(error.exception.mensaje, "Error al eliminar la imagen del negocio")


if __name__ == "__main__":
    unittest.main()