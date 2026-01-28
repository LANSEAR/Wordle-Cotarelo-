package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.network.PVPServerConnection
import com.cotarelo.wordle.client.network.PVPServerResponse
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.shared.network.RoomInfoDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LobbyScreen(
    settings: AppSettings,
    onBackToMenu: () -> Unit,
    onGameStart: (String, String, Int, Int, Int, String, PVPServerConnection) -> Unit  // roomId, opponentName, wordLength, maxAttempts, rounds, difficulty, connection
) {
    val scope = rememberCoroutineScope()
    val connection = remember { PVPServerConnection() }

    var isConnected by remember { mutableStateOf(false) }
    var rooms by remember { mutableStateOf<List<RoomInfoDto>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf(settings.playerName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var waitingForOpponent by remember { mutableStateOf(false) }
    var myRoomId by remember { mutableStateOf<String?>(null) }

    // Configuración de la sala (se obtiene al crear o unirse)
    var roomWordLength by remember { mutableStateOf(settings.wordLength) }
    var roomMaxAttempts by remember { mutableStateOf(settings.maxAttempts) }
    var roomRounds by remember { mutableStateOf(settings.roundsBestOf) }
    var roomDifficulty by remember { mutableStateOf(settings.difficulty.toString()) }

    // Conectar al servidor
    LaunchedEffect(Unit) {
        if (connection.connect()) {
            isConnected = true
            // Actualizar lista cada 3 segundos
            while (isConnected) {
                connection.listRooms()
                delay(3000)
            }
        } else {
            errorMessage = "No se pudo conectar al servidor"
        }
    }

    // Escuchar respuestas del servidor
    LaunchedEffect(Unit) {
        connection.serverResponses.collect { response ->
            when (response) {
                is PVPServerResponse.RoomList -> {
                    rooms = response.data.rooms
                }
                is PVPServerResponse.RoomCreated -> {
                    myRoomId = response.data.roomId
                    waitingForOpponent = true
                    // Guardar configuración de la sala creada
                    roomWordLength = response.data.wordLength
                    roomMaxAttempts = response.data.maxAttempts
                    roomRounds = response.data.rounds
                    roomDifficulty = response.data.difficulty
                }
                is PVPServerResponse.RoomJoined -> {
                    // Guardar configuración de la sala a la que nos unimos
                    roomWordLength = response.data.wordLength
                    roomMaxAttempts = response.data.maxAttempts
                    roomRounds = response.data.rounds
                    roomDifficulty = response.data.difficulty

                    if (response.data.opponentName != "Esperando...") {
                        // Hay oponente, esperar inicio de juego
                        waitingForOpponent = true
                    }
                }
                is PVPServerResponse.GameStartedPVP -> {
                    waitingForOpponent = false
                    onGameStart(
                        response.data.roomId,
                        response.data.opponentName,
                        roomWordLength,
                        roomMaxAttempts,
                        roomRounds,
                        roomDifficulty,
                        connection
                    )
                }
                is PVPServerResponse.Error -> {
                    errorMessage = response.data.message
                    waitingForOpponent = false
                }
                else -> {}
            }
        }
    }

    // Desconectar al salir
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                connection.disconnect()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                "Lobby Multijugador",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Crear Sala")
                }

                Button(
                    onClick = { scope.launch { connection.listRooms() } },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Actualizar")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Mensaje de error
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    backgroundColor = MaterialTheme.colors.error
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colors.onError
                    )
                }
            }

            // Lista de salas
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                elevation = 4.dp
            ) {
                if (rooms.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay salas disponibles\nCrea una nueva sala para jugar",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    ) {
                        items(rooms) { room ->
                            RoomItem(
                                room = room,
                                onJoin = {
                                    scope.launch {
                                        connection.joinRoom(room.roomId, playerName)
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón volver
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Volver al Menú")
            }
        }

        // Diálogo crear sala
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Crear Sala") },
                text = {
                    Column {
                        Text("Tu nombre:")
                        TextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Configuración:")
                        Text("• Longitud: ${settings.wordLength} letras", style = MaterialTheme.typography.body2)
                        Text("• Intentos: ${settings.maxAttempts}", style = MaterialTheme.typography.body2)
                        Text("• Rondas: ${settings.roundsBestOf}", style = MaterialTheme.typography.body2)
                        Text("• Dificultad: ${settings.difficulty}", style = MaterialTheme.typography.body2)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            connection.createRoom(
                                wordLength = settings.wordLength,
                                maxAttempts = settings.maxAttempts,
                                rounds = settings.roundsBestOf,
                                difficulty = settings.difficulty.toString(),
                                playerName = playerName
                            )
                            showCreateDialog = false
                        }
                    }) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo esperando oponente
        if (waitingForOpponent) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Esperando Oponente...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Esperando que otro jugador se una...")
                        myRoomId?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("ID de Sala: $it", style = MaterialTheme.typography.caption)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            connection.leaveRoom()
                        }
                        waitingForOpponent = false
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun RoomItem(
    room: RoomInfoDto,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Sala de ${room.creatorName}",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${room.wordLength} letras • ${room.maxAttempts} intentos • Best of ${room.rounds}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Dificultad: ${room.difficulty}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            Button(onClick = onJoin) {
                Text("Unirse")
            }
        }
    }
}
