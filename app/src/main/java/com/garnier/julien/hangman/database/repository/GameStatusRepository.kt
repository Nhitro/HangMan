package com.garnier.julien.hangman.database.repository

import com.garnier.julien.hangman.database.dao.GameStatusDao
import com.garnier.julien.hangman.database.model.GameStatus

class GameStatusRepository(private val gameStatusDao: GameStatusDao) {
    suspend fun updateGameStatus(gameStatus: GameStatus) = gameStatusDao.updateGameStatus(gameStatus)

    suspend fun getCurrentGameStatus() = gameStatusDao.getCurrentGameStatus()
}