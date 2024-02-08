package com.garnier.julien.hangman.di

import android.app.Application
import androidx.room.Room
import com.garnier.julien.hangman.database.HangManDatabase
import com.garnier.julien.hangman.database.repository.GameStatusRepository
import com.garnier.julien.hangman.database.repository.WordToGuessRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideHangManDatabase(application: Application): HangManDatabase {
        return Room
            .databaseBuilder(application, HangManDatabase::class.java, "hangman_database.db")
            .createFromAsset("hangman_database.db")
            .build()
    }

    @Provides
    fun provideGameStatusRepository(hangManDatabase: HangManDatabase) = GameStatusRepository(hangManDatabase.gameStatusDao())

    @Provides
    fun provideWordToGuessRepository(hangManDatabase: HangManDatabase) = WordToGuessRepository(hangManDatabase.wordToGuessDao())

}