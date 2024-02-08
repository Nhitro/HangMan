package com.garnier.julien.hangman.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.garnier.julien.hangman.database.model.GameStatus

@Dao
interface GameStatusDao {
    @Update
    suspend fun updateGameStatus(gameStatus: GameStatus)

    @Query("SELECT * FROM GameStatus LIMIT 1")
    suspend fun getCurrentGameStatus(): GameStatus

}