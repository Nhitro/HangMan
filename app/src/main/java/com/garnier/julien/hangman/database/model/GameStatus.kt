package com.garnier.julien.hangman.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameStatus(
    @PrimaryKey val id: Int,
    val numberOfVictories: Int,
    val numberOfGames: Int,
    val currentWordToGuessId: Int,
    val currentTriesLeft: Int,
)