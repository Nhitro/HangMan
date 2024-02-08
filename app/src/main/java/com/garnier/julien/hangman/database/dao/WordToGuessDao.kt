package com.garnier.julien.hangman.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.garnier.julien.hangman.database.model.WordToGuess

@Dao
interface WordToGuessDao {
    @Update
    suspend fun updateWordToGuess(wordToGuess: WordToGuess)

    @Query("SELECT * FROM WordToGuess")
    suspend fun getAllWordToGuess(): List<WordToGuess>

}