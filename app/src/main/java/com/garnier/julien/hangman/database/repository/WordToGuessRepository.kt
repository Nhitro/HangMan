package com.garnier.julien.hangman.database.repository

import com.garnier.julien.hangman.database.dao.WordToGuessDao
import com.garnier.julien.hangman.database.model.WordToGuess

class WordToGuessRepository(private val wordToGuessDao: WordToGuessDao) {
    suspend fun updateWordToGuess(wordToGuess: WordToGuess) = updateWordToGuess(listOf(wordToGuess))
    suspend fun updateWordToGuess(wordToGuess: List<WordToGuess>) = wordToGuessDao.updateWordToGuess(wordToGuess)
    suspend fun getAllWordToGuess() = wordToGuessDao.getAllWordToGuess()
}