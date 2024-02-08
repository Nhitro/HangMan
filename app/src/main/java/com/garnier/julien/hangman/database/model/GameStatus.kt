package com.garnier.julien.hangman.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameStatus(
    @PrimaryKey val id: Long,
    val numberOfVictories: Int,
    val numberOfGames: Int,
    val currentWordToGuessId: Long,
    val currentTriesLeft: Int,
)