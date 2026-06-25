import unittest
import sys
from unittest.mock import Mock
from unittest.mock import patch
from unittest.mock import call

sys.path.append("/backend")
from model.sistema import Sistema
from model.juntada import Juntada
from model.usuario import Usuario
from model.negocio import Negocio
from model.excepciones import ExcepcionSistema
from model.resenia import Resenia
from model.imagen import Imagen
from proveedores.proveedor_codigos import ProveedorCodigos

class TestSistema(unittest.TestCase):
    def setUp(self):
        self.repositorio_juntadas = Mock()
        self.repositorio_usuarios = Mock()
        self.repositorio_participantes = Mock()
        self.repositorio_negocios = Mock()
        self.repositorio_filtros = Mock()
        self.proveedor_codigos = Mock()
        self.repo_resenia = Mock()
        self.repo_propuestas = Mock()
        self.sistema = Sistema(self.repositorio_juntadas, self.repositorio_usuarios, self.repositorio_participantes, self.repositorio_negocios, self.repositorio_filtros, self.repo_resenia, self.repo_propuestas, self.proveedor_codigos)

    def test_01_sistema_devuelve_juntada_creada(self):
        self.repositorio_juntadas.buscar_por_codigo.return_value = None
        usuario_mock = Mock()
        self.repositorio_usuarios.buscar_por_id.return_value = usuario_mock

        titulo = "Cenita"
        codigo = "ABCD"
        id_organizador = 1 
        juntada = self.sistema.crear_juntada(titulo, codigo, id_organizador)

        self.repositorio_juntadas.guardar.assert_called_once()
        self.assertEqual(juntada.titulo(), titulo)
        self.assertEqual(juntada.codigo(), codigo)

    def test_02_sistema_devuelve_juntada_con_organizador_correcto(self):
        self.repositorio_juntadas.buscar_por_codigo.return_value = None
        usuario_mock = Mock()
        self.repositorio_usuarios.buscar_por_id.return_value = usuario_mock
        
        usuario_mock.usuario = "juancito2005"
        id_organizador = 1
        juntada = self.sistema.crear_juntada("Cenita", "ABCD", id_organizador)
        
        self.repositorio_juntadas.guardar.assert_called_once()
        self.assertEqual(juntada.organizador.usuario, "juancito2005")
        self.repositorio_usuarios.buscar_por_id.assert_called_with(id_organizador)
    
    def test_03_sistema_no_crea_juntada_con_codigo_generado_ya_usado(self):
        codigo_a_generar = "ABCD"
        # side_effects retorna el siguiente elemento de la lista
        # cada vez que se ejecuta ese metodo (similar a return_value)
        self.proveedor_codigos.generar_codigo.side_effect = [codigo_a_generar, "EFGH"]
        self.repositorio_juntadas.buscar_por_codigo.side_effect = [
            Juntada("Juntada", codigo_a_generar, Usuario("Juan"), ProveedorCodigos(), Mock()), 
            None
        ]

        juntada = self.sistema.crear_juntada("Cenita", None, 1)

        self.assertNotEqual(juntada.codigo(), codigo_a_generar)

    def test_04_sistema_devuelve_resultado_vacio_si_no_encuentra_coincidencias(self):
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        resultado = self.sistema.buscar_negocio("Cafe martinez", 1)

        self.assertEqual(resultado, [])
    
    def test_05_sistema_devuelve_resultado_con_nombre_exacto_si_la_encuentra(self):
        negocio = Negocio(
            "Cafe martinez",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [],
            1
        )
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = [negocio]
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        resultado = self.sistema.buscar_negocio("Cafe martinez", 1)

        self.assertEqual(resultado, [negocio])

    def test_06_sistema_devuelve_resultado_si_hay_coincidencia_en_minusculas(self):
        negocio = Negocio(
            "Cafe martinez",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [],
            1
        )
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.side_effect = lambda x: [negocio] if x == "cafe martinez" else None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        resultado = self.sistema.buscar_negocio("CAFE MaRtinEz", 1)

        self.assertEqual(resultado, [negocio])
    
    def test_07_sistema_devuelve_resultado_si_hay_coincidencia_sin_acentos(self):
        negocio = Negocio(
            "Cafe martinez",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [],
            1
        )
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.side_effect = lambda x: [negocio] if x == "cafe martinez" else None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        resultado = self.sistema.buscar_negocio("Café Martínez", 1)

        self.assertEqual(resultado, [negocio])
    
    def test_08_sistema_devuelve_resultado_si_coincide_alguna_palabra(self):
        negocio = Negocio(
            "Cafe",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [],
            1
        )
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.side_effect = lambda x: [negocio] if x == "cafe" else None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        resultado = self.sistema.buscar_negocio("Café Martínez", 1)

        self.assertEqual(resultado, [negocio])
    
    def test_09_sistema_no_realiza_busqueda_si_el_usuario_no_existe(self):
        self.repositorio_usuarios.buscar_por_id.return_value = None
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.buscar_negocio("Cafe", 1)

        self.assertEqual(resultado.exception.mensaje, "El usuario no existe")
    

    def test_10_sistema_no_realiza_busqueda_si_no_hay_id_usuario(self):
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = None
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None
        self.repositorio_filtros.listar_filtros_usuario.return_value = []

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.buscar_negocio("cafe", None)

        self.assertEqual(resultado.exception.mensaje, "El usuario no existe")

    def test_11_sistema_muestra_antes_resultados_con_filtros_en_comun(self):
        filtro_cafeteria = Mock() # No esta modelado, uso un mock para testearlo
        filtro_bar = Mock() # No esta modelado, uso un mock para testearlo
        filtro_cafeteria.nombre.return_value = "Cafeteria"
        filtro_bar.nombre.return_value = "Bar"
        negocio_interes = Negocio(
            "Cafe",
            "Cafe",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "cafe@example.com",
            [filtro_cafeteria],
            1
        )
        negocio_fuera_de_interes = Negocio(
            "Bar",
            "Bar",
            "08:00 - 10:00",
            "Av. de Mayo 825, CABA",
            "bar@example.com",
            [filtro_bar],
            2
        )
        self.repositorio_usuarios.buscar_por_id.return_value = Mock()
        self.repositorio_filtros.listar_filtros_usuario.return_value = [filtro_cafeteria]
        self.repositorio_negocios.buscar_por_nombre_en_minusculas.return_value = [negocio_fuera_de_interes, negocio_interes]
        self.repositorio_negocios.buscar_por_contencion_de_palabra.return_value = None

        resultado = self.sistema.buscar_negocio("CafE MaRTInEz", 1)

        self.assertEqual(resultado, [negocio_interes, negocio_fuera_de_interes])

    def test_12_sistema_permite_que_el_organizador_salga_reasignando_rol(self):
        organizador = Mock(id=1)
        juntada = Juntada("Cenita", "ABCD", organizador, Mock(), Mock(), id=7, estado="CONFIRMADA", id_propuesta_ganadora=10)

        self.repositorio_juntadas.buscar_por_id.return_value = juntada
        self.repositorio_participantes.ya_es_participante.return_value = True
        self.repositorio_participantes.obtener_ids_participantes.return_value = [2, 3]
        self.repositorio_usuarios.buscar_por_id.return_value = Mock(id=2)

        with patch("model.sistema.repositorio_propuestas") as repo_propuestas, patch("model.sistema.repositorio_votacion") as repo_votacion:
            repo_propuestas.listar_propuestas_por_juntada.return_value = ([], {})

            self.sistema.salir_de_juntada(7, 1)

            repo_votacion.borrar_votacion_usuario.assert_not_called()

        self.repositorio_juntadas.actualizar_organizador.assert_called_once_with(7, 2)
        self.repositorio_participantes.eliminar.assert_called_once_with(7, 2)
        self.assertNotIn(call(7, 1), self.repositorio_participantes.eliminar.call_args_list)

    def test_13_sistema_reabre_y_persiste_pendiente_si_sale_el_ganador(self):
        organizador = Mock(id=1)
        juntada = Juntada("Cenita", "ABCD", organizador, Mock(), Mock(), id=7, estado="CONFIRMADA", id_propuesta_ganadora=20)
        propuesta_ganadora = Mock(id=20, id_usuario=2)
        propuesta_otra = Mock(id=10, id_usuario=3)

        self.repositorio_juntadas.buscar_por_id.return_value = juntada
        self.repositorio_participantes.ya_es_participante.return_value = True

        with patch("model.sistema.repositorio_propuestas") as repo_propuestas, patch("model.sistema.repositorio_votacion") as repo_votacion:
            repo_propuestas.listar_propuestas_por_juntada.return_value = ([propuesta_otra, propuesta_ganadora], {10: 1, 20: 3})

            self.sistema.salir_de_juntada(7, 2)

            repo_votacion.borrar_votacion_usuario.assert_any_call(2, 10)
            repo_votacion.borrar_votacion_usuario.assert_any_call(2, 20)
            repo_propuestas.eliminar_propuesta.assert_called_once_with(20)

        self.repositorio_juntadas.actualizar_estado_votacion.assert_called_once_with(7, "PENDIENTE", None)
        self.repositorio_participantes.eliminar.assert_called_once_with(7, 2)
    
    def test_14_sistema_crea_resenia_y_la_persiste(self):
        id_negocio = 1
        propuesta = Mock(id_negocio=id_negocio)
        self.repo_propuestas.buscar_por_id.return_value = propuesta
        self.repositorio_juntadas.buscar_por_id.return_value = Juntada(
            "CENA",
            "ABCD",
            Mock(),
            Mock(),
            self.repositorio_juntadas,
            1,
            "PASADA",
            123
        )
        self.repo_resenia.buscar_resenia.return_value = None
        self.sistema.crear_resenia(1,1,id_negocio, 5, "Lindo lugar!")

        self.repo_resenia.guardar_resenia.assert_called_once()

    def test_15_sistema_no_crea_resenia_si_ya_existe_otra(self):
        propuesta = Mock(id_negocio=1)
        self.repo_propuestas.buscar_por_id.return_value = propuesta
        self.repositorio_juntadas.buscar_por_id.return_value = Juntada(
            "CENA",
            "ABCD",
            Mock(),
            Mock(),
            self.repositorio_juntadas,
            1,
            "PASADA",
            123
        )
        self.repo_resenia.buscar_resenia.return_value = Resenia(
            id_usuario=1,
            id_juntada=1,
            id_negocio=1,
            valoracion=5,
            comentario="Lindo lugar"
        )

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.crear_resenia(1,1,1,5, "Horrible lugar!")

        self.assertEqual(resultado.exception.mensaje, "Ya hiciste una reseña de ese lugar para esta juntada")

    def test_16_sistema_no_crea_resenia_si_el_negocio_no_fue_el_ganador(self):
        self.repo_resenia.buscar_resenia.return_value = None
        self.repositorio_juntadas.buscar_por_id.return_value = Juntada(
            "CENA",
            "ABCD",
            Mock(),
            Mock(),
            self.repositorio_juntadas,
            1,
            "PASADA",
            123
        )
        id_negocio = 1
        propuesta = Mock()
        propuesta.id_negocio.return_value = id_negocio + 1 # Que haya ganado otro negocio
        self.repo_propuestas.buscar_por_id.return_value = propuesta

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.crear_resenia(1,1,id_negocio,5, "Horrible lugar!")
        
        self.assertEqual(resultado.exception.mensaje, "No se puede reseñar un lugar no visitado")

    def test_17_sistema_no_crea_resenia_si_juntada_no_esta_cerrada(self):
        id_negocio = 1
        propuesta = Mock(id_negocio=id_negocio)
        self.repo_propuestas.buscar_por_id.return_value = propuesta
        self.repo_resenia.buscar_resenia.return_value = None
        self.repositorio_juntadas.buscar_por_id.return_value = Juntada(
            "CENA",
            "ABCD",
            Mock(),
            Mock(),
            self.repositorio_juntadas,
            1,
            "CONFIRMADA",
            123
        )

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.crear_resenia(1,1,id_negocio,5, "Horrible lugar!")
        
        self.assertEqual(resultado.exception.mensaje, "La juntada debe ser pasada y estar cerrada")
    
    def test_18_sistema_guarda_imagen_asociada_a_resenia(self):
        self.sistema.agregar_imagen_a_resenia(1, 1, 1, "IMAGEN", "imagen.jpg")
        self.repo_resenia.guardar_resenia.assert_called_once()

    def test_19_sistema_guarda_imagen_con_contenido_especificado(self):
        self.repo_resenia.guardar_resenia.return_value = True
        imagen = self.sistema.agregar_imagen_a_resenia(1, 1, 1, "IMAGEN", "imagen.jpg")
        
        self.assertEqual(imagen.contenido, "IMAGEN")

    def test_20_sistema_guarda_imagen_con_nombre_especificado(self):
        self.repo_resenia.guardar_resenia.return_value = True
        imagen = self.sistema.agregar_imagen_a_resenia(1, 1, 1, "IMAGEN", "imagen.jpg")
        
        self.assertEqual(imagen.nombre, "imagen.jpg")

    def test_21_sistema_guarda_imagen_con_nombre_especificado(self):
        self.repo_resenia.guardar_resenia.return_value = True
        imagen = self.sistema.agregar_imagen_a_resenia(1, 1, 1, "IMAGEN", "imagen.jpg")
        
        self.assertEqual(imagen.nombre, "imagen.jpg")

    def test_22_sistema_no_guarda_imagen_en_resenia_si_se_guardaron_demasiadas(self):
        resenia = Resenia(1,1,1,5,"Gran lugar")
        for i in range(5):
            resenia.agregar_imagen(Imagen(resenia, b"ABCD", f"{i}.jpg"))
        self.repo_resenia.buscar_resenia.return_value = resenia
        self.repo_resenia.guardar_resenia.return_value = True
        
        
        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.agregar_imagen_a_resenia(1, 1, 1, b"IMAGEN", "imagen.jpg")
        
        self.assertEqual(resultado.exception.mensaje, "No se pueden cargar mas de 5 imagenes a la reseña")

    def test_23_sistema_no_elimina_juntada_si_no_es_el_organizador(self):
        organizador = Mock(id=1)
        juntada = Juntada("Cenita", "ABCD", organizador, Mock(), Mock(), id=7, estado="PENDIENTE")

        self.repositorio_juntadas.buscar_por_id.return_value = juntada

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.eliminar_juntada(7, 2)

        self.assertEqual(resultado.exception.mensaje, "Solo el organizador puede eliminar la juntada")
        self.repo_propuestas.eliminar_propuestas_por_juntada.assert_not_called()
        self.repositorio_participantes.eliminar_por_juntada.assert_not_called()
        self.repositorio_juntadas.eliminar.assert_not_called()

    def test_24_sistema_no_elimina_juntada_pasada(self):
        organizador = Mock(id=1)
        juntada = Juntada("Cenita", "ABCD", organizador, Mock(), Mock(), id=7, estado="PASADA")

        self.repositorio_juntadas.buscar_por_id.return_value = juntada

        with self.assertRaises(ExcepcionSistema) as resultado:
            self.sistema.eliminar_juntada(7, 1)

        self.assertEqual(resultado.exception.mensaje, "Solo se puede eliminar una juntada pendiente o confirmada")
        self.repo_propuestas.eliminar_propuestas_por_juntada.assert_not_called()
        self.repositorio_participantes.eliminar_por_juntada.assert_not_called()
        self.repositorio_juntadas.eliminar.assert_not_called()
    
def assert_coincide_contenido_imagen_resenia(resenia, contenido_imagen):
    return resenia.imagenes[0].contenido == contenido_imagen


if __name__ == "__main__":
    unittest.main()