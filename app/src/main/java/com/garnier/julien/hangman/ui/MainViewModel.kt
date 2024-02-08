package com.garnier.julien.hangman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnier.julien.hangman.database.model.WordToGuess
import com.garnier.julien.hangman.database.repository.GameStatusRepository
import com.garnier.julien.hangman.database.repository.WordToGuessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gameStatusRepository: GameStatusRepository,
    private val wordToGuessRepository: WordToGuessRepository,
): ViewModel() {

    private val currentGameStatusMutableLiveData = MutableLiveData<CurrentGameStatus>()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val gameStatus = gameStatusRepository.getCurrentGameStatus()
            val allWordToGuess = wordToGuessRepository.getAllWordToGuess()
            val nextWordToGuess: WordToGuess? =
                if (gameStatus.currentWordToGuessId != -1) {
                    allWordToGuess.find { gameStatus.id == it.id }
                }
                else {
                    allWordToGuess
                        .filter { !it.alreadyGuessed }
                        .randomOrNull()
                }
            val isGameOver = nextWordToGuess == null
            val lettersAlreadyGuessed: List<String>? = nextWordToGuess?.let {
                it.lettersAlreadyGuessed?.chunked(1) ?: it.word.map { HIDDEN_LETTER }
            }

            currentGameStatusMutableLiveData.postValue(
                CurrentGameStatus(
                    nextWordToGuess,
                    lettersAlreadyGuessed,
                    gameStatus.numberOfGames,
                    gameStatus.numberOfVictories,
                    gameStatus.currentTriesLeft.takeUnless { it == -1 } ?: MAX_NUMBER_OF_TRIES,
                    isGameOver
                )
            )
        }
    }

    fun getCurrentGameStatusLiveData(): LiveData<CurrentGameStatus> = currentGameStatusMutableLiveData

    data class CurrentGameStatus(
        val wordToGuess: WordToGuess?,
        val lettersAlreadyGuessed: List<String>?,
        val numberOfGames: Int,
        val numberOfVictories: Int,
        val numberOfTriesLeft: Int,
        val isGameOver: Boolean = false,
    )

    companion object {
        private const val MAX_NUMBER_OF_TRIES = 10
        private const val HIDDEN_LETTER = "_"
    }
}