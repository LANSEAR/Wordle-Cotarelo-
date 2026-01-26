package com.cotarelo.wordle.client.data

/**
 * Repositorio centralizado de palabras para el juego Wordle
 * TODAS LAS PALABRAS EN MAYÚSCULAS SIN TILDES
 *
 * Este repositorio se usa tanto para modo offline como para validación
 * local antes de enviar al servidor en modos online.
 */
object WordRepository {

    /**
     * Obtiene todas las palabras disponibles para una longitud específica
     */
    fun getWords(length: Int, difficulty: String = "all"): List<String> {
        val common = getCommonWords(length)
        val rare = getRareWords(length)

        return when (difficulty.lowercase()) {
            "easy", "common" -> common
            "hard", "rare" -> rare
            else -> common + rare
        }
    }

    /**
     * Verifica si una palabra es válida
     */
    fun isValidWord(word: String): Boolean {
        val normalized = word.trim().uppercase()
        val length = normalized.length
        return normalized in getWords(length)
    }

    /**
     * Obtiene una palabra aleatoria
     */
    fun getRandomWord(length: Int, difficulty: String = "normal"): String {
        val words = when (difficulty.lowercase()) {
            "easy" -> getCommonWords(length)
            "hard" -> getRareWords(length)
            "normal" -> {
                val common = getCommonWords(length)
                val rare = getRareWords(length)
                if (Math.random() < 0.7) common else rare
            }
            else -> getWords(length)
        }
        return words.randomOrNull() ?: error("No hay palabras para longitud $length")
    }

    /**
     * Palabras comunes por longitud
     */
    private fun getCommonWords(length: Int): List<String> {
        return when (length) {
            4 -> listOf(
                // Palabras muy comunes
                "CASA", "MESA", "GATO", "AMOR", "VIDA", "AGUA", "SOPA", "LUNA",
                "PATO", "PELO", "PINO", "CAMA", "SOFA", "TREN", "AUTO", "MANO",
                "DEDO", "BOCA", "CARA", "OJOS", "PIES", "ALMA", "BESO", "DATO",
                "FOCO", "HOYO", "IDEA", "LODO", "MOTO", "NOTA", "OLOR", "PALA",
                "RAMA", "ROPA", "SALA", "TAPA", "VASO", "ZONA", "BOTE", "CAJA",
                "PASO", "PESO", "PISO", "ROSA", "SACO", "SEDA", "TEMA", "TORO",
                "LAVA", "NUBE", "CUBO", "MAPA", "RATA", "TUBO", "VELA",
                "CERO", "HUMO", "LAGO", "RAYO",
                // Sustantivos adicionales
                "ARCO", "ASNO", "AULA", "BAÑO", "BOLA", "CAPA",
                "CAÑA", "CINE", "COCO", "COLA", "COPA", "CUNA", "CUÑA", "DAMA",
                "DADO", "DOTE", "FAMA", "FILA", "FOTO", "GAFA", "GOMA", "HADA",
                "HIJO", "HOJA", "ISLA", "JOYA", "KILO", "LADO", "LANA",
                "LEON", "LIMA", "LINO", "LOBO", "LOTE", "LUJO", "LUTO", "MAIZ",
                "MALO", "MASA", "META", "MIEL", "MINA", "MODO", "MOÑO",
                "NIÑO", "NIÑA", "NIDO", "ONDA", "OBRA", "ODIO", "PAJA",
                "PALO", "PAPA", "PARE", "PAVO", "PERA", "PICO",
                "PILA", "PIPA", "PLAN", "POLO", "POMO", "POZA", "POZO",
                "PUMA", "PUNO", "RAMO", "RANA", "RAZO", "REMO", "RETO", "RIFA",
                "RIMA", "RITO", "SAPO", "SENO", "SETA",
                "SILO", "SINO", "SOLO", "TACO", "TAJO", "TELA", "TIPO",
                "TIRO", "TIZA", "TODO", "TOMO", "TONO", "TOPO", "TUFO",
                "URNA", "VANO", "VETA", "VIÑA",
                "VISA", "VOTO", "YEMA", "YESO", "YUGO", "ZETA", "ZINC", "ZUMO"
            )

            5 -> listOf(
                // Palabras muy comunes
                "PLAYA", "CAMPO", "MUNDO", "TIERRA", "FUEGO", "NOCHE", "TARDE", "LIBRO",
                "PAPEL", "VERDE", "ROJO", "AZUL", "NEGRO", "ROSA", "ALTO", "BAJO",
                "MESA", "SILLA", "PUERTA", "PARED", "FLOR", "PLANTA", "FRUTA", "PERA",
                "COCHE", "MOTO", "TREN", "BARCO", "CIELO", "COLOR", "FONDO", "FORMA",
                "GENTE", "GRUPO", "HECHO", "LUGAR", "MEDIO", "NIVEL", "ORDEN",
                "PARTE", "PUNTO", "RADIO", "SIGLO", "SUELO", "TIEMPO", "VISTA", "VOCAL",
                "NIEVE", "CARNE", "BRAZO", "DEDO", "PIANO", "FERIA", "JUEGO", "LLAMA",
                // Sustantivos comunes
                "ARENA", "BANCO", "BARRO", "BOLSA", "BOMBA", "CABLE", "CALLE", "CALOR",
                "CARTA", "CENA", "CERRO", "CHICO", "CLASE", "COSTA", "CREMA", "CUERO",
                "CUERPO", "CURSO", "DANZA", "DISCO", "DOLOR", "DROGA", "DUCHA", "FECHA",
                "FIESTA", "FIRMA", "FRENO", "FUENTE", "GASTO", "GOLPE", "GRASA", "GRUTA",
                "GUERRA", "GUSTO", "HABLA", "HIELO", "HORNO", "HOTEL", "HUESO", "HUMOR",
                "ISLA", "JABON", "JOVEN", "JUGO", "LABOR", "LECHE", "LETRA", "LINEA",
                "LISTA", "LLAVE", "LLUVIA", "LUCHA", "MADRE", "MARCA", "MARZO", "METAL",
                "METRO", "MIEL", "MONTE", "MURO", "MUSEO", "NOVIO", "OBRA", "PADRE",
                "PAGO", "PAIS", "PALMA", "PARQUE", "PASO", "PATIO", "PELEA",
                "PIEL", "PIEZA", "PLAZA", "POEMA", "POLLO", "PRECIO", "PREMIO", "PRISA",
                "PUEBLO", "PUENTE", "PULSO", "QUESO", "RAIZ", "RATON", "REINO", "RELOJ",
                "RESTO", "RISA", "RITMO", "ROCA", "RUEDA", "RUIDO", "SALSA",
                "SALTO", "SANGRE", "SELVA", "SEÑAL", "SITIO",
                "SOMBRA", "SONIDO", "SUEÑO", "TABLA", "TALLA", "TECHO", "TELA", "TEMA",
                "TIGRE", "TINTA", "TITULO", "TORRE", "TORTA", "TRAJE", "TRAMO",
                "TRATO", "TRIBU", "TRONO", "TROZO", "UNION", "VALOR", "VASO", "VENTA",
                "VERSO", "VIAJE", "VIENTO", "VINO", "VIRUS", "YATE", "ZONA",
                // Verbos comunes (infinitivos)
                "AMAR", "ANDAR", "ABRIR", "BEBER", "COMER", "CREER", "DEBER", "DECIR",
                "ENTRAR", "HACER", "HABLAR", "LEER", "MIRAR", "PASAR",
                "PEDIR", "PONER", "PODER", "SABER", "SALIR", "TENER", "TOMAR", "TRAER",
                "VENIR", "VIVIR", "VOLAR", "VOLVER", "CREAR", "BAJAR", "SUBIR", "SACAR",
                "BUSCAR", "LLEVAR", "PERDER", "GANAR", "CANTAR", "BAILAR", "TOCAR", "SOÑAR",
                // Adjetivos
                "BUENO", "MALO", "GRANDE", "NUEVO", "VIEJO", "LARGO", "CORTO",
                "ANCHO", "FINO", "DURO", "BLANDO", "CLARO", "OSCURO", "LIMPIO", "SUCIO",
                "RICO", "POBRE", "FELIZ", "TRISTE", "LENTO", "RAPIDO", "FUERTE", "DEBIL",
                "CALIENTE", "FRIO", "DULCE", "AMARGO", "LLENO", "VACIO", "CERCA", "LEJOS"
            )

            6 -> listOf(
                "COMIDA", "BEBIDA", "COCINA", "SALON", "CUARTO", "JARDIN", "PARQUE",
                "ESCUELA", "COLEGIO", "IGLESIA", "TEMPLO", "MUSEO", "TEATRO", "ESTADIO",
                "PLAZA", "CALLE", "CAMINO", "BOSQUE", "VOLCAN", "DESIERTO",
                "OCEANO", "ISLA", "COSTA", "ARENA", "ROCA",
                "PIEDRA", "METAL", "HIERRO", "COBRE", "PLATA", "VIDRIO", "MADERA",
                "CIUDAD", "PUEBLO", "CENTRO", "BARRIO", "MERCADO", "TIENDA", "BANCO",
                "PUERTA", "CAMISA", "ZAPATO", "ESPEJO", "BALCON", "FIGURA",
                "CUADRO", "TIEMPO", "SONIDO", "MUSICA", "LETRAS", "NOVELA",
                "CUENTO", "PAGINA", "ABRIGO", "AMIGOS", "VECINO", "VEREDA",
                "VIAJES", "BOLETO", "MALETA", "PASAJE", "AGENDA", "CORREO",
                "POSTAL", "CAMION", "TRENES", "BARCOS", "VUELOS", "MUELLE",
                "ANIMAL", "ARROYO", "BALADA", "BODEGA", "BRONCE", "CABINA",
                "CADENA", "CALIDO", "CARCEL", "CARIÑO", "COLINA", "CORONA",
                "CUERDA", "DORMIR", "DRAGON", "EDITAR", "ENFADO", "ESTUFA",
                "EXAMEN", "FLORES", "FUSION", "GANADO", "GENERO", "GRANDE",
                "HERIDO", "IDIOMA", "IMAGEN", "INFLAR", "ISLOTE", "LIQUEN",
                "LLUVIA", "MANTRA", "MEZCLA", "MONEDA", "MORADA", "NACION",
                "OBJETO", "ORIGEN"
            )

            7 -> listOf(
                "VENTANA", "PALABRA", "MENSAJE", "CULTURA", "MUSICA", "PINTURA",
                "TRABAJO", "ESTUDIO", "CIENCIA", "FISICA", "QUIMICA", "BIOLOGIA",
                "MEDICINA", "HOSPITAL", "CLINICA", "FARMACIA", "DOCTOR", "PACIENTE",
                "GOBIERNO", "ECONOMIA", "FINANZAS", "EMPRESA", "NEGOCIO", "MERCADO", "COMERCIO",
                "HISTORIA", "BATALLA", "VICTORIA", "DERROTA", "EJERCITO", "SOLDADO",
                "CAMINAR", "ESCALON", "COCINAR", "ARMARIO", "ZAPATOS", "ESPEJOS",
                "LIBRERO", "PAPELES", "LAPICES", "ESCRITO", "LECTURA", "NOVELAS",
                "CUENTOS", "REVISTA", "PERIODO", "PORTADA", "PAGINAS", "ARCHIVO",
                "CARPETA", "AGENDAS", "PAQUETE", "ALMACEN", "TIENDAS", "COMPRAS",
                "OFERTAS", "FACTURA", "RECIBOS", "AMISTAD", "MONTANA", "REFLEJO",
                "SONRISA", "CAMELLO", "SENDERO", "AVIONES", "PLANETA", "GALERIA",
                "ARTISTA", "ESCUELA", "PROFETA", "RELATAR", "REFORMA", "COSTERO",
                "VIAJERO", "FACHADA", "LECTORA", "IMPULSO", "CEREBRO", "EMOCION",
                "LIBRETA", "SILUETA", "CAMPEON", "RELIEVE", "MANZANA", "CRISTAL",
                "TORNADO", "VOLUMEN", "ESPACIO", "REFUGIO", "PAISAJE", "DOMINIO",
                "PORTERO", "COLORES", "CARRERA", "BOTELLA", "MASCARA", "BANDERA",
                "REUNION", "VENTAJA", "DESTINO", "CALZADO", "CABALLO", "PESCADO",
                "BALANCE", "EDICION", "FABRICA"
            )

            else -> emptyList()
        }
    }

    /**
     * Palabras raras/difíciles por longitud
     */
    private fun getRareWords(length: Int): List<String> {
        return when (length) {
            4 -> listOf(
                "YATE", "YOGA", "YODO", "YUGO", "YUCA", "ZETA", "ZINC", "ZONA",
                "ZUMO", "APTO", "ARCO", "AUGE", "AULA", "AXIS", "BUEY", "CAYO",
                "EDIL", "FARO", "HALO", "LIMO", "MICA", "NETO", "ODIO", "PUMA",
                "RETO", "RUBO", "SACO", "TACO", "TUFO", "VADO"
            )

            5 -> listOf(
                "AMBAR", "ATOMO", "EBANO", "ETER", "OPALO", "OVULO", "ICONO", "EPICA",
                "FAENA", "GOLPE", "HURTO", "INDIO", "KARMA", "LAMPA", "MANGO",
                "NEXO", "NYLON", "OMEGA", "PIQUE", "RUEDA", "SAQUE"
            )

            6 -> listOf(
                "ABSIDE", "ACACIA", "ACENTO", "ACIDO", "ACORDE", "AFABLE", "AFECTO", "AGENTE",
                "AGUILA", "ALBUM", "ALCOBA", "ALEGRE", "ALERTA", "ALGEBRA", "ALIADO",
                "AMABLE", "ANCIANO", "ANILLO", "ANIMAL", "ALTURA", "ASTUTO"
            )

            7 -> listOf(
                "ABEJON", "ABNEGAR", "ABORDAR", "ABRAZAR", "ABSOLUTO", "ABSORBER", "ABSTENER", "ABUNDAR",
                "ACABADO", "ACADEMIA", "ACAMPAR", "ACCEDER", "ACELERAR", "ACERTIJO", "ACLAMAR",
                "ACONSEJAR", "ACOMODAR", "ACUARIO", "ADELANTE", "ADIVINO"
            )

            else -> emptyList()
        }
    }

    // Método legacy para compatibilidad
    @Deprecated("Use getWords() instead", ReplaceWith("getWords(length, \"common\")"))
    fun loadWordsFromResource(resourcePath: String, length: Int = 5): List<String> {
        return getCommonWords(length)
    }
}
