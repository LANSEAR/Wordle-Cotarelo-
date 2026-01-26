package com.cotarelo.wordle.server.data

/**
 * Diccionario de palabras del servidor
 * TODAS LAS PALABRAS EN MAYUSCULAS SIN TILDES
 *
 * Categorías de dificultad:
 * - FACIL: Palabras muy comunes del día a día
 * - MEDIA: Palabras comunes pero menos frecuentes
 * - DIFICIL: Palabras poco comunes o técnicas
 * - MIXTA: Mezcla de todas las categorías
 */
object WordDictionary {

    /**
     * Obtiene el diccionario completo para una longitud de palabra según dificultad
     */
    fun getWords(length: Int, difficulty: String = "MIXTA"): List<String> {
        val easy = getEasyWords(length)
        val medium = getMediumWords(length)
        val hard = getHardWords(length)

        return when (difficulty.uppercase()) {
            "EASY", "FACIL" -> easy
            "NORMAL", "MEDIA", "MEDIUM" -> medium
            "HARD", "DIFICIL", "DIFFICULT" -> hard
            else -> easy + medium + hard // MIXTA
        }
    }

    /**
     * Palabras FÁCILES - Muy comunes del día a día
     */
    private fun getEasyWords(length: Int): List<String> {
        return when (length) {
            4 -> listOf(
                // Palabras básicas del vocabulario diario
                "CASA", "MESA", "GATO", "AMOR", "VIDA", "AGUA", "SOPA", "LUNA",
                "PATO", "PELO", "PINO", "CAMA", "SOFA", "TREN", "AUTO", "MANO",
                "DEDO", "BOCA", "CARA", "OJOS", "PIES", "BESO", "DATO",
                "HORA", "IDEA", "NOTA", "PASO", "PESO", "PISO", "ROSA",
                "TEMA", "TORO", "NUBE", "CUBO", "MAPA", "TUBO", "VELA",
                "CERO", "HUMO", "LAGO", "RAYO", "BOLA", "CINE", "COPA",
                "FOTO", "LEON", "LOBO", "MALO", "NIÑO", "NIÑA", "PAPA",
                "PATO", "PERA", "TELA", "VASO", "ZONA", "BOTE", "CAJA"
            )
            5 -> listOf(
                // Palabras cotidianas
                "CAMPO", "MUNDO", "FUEGO", "NOCHE", "TARDE", "LIBRO",
                "PAPEL", "VERDE", "NEGRO", "SILLA", "PUERTA", "PARED",
                "COCHE", "BARCO", "CIELO", "COLOR", "FORMA", "GENTE",
                "LUGAR", "PARTE", "PUNTO", "SUELO", "VISTA", "NIEVE",
                "CARNE", "BRAZO", "PIANO", "JUEGO", "BANCO", "BOLSA",
                "CALLE", "CALOR", "CARTA", "CHICO", "CLASE", "COSTA",
                "DISCO", "DUCHA", "FECHA", "FIESTA", "HIELO", "HOTEL",
                "LECHE", "LETRA", "LISTA", "LLAVE", "MADRE", "METAL",
                "PADRE", "PLAZA", "POLLO", "QUESO", "RELOJ", "RUIDO",
                "SALTO", "SITIO", "SUEÑO", "TABLA", "TIGRE", "TORRE"
            )
            6 -> listOf(
                // Palabras familiares
                "COMIDA", "BEBIDA", "COCINA", "CUARTO", "JARDIN", "PARQUE",
                "COLEGIO", "TEMPLO", "TEATRO", "CAMINO", "BOSQUE", "CIUDAD",
                "PUEBLO", "CENTRO", "BARRIO", "MERCADO", "TIENDA", "PUERTA",
                "CAMISA", "ZAPATO", "ESPEJO", "PIEDRA", "MADERA"
            )
            7 -> listOf(
                // Palabras comunes
                "VENTANA", "PALABRA", "MENSAJE", "CULTURA", "PINTURA",
                "TRABAJO", "ESTUDIO", "ESCUELA", "CAMINAR", "ESCALON",
                "COCINAR", "ARMARIO", "ZAPATOS", "ESPEJOS", "LECTURA",
                "CAMELLO", "SENDERO", "PLANETA", "REFUGIO", "DESTINO"
            )
            else -> emptyList()
        }
    }

    /**
     * Palabras MEDIAS - Comunes pero menos frecuentes
     */
    private fun getMediumWords(length: Int): List<String> {
        return when (length) {
            4 -> listOf(
                // Palabras conocidas pero menos usadas
                "ALMA", "FOCO", "HOYO", "LODO", "MOTO", "OLOR", "PALA",
                "RAMA", "ROPA", "SALA", "TAPA", "SACO", "SEDA", "LAVA",
                "RATA", "ARCO", "ASNO", "AULA", "BAÑO", "CAPA", "CAÑA",
                "COCO", "COLA", "CUNA", "CUÑA", "DAMA", "DADO", "DOTE",
                "FAMA", "FILA", "GAFA", "GOMA", "HADA", "HIJO", "HOJA",
                "ISLA", "JOYA", "KILO", "LADO", "LANA", "LIMA", "LINO",
                "LOTE", "LUJO", "LUTO", "MAIZ", "MASA", "META", "MIEL",
                "MINA", "MODO", "MOÑO", "NIDO", "ONDA", "OBRA", "ODIO",
                "PAJA", "PALO", "PARE", "PAVO", "PICO", "PILA", "PIPA",
                "PLAN", "POLO", "POMO", "POZA", "POZO", "PUMA", "PUNO",
                "RAMO", "RANA", "RAZO", "REMO", "RETO", "RIFA", "RIMA",
                "RITO", "SAPO", "SENO", "SETA", "SILO", "SINO", "SOLO",
                "TACO", "TAJO", "TIPO", "TIRO", "TIZA", "TODO", "TOMO",
                "TONO", "TOPO", "TUFO", "URNA", "VANO", "VETA", "VIÑA",
                "VISA", "VOTO", "YEMA", "YESO", "YUGO", "ZETA", "ZINC", "ZUMO"
            )
            5 -> listOf(
                // Palabras comunes pero no tan cotidianas
                "PLAYA", "TIERRA", "FRUTA", "FONDO", "GRUPO", "HECHO",
                "MEDIO", "NIVEL", "ORDEN", "RADIO", "SIGLO", "VOCAL",
                "FERIA", "LLAMA", "ARENA", "BARRO", "BOMBA",
                "CABLE", "CERRO", "CREMA", "CUERO", "CURSO", "DANZA",
                "DOLOR", "DROGA", "FIRMA", "FRENO", "GASTO", "GOLPE",
                "GRASA", "GRUTA", "GUERRA", "GUSTO", "HABLA", "HORNO",
                "HUESO", "HUMOR", "JABON", "JOVEN", "LABOR", "LINEA",
                "LUCHA", "MARCA", "MARZO", "METRO", "MONTE", "MUSEO",
                "NOVIO", "PALMA", "PATIO", "PELEA", "PIEZA", "POEMA",
                "PRISA","PULSO", "RATON", "REINO", "RESTO",
                "RITMO", "RUEDA", "SALSA", "SELVA", "SEÑAL", "TALLA",
                "TECHO", "TINTA", "TORTA", "TRAJE", "TRAMO", "TRATO",
                "TRIBU", "TRONO", "TROZO", "UNION", "VALOR", "VENTA",
                "VERSO", "VIAJE", "VIENTO", "VIRUS", "ANDAR", "ABRIR",
                "BEBER", "COMER", "CREER", "DEBER", "DECIR", "HACER",
                "LEER", "MIRAR", "PASAR", "PEDIR", "PONER", "PODER",
                "SABER", "SALIR", "TENER", "TOMAR", "TRAER", "VENIR",
                "VIVIR", "VOLAR", "CREAR", "BAJAR", "SUBIR", "SACAR",
                "GANAR", "TOCAR", "SOÑAR", "BUENO", "NUEVO", "VIEJO",
                "JOVEN", "LARGO", "CORTO", "ANCHO", "CLARO", "SUCIO",
                "POBRE", "FELIZ", "LENTO", "DEBIL", "DULCE", "LLENO",
                "VACIO", "CERCA", "LEJOS"
            )
            6 -> listOf(
                // Palabras de uso medio
                "VOLCAN", "OCEANO", "HIERRO", "VIDRIO", "ESTADO", "PLAZAS",
                "CALLES", "BALCON", "FIGURA", "CUADRO", "TIEMPO", "SONIDO",
                "MUSICA", "LETRAS", "NOVELA", "CUENTO", "PAGINA", "ABRIGO",
                "AMIGOS", "VECINO", "VEREDA", "VIAJES", "BOLETO", "MALETA",
                "PASAJE", "AGENDA", "CORREO", "POSTAL", "CAMION", "TRENES",
                "BARCOS", "VUELOS", "MUELLE", "ANIMAL", "ARROYO", "BALADA",
                "BODEGA", "BRONCE", "CABINA", "CADENA", "CALIDO", "CARCEL",
                "CARIÑO", "COLINA", "CORONA", "CUERDA", "DORMIR", "DRAGON",
                "EDITAR", "ENFADO", "ESTUFA", "EXAMEN", "FLORES", "FUSION",
                "GANADO", "GENERO", "GRANDE", "HERIDO", "IDIOMA", "IMAGEN",
                "INFLAR", "ISLOTE", "LIQUEN", "LLUVIA", "MANTRA", "MEZCLA",
                "MONEDA", "MORADA", "NACION", "OBJETO", "ORIGEN"
            )
            7 -> listOf(
                // Palabras de uso medio
                "CIENCIA", "FISICA", "QUIMICA", "BIOLOGIA", "MEDICINA",
                "HOSPITAL", "CLINICA", "FARMACIA", "PACIENTE", "GOBIERNO",
                "HISTORIA", "BATALLA", "VICTORIA", "DERROTA", "EJERCITO",
                "SOLDADO", "LIBRERO", "PAPELES", "LAPICES", "ESCRITO",
                "NOVELAS", "CUENTOS", "REVISTA", "PERIODO", "PORTADA",
                "PAGINAS", "ARCHIVO", "CARPETA", "AGENDAS", "PAQUETE",
                "ALMACEN", "TIENDAS", "COMPRAS", "OFERTAS", "FACTURA",
                "RECIBOS", "AMISTAD", "MONTANA", "REFLEJO", "SONRISA",
                "AVIONES", "GALERIA", "ARTISTA", "PROFETA", "RELATAR",
                "REFORMA", "COSTERO", "VIAJERO", "FACHADA", "LECTORA",
                "IMPULSO", "CEREBRO", "EMOCION", "LIBRETA", "SILUETA",
                "CAMPEON", "RELIEVE", "MANZANA", "CRISTAL", "TORNADO",
                "VOLUMEN", "ESPACIO", "PAISAJE", "DOMINIO", "PORTERO",
                "COLORES", "CARRERA", "BOTELLA", "MASCARA", "BANDERA",
                "REUNION", "VENTAJA", "CALZADO", "CABALLO", "PESCADO",
                "BALANCE", "EDICION", "FABRICA"
            )
            else -> emptyList()
        }
    }

    /**
     * Palabras DIFÍCILES - Poco comunes o técnicas
     */
    private fun getHardWords(length: Int): List<String> {
        return when (length) {
            4 -> listOf(
                // Palabras poco frecuentes o técnicas
                "YATE", "YOGA", "YODO", "YUCA", "APTO", "AUGE", "AXIS",
                "BUEY", "CAYO", "EDIL", "FARO", "HALO", "LIMO", "MICA",
                "NETO", "RUBO", "VADO"
            )
            5 -> listOf(
                // Palabras raras o especializadas
                "AMBAR", "ATOMO", "EBANO", "ETER", "OPALO", "OVULO",
                "ICONO", "EPICA", "FAENA", "HURTO", "INDIO", "KARMA",
                "LAMPA", "MANGO", "NEXO", "NYLON", "OMEGA", "PIQUE", "SAQUE"
            )
            6 -> listOf(
                // Palabras cultas o técnicas
                "ABSIDE", "ACACIA", "ACENTO", "ACORDE", "AFABLE",
                "AFECTO", "AGENTE", "AGUILA","ALCOBA", "ALEGRE",
                "ALERTA", "ALIADO", "AMABLE", "ANILLO",
                "ALTURA", "ASTUTO"
            )
            7 -> listOf(
                // Palabras cultas o especializadas
                "ABNEGAR", "ABORDAR", "ABRAZAR",
                "ABUNDAR", "ACABADO",
                "ACAMPAR", "ACCEDER", "ACLAMAR",
                "ACUARIO", "ADIVINO",
                "EMPRESA", "NEGOCIO",
            )
            else -> emptyList()
        }
    }

    /**
     * Selecciona una palabra aleatoria según dificultad
     */
    fun getRandomWord(length: Int, difficulty: String = "MIXTA"): String {
        val words = when (difficulty.uppercase()) {
            "EASY", "FACIL" -> getEasyWords(length)
            "NORMAL", "MEDIA", "MEDIUM" -> getMediumWords(length)
            "HARD", "DIFICIL", "DIFFICULT" -> getHardWords(length)
            else -> { // MIXTA
                val easy = getEasyWords(length)
                val medium = getMediumWords(length)
                val hard = getHardWords(length)
                easy + medium + hard
            }
        }

        return words.randomOrNull()?.uppercase() ?: error("No hay palabras para longitud $length y dificultad $difficulty")
    }

    /**
     * Verifica si una palabra está en el diccionario (cualquier dificultad)
     */
    fun isValidWord(word: String): Boolean {
        val length = word.length
        val normalizedWord = word.uppercase()
        return normalizedWord in getWords(length, "MIXTA") // Valida contra todas las palabras
    }
}
