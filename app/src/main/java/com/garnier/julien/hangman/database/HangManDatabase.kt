package com.garnier.julien.hangman.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.garnier.julien.hangman.database.dao.GameStatusDao
import com.garnier.julien.hangman.database.dao.WordToGuessDao
import com.garnier.julien.hangman.database.model.GameStatus
import com.garnier.julien.hangman.database.model.WordToGuess

@Database(entities = [GameStatus::class, WordToGuess::class], version = 1, exportSchema = false)
abstract class HangManDatabase : RoomDatabase() {
    abstract fun gameStatusDao(): GameStatusDao
    abstract fun wordToGuessDao(): WordToGuessDao
}
