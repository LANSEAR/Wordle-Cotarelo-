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
                "CASA", "MESA", "GATO", "AMOR", "VIDA", "AGUA", "SOPA", "LUNA",
                "PATO", "PELO", "PINO", "CAMA", "SOFA", "TREN", "AUTO", "MANO",
                "DEDO", "BOCA", "CARA", "OJOS", "PIES", "ALMA", "BESO", "DATO",
                "FOCO", "HOYO", "IDEA", "LODO", "MOTO", "NOTA", "OLOR", "PALA",
                "RAMA", "ROPA", "SALA", "TAPA", "VASO", "ZONA", "BOTE", "CAJA",
                "PASO", "PESO", "PISO", "ROSA", "SACO", "SEDA", "TEMA", "TORO",
                "LAVA", "NUBE", "CUBO", "PELO", "MAPA", "PALA", "RATA", "TUBO",
                "CERO", "HUMO", "LAGO", "PATO", "RAYO", "SACO", "TAPA", "VELA"
            )
            5 -> listOf(
                "PLAYA", "CAMPO", "MUNDO", "TIERRA", "FUEGO", "NOCHE", "TARDE", "LIBRO",
                "PAPEL", "VERDE", "ROJO", "AZUL", "NEGRO", "ROSA", "ALTO", "BAJO",
                "MESA", "SILLA", "PUERTA", "PARED", "FLOR", "PLANTA", "FRUTA", "PERA",
                "COCHE", "MOTO", "TREN", "BARCO", "CIELO", "COLOR", "FONDO", "FORMA",
                "GENTE", "GRUPO", "HECHO", "LUGAR", "MEDIO", "NIVEL", "ORDEN",
                "PARTE", "PUNTO", "RADIO", "SIGLO", "SUELO", "TIEMPO", "VISTA", "VOCAL",
                "NIEVE", "CARNE", "BRAZO", "DEDO", "PIANO", "FERIA", "JUEGO", "LLAMA"
            )
            6 -> listOf(
                "COMIDA", "BEBIDA", "COCINA", "SALON", "CUARTO", "JARDIN", "PARQUE",
                "ESCUELA", "COLEGIO", "IGLESIA", "TEMPLO", "MUSEO", "TEATRO", "ESTADIO",
                "PLAZA", "CALLE", "CAMINO", "BOSQUE", "VOLCAN", "DESIERTO",
                "LAGO", "OCEANO", "ISLA", "COSTA", "PLAYA", "ARENA", "ROCA",
                "PIEDRA", "METAL", "HIERRO", "COBRE", "PLATA", "VIDRIO", "MADERA",
                "CIUDAD", "PUEBLO", "CENTRO", "BARRIO", "MERCADO", "TIENDA", "BANCO"
            )
            7 -> listOf(
                "VENTANA", "PALABRA", "MENSAJE", "CULTURA", "MUSICA", "PINTURA",
                "TRABAJO", "ESTUDIO", "CIENCIA", "FISICA", "QUIMICA", "BIOLOGIA",
                "MEDICINA", "HOSPITAL", "CLINICA", "FARMACIA", "DOCTOR", "PACIENTE",
                "GOBIERNO", "ECONOMIA", "FINANZAS", "EMPRESA", "NEGOCIO", "MERCADO", "COMERCIO",
                "HISTORIA", "BATALLA", "VICTORIA", "DERROTA", "EJERCITO", "SOLDADO"
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
