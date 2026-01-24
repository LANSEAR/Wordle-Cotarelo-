# 🎮 Wordle Multijugador - Kotlin Multiplatform

Implementación de Wordle multijugador con modo **PVE (Jugador vs IA)** desarrollado en Kotlin Multiplatform con soporte para Desktop (Windows).

---

## 📋 Características Implementadas

### ✅ Modo de Juego Local
- Juego Wordle single-player completamente funcional
- Palabras de 4-7 letras configurables
- Intentos configurables (4-10)
- Sistema de rondas (Best-Of 1, 3, 5, 7)
- Temporizador opcional (30-180 segundos)
- 3 niveles de dificultad (Fácil, Normal, Difícil)

### ✅ Modo PVE (Jugador vs IA)
- Conexión cliente-servidor mediante sockets TCP
- IA con 3 niveles de dificultad:
  - **Fácil**: Intentos aleatorios del diccionario
  - **Normal**: Elimina palabras imposibles y usa frecuencia de letras
  - **Difícil**: Algoritmo optimizado que maximiza información en cada intento
- Sistema de rondas competitivas
- Visualización en tiempo real de los movimientos de la IA
- Validación de palabras en el servidor

### ✅ Servidor Multijugador
- Servidor TCP con gestión de múltiples clientes concurrentes
- Configuración mediante archivo `server.properties`
- Sistema de records con estadísticas de jugadores
- Protocolo de comunicación JSON con `kotlinx.serialization`
- Manejo robusto de errores y desconexiones

### ✅ Interfaz Gráfica
- Compose for Desktop con Material Design
- Tema claro/oscuro con persistencia
- Tablero 6x5 con animaciones
- Teclado virtual QWERTY + Ñ
- Feedback visual según reglas Wordle (🟩🟨⬜)

---

## 🏗️ Arquitectura del Proyecto

```
Wordle-Cotarelo-/
├── client-desktop/          # Cliente Desktop (Compose Multiplatform)
│   ├── src/main/kotlin/
│   │   └── com/cotarelo/wordle/client/
│   │       ├── data/           # Repositorio de palabras
│   │       ├── network/        # Conexión al servidor
│   │       ├── settings/       # Configuración persistente
│   │       ├── state/          # Controladores de juego
│   │       └── ui/             # Interfaz gráfica Compose
│   └── resources/              # Diccionarios (8 archivos .txt)
│
├── server/                   # Servidor TCP
│   ├── src/main/kotlin/
│   │   └── com/cotarelo/wordle/server/
│   │       ├── ai/            # IA de Wordle (3 dificultades)
│   │       ├── config/        # Carga de server.properties
│   │       ├── data/          # Diccionarios y records
│   │       ├── game/          # Lógica de sesiones de juego
│   │       └── network/       # Manejo de clientes TCP
│   ├── server.properties      # Configuración del servidor
│   └── records.json           # Estadísticas de jugadores
│
└── shared/                   # Código compartido (KMP)
    └── src/commonMain/kotlin/
        └── com/cotarelo/wordle/shared/
            ├── model/          # Evaluación de palabras
            └── network/        # Protocolo de mensajes JSON
```

---

## 🚀 Compilación y Ejecución

### Requisitos
- **Java Development Kit (JDK) 21**
- Gradle (incluido via wrapper)

### 1. Compilar el Proyecto

```bash
# En la raíz del proyecto
./gradlew build
```

### 2. Ejecutar el Servidor

```bash
# Opción 1: Usando Gradle
./gradlew :server:run

# Opción 2: Usando JAR
cd server
java -jar build/libs/server.jar
```

**Salida esperada:**
```
╔═══════════════════════════════════════════════════════╗
║     🎮 SERVIDOR WORDLE MULTIJUGADOR INICIADO 🎮      ║
╚═══════════════════════════════════════════════════════╝

📋 Configuración del Servidor:
   Host: localhost
   Puerto: 5678
   Máximo de clientes: 10
   ...

🚀 Servidor escuchando en localhost:5678
⏳ Esperando conexiones de clientes...
```

### 3. Ejecutar el Cliente

```bash
# Opción 1: Usando Gradle
./gradlew :client-desktop:run

# Opción 2: Generar ejecutable
./gradlew :client-desktop:packageUberJarForCurrentOS
# Ejecutar: client-desktop/build/compose/jars/client-desktop-*.jar
```

---

## ⚙️ Configuración

### Archivo `server.properties`

```properties
# Host del servidor
server.host=localhost

# Puerto TCP para conexiones
server.port=5678

# Número máximo de clientes simultáneos
max.clients=10

# Timeout de conexión en segundos
connection.timeout=300

# Habilitar logs detallados
debug.enabled=true
```

### Configuración del Cliente

Se puede modificar desde la pantalla **Configuración** en el menú principal:

- **Longitud de palabra**: 4-7 letras
- **Intentos máximos**: 4-10
- **Rondas (Best-Of)**: 1, 3, 5, 7
- **Dificultad**: Fácil, Normal, Difícil
- **Temporizador**: 30-180 segundos (opcional)
- **Tema**: Claro u Oscuro

---

## 📡 Protocolo de Comunicación

### Mensajes Cliente → Servidor

#### 1. Iniciar Partida
```json
{
  "type": "StartGame",
  "mode": "PVE",
  "rounds": 3,
  "wordLength": 5,
  "maxAttempts": 6,
  "difficulty": "NORMAL"
}
```

#### 2. Enviar Intento
```json
{
  "type": "Guess",
  "word": "PERRO",
  "attemptNumber": 1
}
```

#### 3. Sincronizar Records
```json
{
  "type": "SyncRecords"
}
```

### Mensajes Servidor → Cliente

#### 1. Confirmación de Inicio
```json
{
  "type": "GameStarted",
  "gameId": "uuid-1234",
  "wordLength": 5,
  "maxAttempts": 6,
  "rounds": 3
}
```

#### 2. Resultado de Intento
```json
{
  "type": "GuessResult",
  "word": "PERRO",
  "result": [
    {"state": "CORRECT"},
    {"state": "PRESENT"},
    {"state": "ABSENT"},
    {"state": "ABSENT"},
    {"state": "CORRECT"}
  ],
  "isValid": true,
  "message": null
}
```

#### 3. Movimiento de IA
```json
{
  "type": "AIMove",
  "word": "GATOS",
  "attemptNumber": 2,
  "result": [...]
}
```

#### 4. Ganador de Ronda
```json
{
  "type": "RoundWinner",
  "winner": "PLAYER",
  "attempts": 4,
  "solution": "MUNDO"
}
```

#### 5. Ganador Final
```json
{
  "type": "GameWinner",
  "winner": "PLAYER",
  "playerRounds": 2,
  "aiRounds": 1
}
```

---

## 🤖 Sistema de IA

### Estrategias por Dificultad

#### FÁCIL
- Selección aleatoria de palabras del diccionario
- No aprende de intentos anteriores
- Ideal para jugadores principiantes

#### NORMAL
- Elimina palabras imposibles basándose en feedback
- Usa frecuencia de letras en español
- Mezcla 70% palabras comunes + 30% raras
- Balance entre desafío y jugabilidad

#### DIFÍCIL
- Algoritmo optimizado que maximiza información
- Prioriza letras confirmadas y posiciones correctas
- Minimiza el espacio de búsqueda eficientemente
- Desafío máximo para jugadores expertos

### Ejemplo de Comportamiento de IA (DIFÍCIL)

```
Palabra secreta: MUNDO
Turno 1: RATOS → 🟨⬜⬜🟨⬜  (R y O presentes)
Turno 2: ROMPO → 🟩🟩⬜⬜🟨  (M-U correctos, O presente)
Turno 3: MUNDO → 🟩🟩🟩🟩🟩  ¡GANÓ!
```

---

## 📊 Sistema de Records

### Estructura de `records.json`

```json
{
  "players": {
    "Player1": {
      "gamesWon": 5,
      "gamesLost": 2,
      "totalGames": 7,
      "currentStreak": 2,
      "maxStreak": 4,
      "averageAttempts": 4.2,
      "attemptsDistribution": {
        "3": 1,
        "4": 3,
        "5": 2,
        "6": 1
      },
      "wordsGuessed": 15,
      "totalWords": 18
    }
  }
}
```

### Estadísticas Rastreadas

- **Partidas ganadas/perdidas/totales**
- **Racha actual y máxima de victorias**
- **Promedio de intentos por palabra**
- **Distribución de intentos** (cuántas veces ganó en 3, 4, 5... intentos)
- **Porcentaje de palabras adivinadas**
- **Win Rate** (tasa de victorias)

---

## 🎯 Cómo Jugar

### Modo Local (1 Jugador)

1. Abre el cliente
2. Click en **"Nueva partida (Local)"**
3. Adivina la palabra en 6 intentos
4. Usa las pistas de colores:
   - 🟩 **Verde**: Letra correcta en posición correcta
   - 🟨 **Amarillo**: Letra existe pero en otra posición
   - ⬜ **Gris**: Letra no existe en la palabra

### Modo PVE (vs IA)

1. **Inicia el servidor** (ver sección de ejecución)
2. Abre el cliente
3. Click en **"Nueva partida PVE (vs IA)"**
4. Compite contra la IA:
   - Ambos intentan adivinar la misma palabra
   - Gana quien adivine primero
   - Si ambos adivinan, gana quien usó menos intentos
5. Juega múltiples rondas (configurables)
6. El ganador es quien gane más rondas

---

## 🛠️ Tecnologías Utilizadas

- **Kotlin Multiplatform (KMP)**: Compartición de código entre módulos
- **Compose for Desktop**: UI moderna y declarativa
- **kotlinx.serialization**: Serialización JSON
- **kotlinx.coroutines**: Manejo concurrente de clientes
- **Java Sockets**: Comunicación TCP cliente-servidor
- **Material Design**: Diseño de interfaz
- **Gradle**: Build system

---

## 📝 Características Pendientes (Futuras)

- [ ] Modo PVP (Jugador vs Jugador)
- [ ] Pantalla de Records funcional con gráficos
- [ ] Sincronización de records con el cliente
- [ ] Persistencia de sesiones de juego
- [ ] Sala de espera para matchmaking
- [ ] Chat entre jugadores
- [ ] Sonidos y animaciones mejoradas
- [ ] Soporte para más idiomas

---

## 🐛 Solución de Problemas

### El servidor no inicia

**Problema**: `Address already in use`

**Solución**: El puerto 5678 ya está en uso. Cambia el puerto en `server.properties`:
```properties
server.port=5679
```

### El cliente no puede conectar

**Problema**: "Error de conexión"

**Soluciones**:
1. Verifica que el servidor esté ejecutándose
2. Confirma que el puerto en `server.properties` sea 5678
3. Verifica firewall/antivirus (permite localhost:5678)

### Diccionarios vacíos

**Problema**: "Diccionarios vacíos para longitud X"

**Solución**: Los archivos de recursos deben estar en:
```
client-desktop/src/main/resources/
  - words_es_4_common.txt
  - words_es_4_rare.txt
  - words_es_5_common.txt
  - words_es_5_rare.txt
  - (... hasta 7)
```

---

## 👨‍💻 Autor

**Cotarelo**
Proyecto desarrollado como parte de la asignatura de Kotlin Multiplatform - Juegos Multijugador

---

## 📄 Licencia

Este proyecto es de uso educativo.

---

## 🙏 Agradecimientos

- Reglas de Wordle basadas en el juego original de Josh Wardle
- Diccionarios de palabras en español de uso público
- Comunidad de Kotlin Multiplatform

---

**¡Disfruta jugando Wordle Multijugador! 🎮🎉**
