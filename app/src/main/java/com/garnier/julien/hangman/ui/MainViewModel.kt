package com.garnier.julien.hangman.ui

import androidx.annotation.MainThread
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
                if (gameStatus.currentWordToGuessId != -1L) {
                    allWordToGuess.find { gameStatus.currentWordToGuessId == it.id }
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

    @MainThread
    fun guessLetter(letter: String) {
        val currentGameStatus = getCurrentGameStatus()

        // Do nothing when the game is already over, best way would be disable text input on UI side
        if (currentGameStatus.isGameOver || currentGameStatus.wordToGuess == null)
            return

        // Do nothing when player types a letter already guessed
        if (currentGameStatus.lettersAlreadyGuessed?.contains(letter) == true)
            return

        val word = currentGameStatus.wordToGuess.word

        currentGameStatusMutableLiveData.value =
            if (!word.contains(letter, ignoreCase = true))
                currentGameStatus.copy(numberOfTriesLeft = currentGameStatus.numberOfTriesLeft - 1)
            else {
                val newAlreadyGuessedLettersList =
                    currentGameStatus
                        .lettersAlreadyGuessed
                        ?.mapIndexed { index, shownLetter ->
                            if (word.indexOf(letter) == index) letter
                            else shownLetter
                        }
                        ?: arrayListOf()

                currentGameStatus.copy(lettersAlreadyGuessed = newAlreadyGuessedLettersList)
            }
    }

    fun getCurrentGameStatusLiveData(): LiveData<CurrentGameStatus> = currentGameStatusMutableLiveData

    private fun getCurrentGameStatus() = currentGameStatusMutableLiveData.value ?: CurrentGameStatus()
    data class CurrentGameStatus(
        val wordToGuess: WordToGuess? = null,
        val lettersAlreadyGuessed: List<String>? = null,
        val numberOfGames: Int = -1,
        val numberOfVictories: Int = -1,
        val numberOfTriesLeft: Int = -1,
        val isGameOver: Boolean = false,
    )

    companion object {
        private const val MAX_NUMBER_OF_TRIES = 10
        private const val HIDDEN_LETTER = "_"
    }
}