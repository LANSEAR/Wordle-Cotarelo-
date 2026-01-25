package com.cotarelo.wordle.server.game

import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de salas de juego PVP
 */
object RoomManager {
    private val rooms = ConcurrentHashMap<String, GameRoom>()
    private val playerToRoom = ConcurrentHashMap<Int, String>()

    /**
     * Crea una nueva sala
     */
    fun createRoom(
        wordLength: Int,
        maxAttempts: Int,
        rounds: Int,
        difficulty: String,
        creatorId: Int,
        creatorName: String
    ): GameRoom {
        val room = GameRoom(
            wordLength = wordLength,
            maxAttempts = maxAttempts,
            rounds = rounds,
            difficulty = difficulty
        )

        room.addPlayer(creatorId, creatorName)
        rooms[room.roomId] = room
        playerToRoom[creatorId] = room.roomId

        println("🏠 Sala creada: ${room.roomId} por $creatorName (#$creatorId)")
        return room
    }

    /**
     * Lista todas las salas disponibles (no llenas y no iniciadas)
     */
    fun getAvailableRooms(): List<RoomInfo> {
        return rooms.values
            .filter { !it.isFull() && !it.gameStarted }
            .map { room ->
                RoomInfo(
                    roomId = room.roomId,
                    wordLength = room.wordLength,
                    maxAttempts = room.maxAttempts,
                    rounds = room.rounds,
                    difficulty = room.difficulty,
                    playerCount = if (room.player1Id != null) 1 else 0,
                    creatorName = room.player1Name ?: "Unknown"
                )
            }
    }

    /**
     * Une a un jugador a una sala específica
     */
    fun joinRoom(roomId: String, playerId: Int, playerName: String): GameRoom? {
        val room = rooms[roomId] ?: return null

        if (room.isFull() || room.gameStarted) {
            return null
        }

        if (room.addPlayer(playerId, playerName)) {
            playerToRoom[playerId] = roomId

            // Si la sala se llenó, iniciar juego automáticamente
            if (room.isFull()) {
                room.startGame()
            }

            return room
        }

        return null
    }

    /**
     * Obtiene la sala de un jugador
     */
    fun getPlayerRoom(playerId: Int): GameRoom? {
        val roomId = playerToRoom[playerId] ?: return null
        return rooms[roomId]
    }

    /**
     * Remueve a un jugador de su sala
     */
    fun removePlayer(playerId: Int): GameRoom? {
        val roomId = playerToRoom.remove(playerId) ?: return null
        val room = rooms[roomId] ?: return null

        room.removePlayer(playerId)

        // Si la sala quedó vacía, eliminarla
        if (room.isEmpty()) {
            rooms.remove(roomId)
            println("🗑️  Sala $roomId eliminada (vacía)")
        }

        return room
    }

    /**
     * Obtiene el ID del oponente
     */
    fun getOpponentId(playerId: Int): Int? {
        val room = getPlayerRoom(playerId) ?: return null
        return when (playerId) {
            room.player1Id -> room.player2Id
            room.player2Id -> room.player1Id
            else -> null
        }
    }

    /**
     * Limpia salas vacías o abandonadas
     */
    fun cleanup() {
        val emptyRooms = rooms.filterValues { it.isEmpty() }
        emptyRooms.keys.forEach { roomId ->
            rooms.remove(roomId)
            println("🧹 Sala $roomId limpiada")
        }
    }
}

/**
 * Información de sala para el cliente
 */
data class RoomInfo(
    val roomId: String,
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String,
    val playerCount: Int,
    val creatorName: String
)
