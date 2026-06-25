from faker import Faker
import random
from typing import List, Dict

fake = Faker('es_AR')

# Configuración de generación de datos
NUM_USUARIOS = 5
NUM_JUNTADAS = 5
NUM_NEGOCIOS = 10
NUM_FILTROS = 14 
NUM_PROPUESTAS = 3

# Tipos de juntadas
TIPOS_JUNTADAS = [
    "Salida al cine",
    "Cena entre amigos",
    "Paseo por la ciudad",
    "Excursión al parque",
    "Tarde de juegos de mesa",
    "Visita a un museo",
    "Picnic en el parque",
    "Noche de películas",
    "Viaje de fin de semana",
    "Reunión de trabajo",
    "Concierto en vivo",
    "Torneo de deportes",
    "Almuerzo de negocios",
]

# Tipos de negocios
TIPOS_NEGOCIOS = [
    "Pizzería",
    "Café",
    "Restaurante",
    "Bar",
    "Heladería",
    "Cine",
    "Sala de juegos",
]

# Nombres de barrios/ubicaciones comunes
BARRIOS = [
    "Centro", "San Isidro", "La Boca", "San Telmo", "Retiro",
    "Recoleta", "Almagro", "San Cristóbal", "Caballito", "Flores",
    "Floresta", "Belgrano", "Núñez", "Saavedra", "Villa Ortúzar",
    "Parque Chacabuco", "Villa Riachuelo", "Parque Patricios"
]

# Ubicacion de los barrios en Maps
UBICACION_BARRIOS = {
    "Centro": "https://www.google.com/maps/search/?api=1&query=Centro,+Buenos+Aires",
    "San Isidro": "https://www.google.com/maps/search/?api=1&query=San+Isidro,+Buenos+Aires",
    "La Boca": "https://www.google.com/maps/search/?api=1&query=La+Boca,+Buenos+Aires",
    "San Telmo": "https://www.google.com/maps/search/?api=1&query=San+Telmo,+Buenos+Aires",
    "Retiro": "https://www.google.com/maps/search/?api=1&query=Retiro,+Buenos+Aires",
    "Recoleta": "https://www.google.com/maps/search/?api=1&query=Recoleta,+Buenos+Aires",
    "Almagro": "https://www.google.com/maps/search/?api=1&query=Almagro,+Buenos+Aires",
    "San Cristóbal": "https://www.google.com/maps/search/?api=1&query=San+Crist%C3%B3bal,+Buenos+Aires",
    "Caballito": "https://www.google.com/maps/search/?api=1&query=Caballito,+Buenos+Aires",
    "Flores": "https://www.google.com/maps/search/?api=1&query=Flores,+Buenos+Aires",
    "Floresta": "https://www.google.com/maps/search/?api=1&query=Floresta,+Buenos+Aires",
    "Belgrano": "https://www.google.com/maps/search/?api=1&query=Belgrano,+Buenos+Aires",
    "Núñez": "https://www.google.com/maps/search/?api=1&query=N%C3%BA%C3%B1ez,+Buenos+Aires",
    "Saavedra": "https://www.google.com/maps/search/?api=1&query=Saavedra,+Buenos+Aires",
    "Villa Ortúzar": "https://www.google.com/maps/search/?api=1&query=Villa+Ort%C3%BAzar,+Buenos+Aires",
    "Parque Chacabuco": "https://www.google.com/maps/search/?api=1&query=Parque+Chacabuco,+Buenos+Aires",
    "Villa Riachuelo": "https://www.google.com/maps/search/?api=1&query=Villa+Riachuelo,+Buenos+Aires",
    "Parque Patricios": "https://www.google.com/maps/search/?api=1&query=Parque+Patricios,+Buenos+Aires"
} 

# Servicios por tipo de negocio para crear descripciones coherentes
SERVICIOS_POR_TIPO = {
    "Pizzería": ["pizza artesanal", "delivery", "para llevar", "opciones vegetarianas", "masa artesanal"],
    "Café": ["cafés especiales", "postres caseros", "desayunos", "wifi gratis", "opciones veganas"],
    "Restaurante": ["menú del día", "cocina regional", "reservas", "carta de vinos", "opciones para grupos"],
    "Bar": ["tragos clásicos", "cervezas artesanales", "ambiente musical", "happy hour", "picadas"],
    "Heladería": ["sabores artesanales", "cucuruchos", "opciones sin lactosa", "promociones familiares"],
    "Cine": ["salas modernas", "estrenos", "butacas cómodas", "sonido envolvente"],
    "Sala de juegos": ["juegos de mesa", "arcade", "áreas de descanso", "promociones especiales"]
}

def servicios_por_tipo(tipo: str, cuantos_min: int = 2, cuantos_max: int = 4) -> List[str]:
    opciones = SERVICIOS_POR_TIPO.get(tipo, ["servicio atento", "buen ambiente", "precios razonables"])
    cuantos = random.randint(cuantos_min, min(cuantos_max, len(opciones)))
    return random.sample(opciones, cuantos)

def generar_descripcion_negocio(tipo: str, barrio: str, nombre: str) -> str:
    servicios = servicios_por_tipo(tipo)
    servicios_texto = ", ".join(servicios[:-1]) + (" y " + servicios[-1] if len(servicios) > 1 else servicios[0])
    descripcion = f"{nombre} es un {tipo.lower()} ubicado en {barrio}. Ofrece {servicios_texto}."
    return descripcion


def generar_usuarios(cantidad: int, es_personal: bool) -> List[Dict]:
    """Genera usuarios aleatorios usando Faker"""
    usuarios = []
    apellidos_usados = set()
    
    for i in range(cantidad):
        # Generar nombre único
        nombre = fake.first_name()
        apellido = fake.last_name()
        # Asegurar que no se repita la combinación
        contador = 0
        while f"{nombre}_{apellido}" in apellidos_usados and contador < 10:
            apellido = fake.last_name()
            contador += 1
        
        apellidos_usados.add(f"{nombre}_{apellido}")

        # Manejo de nombres compuestos.
        if " " in nombre:
            nombre_usuario = "".join(nombre.split())
        else: 
            nombre_usuario = nombre
        
        usuario = {
            "nombre_completo": f"{nombre} {apellido}",
            "usuario": f"{nombre_usuario.lower()}{random.randint(100, 999)}",
            "email": f"{nombre_usuario.lower()}{apellido.lower()}@gmail.com",
            "password": "Password123!",
            "password_confirm": "Password123!",
            "es_cuenta_personal": es_personal,         
        }
        
        usuarios.append(usuario)
    
    return usuarios

def generar_juntadas(cantidad: int) -> List[Dict]:
    """Genera juntadas aleatorias"""
    juntadas = []
    
    for _ in range(cantidad):
        juntada = {
            "titulo": random.choice(TIPOS_JUNTADAS),
        }
        juntadas.append(juntada)
    
    return juntadas

def generar_invitados(num_juntadas: int, num_usuarios: int, porcentaje_participacion: float = 0.3) -> List[Dict]:
    """Genera relaciones de invitados a juntadas"""
    invitados = []
    id_invitado = 1
    
    for id_juntada in range(1, num_juntadas + 1):
        # Cantidad aleatoria de invitados por juntada (entre 1 y 15)
        num_invitados_juntada = random.randint(1, min(15, num_usuarios - 1))
        
        # Seleccionar usuarios aleatorios (excluyendo el organizador)
        usuarios_disponibles = [u for u in range(1, num_usuarios + 1)]
        
        # Obtener el organizador de la juntada (asumiendo que está en juntadas)
        usuarios_invitados = random.sample(usuarios_disponibles, min(num_invitados_juntada, len(usuarios_disponibles)))
        
        for id_usuario in usuarios_invitados:
            invitado = {
                "id_juntada": id_juntada,
                "id_usuario": id_usuario
            }
            invitados.append(invitado)
            id_invitado += 1
    
    return invitados

def generar_horarios_aleatorios() -> str:
    """Genera horarios aleatorios para negocios"""
    dias = ["LU", "MA", "MI", "JU", "VI", "SA", "DO"]
    hora_inicio = random.randint(6, 18)
    hora_cierre = random.randint(hora_inicio + 2, 23)
    
    horarios = []
    for dia in dias:
        horario = f"{dia}{hora_inicio:02d}:00-{hora_cierre:02d}:00"
        horarios.append(horario)
    
    return ",".join(horarios)

def generar_negocios(cantidad: int) -> List[Dict]:
    """Genera negocios aleatorios"""
    negocios = []
    
    # Los IDs de negocios comienzan en 4
    id_base = 4
    
    for i in range(cantidad):
        tipo = random.choice(TIPOS_NEGOCIOS)
        # Usar un apellido o palabra corta para dar variedad en el nombre
        nombre_negocio = f"{tipo} {fake.last_name()}"
        
        # Seleccionar filtros aleatorios (entre 2 y 5, que es el límite del backend)
        num_filtros = random.randint(2, min(5, NUM_FILTROS))
        filtros = random.sample(range(1, NUM_FILTROS + 1), num_filtros)
        
        barrio = random.choice(BARRIOS)
        descripcion = generar_descripcion_negocio(tipo, barrio, nombre_negocio)
        negocio = {
            "id": id_base + i,
            "nombre": nombre_negocio,
            "descripcion": descripcion,
            "horarios": generar_horarios_aleatorios(),
            "ubicacion": f"{fake.street_address()}, {barrio}, Buenos Aires",
            "sitio_web": fake.url(),
            "filtros": sorted(filtros),
            "url_ubicacion": UBICACION_BARRIOS[barrio]
        }
        negocios.append(negocio)
    
    return negocios


# Generar datos
DATOS_USUARIOS = generar_usuarios(NUM_USUARIOS, es_personal=True)
DATOS_USUARIOS_EMPRESARIALES = generar_usuarios(NUM_NEGOCIOS, es_personal=False)
DATOS_USUARIOS.extend(DATOS_USUARIOS_EMPRESARIALES)
DATOS_JUNTADAS = generar_juntadas(NUM_JUNTADAS)
DATOS_INVITADOS = generar_invitados(NUM_JUNTADAS, NUM_USUARIOS)
DATOS_NEGOCIOS = generar_negocios(NUM_NEGOCIOS)