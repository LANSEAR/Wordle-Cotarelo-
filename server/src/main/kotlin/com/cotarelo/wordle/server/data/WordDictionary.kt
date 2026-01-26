package com.cotarelo.wordle.server.data

/**
 * Diccionario de palabras del servidor
 * TODAS LAS PALABRAS EN MAYUSCULAS SIN TILDES
 */
object WordDictionary {

    /**
     * Obtiene el diccionario completo para una longitud de palabra
     */
    fun getWords(length: Int, difficulty: String = "all"): List<String> {
        val commonWords = getCommonWords(length)
        val rareWords = getRareWords(length)

        return when (difficulty.lowercase()) {
            "easy", "common" -> commonWords
            "hard", "rare" -> rareWords
            else -> commonWords + rareWords
        }
    }

    /**
     * Obtiene palabras comunes según longitud
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
                "ARCO", "ASNO", "AULA", "BAÑO", "BOLA", "CAMA", "CAPA",
                "CAÑA", "CINE", "COCO", "COLA", "COPA", "CUNA", "CUÑA", "DAMA",
                "DADO", "DOTE", "FAMA", "FILA", "FOTO", "GAFA", "GOMA", "HADA",
                "HIJO", "HOJA", "ISLA", "JOYA", "KILO", "LADO", "LANA",
                "LEON", "LIMA", "LINO", "LOBO", "LOTE", "LUJO", "LUTO", "MAIZ",
                "MALO", "MAPA", "MASA", "META", "MIEL", "MINA", "MODO", "MOÑO",
                "NIÑO", "NIÑA", "NIDO", "NUBE", "ONDA", "OBRA", "ODIO", "PAJA",
                "PALO", "PAPA", "PARE", "PATO", "PAVO", "PERA", "PICO",
                "PILA", "PINO", "PIPA", "PLAN", "POLO", "POMO", "POZA", "POZO",
                "PUMA", "PUNO", "RAMO", "RANA", "RAZO", "REMO", "RETO", "RIFA",
                "RIMA", "RITO", "SACO", "SALA", "SAPO", "SEDA", "SENO", "SETA",
                "SILO", "SINO", "SOFA", "SOLO", "TACO", "TAJO", "TELA", "TIPO",
                "TIRO", "TIZA", "TODO", "TOMO", "TONO", "TOPO", "TUBO", "TUFO"
                ,"URNA", "VANO", "VASO", "VELA", "VETA", "VIÑA",
                "VISA", "VOTO", "YEMA", "YESO", "YUGO", "ZETA", "ZINC", "ZUMO"
            )
            5 -> listOf(
                // Palabras muy comunes
                "PLAYA", "CAMPO", "MUNDO", "TIERRA", "FUEGO", "NOCHE", "TARDE", "LIBRO",
                "PAPEL", "VERDE", "NEGRO",
                "SILLA", "PUERTA", "PARED", "FRUTA",
                "COCHE", "BARCO", "CIELO", "COLOR", "FONDO", "FORMA",
                "GENTE", "GRUPO", "HECHO", "LUGAR", "MEDIO", "NIVEL", "ORDEN",
                "PARTE", "PUNTO", "RADIO", "SIGLO", "SUELO", "VISTA", "VOCAL",
                "NIEVE", "CARNE", "BRAZO", "DEDO", "PIANO", "FERIA", "JUEGO", "LLAMA",
                // Sustantivos comunes
                "ARENA", "BANCO", "BARRO", "BOLSA", "BOMBA", "CABLE", "CALLE", "CALOR",
                "CARTA", "CERRO", "CHICO", "CLASE", "COSTA", "CREMA", "CUERO",
                "CURSO", "DANZA", "DISCO", "DOLOR", "DROGA", "DUCHA", "FECHA",
                "FIESTA", "FIRMA", "FRENO","GASTO", "GOLPE", "GRASA", "GRUTA",
                "GUERRA", "GUSTO", "HABLA", "HIELO", "HORNO", "HOTEL", "HUESO", "HUMOR",
                "JABON", "JOVEN", "JUGO", "LABOR", "LECHE", "LETRA", "LÍNEA",
                "LISTA", "LLAVE", "LUCHA", "MADRE", "MARCA", "MARZO", "METAL",
                "METRO", "MONTE", "MUSEO", "NOVIO", "PADRE",
                "PALMA", "PATIO", "PELEA",
                "PIEZA", "PLAZA", "POEMA", "POLLO", "PRISA",
                "PUEBLO", "PULSO", "QUESO", "RATON", "REINO", "RELOJ",
                "RESTO", "RITMO", "RUEDA", "RUIDO", "SALSA",
                "SALTO", "SELVA", "SEÑAL", "SILLA", "SITIO",
                "SUEÑO", "TABLA", "TALLA", "TECHO",
                "TIGRE", "TINTA", "TORRE", "TORTA", "TRAJE", "TRAMO",
                "TRATO", "TRIBU", "TRONO", "TROZO", "UNION", "VALOR", "VASO", "VENTA",
                "VERSO", "VIAJE", "VIENTO","VIRUS", "VISTA",
                // Verbos comunes (infinitivos)
                "ANDAR", "ABRIR", "BEBER", "COMER", "CREER", "DEBER", "DECIR",
                "HACER","LEER", "MIRAR","PASAR",
                "PEDIR", "PONER", "PODER", "SABER", "SALIR", "TENER", "TOMAR", "TRAER",
                "VENIR", "VIVIR", "VOLAR", "CREAR", "BAJAR", "SUBIR", "SACAR",
                "GANAR", "TOCAR", "SOÑAR",
                // Adjetivos
                "BUENO", "MALO", "NUEVO", "VIEJO", "JOVEN", "LARGO", "CORTO",
                "ANCHO", "CLARO", "SUCIO",
                "POBRE", "FELIZ", "LENTO", "DEBIL",
                "DULCE","LLENO", "VACIO", "CERCA", "LEJOS"
            )
            6 -> listOf(
                "COMIDA", "BEBIDA", "COCINA", "CUARTO", "JARDIN", "PARQUE",
                "COLEGIO","TEMPLO", "TEATRO", "ESTADO",
                "PLAZAS", "CALLES", "CAMINO", "BOSQUE", "VOLCAN",
                "OCEANO",
                "PIEDRA","HIERRO","VIDRIO", "MADERA",
                "CIUDAD", "PUEBLO", "CENTRO", "BARRIO", "MERCADO", "TIENDA",
            )
            7 -> listOf(
                "VENTANA", "PALABRA", "MENSAJE", "CULTURA","PINTURA",
                "TRABAJO", "ESTUDIO", "CIENCIA","QUIMICA",
                "CLINICA", "FARMACO","PACIENTE",
                "FINANZA", "EMPRESA", "NEGOCIO", "MERCADO",
                "BATALLA", "DERROTA", "SOLDADO"
            )
            else -> emptyList()
        }
    }

    /**
     * Obtiene palabras raras/difíciles según longitud
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
                "FAENA", "GOLPE", "HURTO", "INDIO", "JOVEN", "KARMA", "LAMPA", "MANGO",
                "NEXO", "NYLON", "OMEGA", "PIQUE", "QUESO", "RUEDA", "SAQUE"
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

    /**
     * Selecciona una palabra aleatoria según dificultad
     */
    fun getRandomWord(length: Int, difficulty: String = "normal"): String {
        val words = when (difficulty.lowercase()) {
            "easy" -> getCommonWords(length)
            "hard" -> getRareWords(length)
            "normal" -> {
                val common = getCommonWords(length)
                val rare = getRareWords(length)
                // 70% comunes, 30% raras
                if (Math.random() < 0.7) common else rare
            }
            else -> getWords(length)
        }

        return words.randomOrNull()?.uppercase() ?: error("No hay palabras para longitud $length")
    }

    /**
     * Verifica si una palabra está en el diccionario
     */
    fun isValidWord(word: String): Boolean {
        val length = word.length
        val normalizedWord = word.uppercase()
        return normalizedWord in getWords(length).map { it.uppercase() }
    }
}
