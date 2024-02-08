package com.garnier.julien.hangman.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WordToGuess(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val word: String,
    val lettersAlreadyGuessed: String?,
    val alreadyGuessed: Boolean,
)