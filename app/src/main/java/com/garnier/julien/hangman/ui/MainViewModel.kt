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

    private val gameScreenStateMutableLiveData = MutableLiveData<GameScreenState>()

    private lateinit var lastGameStatus: GameStatus
    private var lastWordToGuess: WordToGuess? = null
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

            // Store init states into local variables for comparison later
            lastGameStatus = gameStatus
            lastWordToGuess = nextWordToGuess

            gameScreenStateMutableLiveData.postValue(
                GameScreenState(
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
        val gameScreenState = getCurrentGameScreenState()
        val wordToGuess = gameScreenState.wordToGuess

        // Do nothing when the game is already over, best way would be disable text input on UI side
        if (gameScreenState.isGameOver || wordToGuess == null)
            return

        // Do nothing when player types a letter already guessed
        if (gameScreenState.lettersAlreadyGuessed?.contains(letter) == true)
            return

        val word = wordToGuess.word

        gameScreenStateMutableLiveData.value =
            if (!word.contains(letter, ignoreCase = true)) {
                val newNumberOfTries = gameScreenState.numberOfTriesLeft - 1
                val isGuessOver = newNumberOfTries == 0

                gameScreenState.copy(
                    wordToGuess = wordToGuess.copy(alreadyGuessed = isGuessOver),
                    numberOfTriesLeft = newNumberOfTries,
                    showLooserAlert = isGuessOver,
                )
            } else {
                val newAlreadyGuessedLettersList =
                    gameScreenState
                        .lettersAlreadyGuessed
                        ?.mapIndexed { index, shownLetter ->
                            if (word[index].toString().equals(letter, ignoreCase = true)) letter
                            else shownLetter
                        }
                        ?: arrayListOf()

                val isGuessOver = !newAlreadyGuessedLettersList.contains(HIDDEN_LETTER)

                gameScreenState.copy(
                    wordToGuess = wordToGuess.copy(
                        lettersAlreadyGuessed = newAlreadyGuessedLettersList.joinToString(""),
                        alreadyGuessed = isGuessOver
                    ),
                    lettersAlreadyGuessed = newAlreadyGuessedLettersList,
                    showWinnerAlert = isGuessOver,
                )
            }
    }

    fun getGameScreenStateLiveData(): LiveData<GameScreenState> = gameScreenStateMutableLiveData

    private fun getCurrentGameScreenState() = gameScreenStateMutableLiveData.value ?: GameScreenState()

    data class GameScreenState(
        val wordToGuess: WordToGuess? = null,
        val lettersAlreadyGuessed: List<String>? = null,
        val numberOfGames: Int = -1,
        val numberOfVictories: Int = -1,
        val numberOfTriesLeft: Int = -1,
        val isGameOver: Boolean = false,
        val showWinnerAlert: Boolean = false,
        val showLooserAlert: Boolean = false,
    )

    companion object {
        private const val MAX_NUMBER_OF_TRIES = 10
        private const val HIDDEN_LETTER = "_"
    }
}