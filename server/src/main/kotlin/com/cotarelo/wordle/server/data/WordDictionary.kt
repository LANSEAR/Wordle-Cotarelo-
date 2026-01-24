package com.cotarelo.wordle.server.data

/**
 * Diccionario de palabras del servidor
 * En una implementación real, esto cargaría desde archivos .txt
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
                "PATO", "PELO", "PINO", "CAMA", "SOFÁ", "TREN", "AUTO", "MANO",
                "DEDO", "BOCA", "CARA", "OJOS", "PIES", "ALMA", "BESO", "DATO",
                "FOCO", "HOYO", "IDEA", "LODO", "MOTO", "NOTA", "OLOR", "PALA",
                "RAMA", "ROPA", "SALA", "TAPA", "VASO", "ZONA", "BOTE", "CAJA",
                "PASO", "PESO", "PISO", "ROSA", "SACO", "SEDA", "TEMA", "TORO"
            )
            5 -> listOf(
                "PLAYA", "CAMPO", "MUNDO", "TIERRA", "FUEGO", "NOCHE", "TARDE", "LIBRO",
                "PAPEL", "VERDE", "ROJO", "AZUL", "NEGRO", "ROSA", "ALTO", "BAJO",
                "MESA", "SILLA", "PUERTA", "PARED", "FLOR", "PLANTA", "FRUTA", "PERA",
                "COCHE", "MOTO", "TREN", "BARCO", "CIELO", "COLOR", "FONDO", "FORMA",
                "GENTE", "GRUPO", "HECHO", "HISTORIA", "LUGAR", "MEDIO", "NIVEL", "ORDEN",
                "PARTE", "PUNTO", "RADIO", "SIGLO", "SUELO", "TIEMPO", "VISTA", "VOCAL"
            )
            6 -> listOf(
                "comida", "bebida", "cocina", "salón", "cuarto", "baño", "jardín", "parque",
                "escuela", "colegio", "iglesia", "templo", "museo", "teatro", "cinema", "estadio",
                "plaza", "calle", "avenida", "camino", "bosque", "montaña", "volcán", "desierto",
                "río", "lago", "océano", "isla", "costa", "playa", "arena", "roca",
                "piedra", "metal", "hierro", "cobre", "plata", "oro", "vidrio", "madera"
            )
            7 -> listOf(
                "ventana", "palabra", "mensaje", "historia", "cultura", "música", "pintura", "escultura",
                "trabajo", "estudio", "ciencia", "matemática", "física", "química", "biología", "anatomía",
                "medicina", "hospital", "clínica", "farmacia", "doctor", "enfermera", "paciente", "tratamiento",
                "gobierno", "política", "economía", "finanzas", "empresa", "negocio", "mercado", "comercio"
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
                "EDIL", "FARO", "HALO", "LIMO", "MICA", "NETO", "ODIO", "PUMA"
            )
            5 -> listOf(
                "AMBAR", "ATOMO", "EBANO", "ETER", "OPALO", "OVULO", "ICONO", "EPICA",
                "FAENA", "GOLPE", "HURTO", "INDIO", "JOVEN", "KARMA", "LAMPA", "MANGO"
            )
            6 -> listOf(
                "ábside", "acacia", "acento", "ácido", "acorde", "afable", "afecto", "agente",
                "águila", "álbum", "alcoba", "alegre", "alerta", "alfiler", "álgebra", "aliado"
            )
            7 -> listOf(
                "abejón", "abnegar", "abordar", "abrazar", "absoluto", "absorber", "abstener", "abundar",
                "acabado", "academia", "acampar", "acariciar", "acceder", "acelerar", "acertijo", "aclamar"
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

        return words.randomOrNull() ?: error("No hay palabras para longitud $length")
    }

    /**
     * Verifica si una palabra está en el diccionario
     */
    fun isValidWord(word: String): Boolean {
        val length = word.length
        return word.uppercase() in getWords(length).map { it.uppercase() }
    }
}
