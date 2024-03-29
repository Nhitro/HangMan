package com.garnier.julien.hangman.ui

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.garnier.julien.hangman.database.model.GameStatus
import com.garnier.julien.hangman.database.model.WordToGuess
import com.garnier.julien.hangman.database.repository.GameStatusRepository
import com.garnier.julien.hangman.database.repository.WordToGuessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            // Initialize state according database data
            val gameStatus = gameStatusRepository.getCurrentGameStatus()
            val allWordToGuess = wordToGuessRepository.getAllWordToGuess()

            withContext(Dispatchers.Main) {
                initGameScreenState(gameStatus, allWordToGuess)
            }

            // Start a flow on current game status in charge of keeping up to date database according player actions
            gameScreenStateMutableLiveData
                .asFlow()
                .flowOn(Dispatchers.IO)
                .filterNotNull()
                .collect { gameScreenState ->
                    val currentGameStatus = gameScreenState.toGameStatus(gameStatus.id)

                    // Only update database if model changed according last database value
                    if (currentGameStatus != gameStatus) {
                        gameStatusRepository.updateGameStatus(currentGameStatus)
                        lastGameStatus = currentGameStatus
                    }

                    gameScreenState.wordToGuess
                        ?.takeIf { it.id == lastWordToGuess?.id && it != lastWordToGuess }
                        ?.let {
                            wordToGuessRepository.updateWordToGuess(it)
                            lastWordToGuess = it
                        }
                }
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
                            if (!word[index].toString().equals(letter, ignoreCase = true)) shownLetter
                            else
                                if (index == 0) letter.uppercase()
                                else letter.lowercase()
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
                    numberOfVictories = if (isGuessOver) gameScreenState.numberOfVictories + 1 else gameScreenState.numberOfVictories,
                )
            }
    }

    @MainThread
    fun startNewGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextWordToGuess =
                wordToGuessRepository
                    .getAllWordToGuess()
                    .filter { !it.alreadyGuessed }
                    .randomOrNull()

            withContext(Dispatchers.Main) {
                val gameScreenState = getCurrentGameScreenState()

                gameScreenStateMutableLiveData.value =
                    if (nextWordToGuess == null)
                        gameScreenState.copy(
                            wordToGuess = null,
                            lettersAlreadyGuessed = null,
                            numberOfGames = gameScreenState.numberOfGames + 1,
                            isGameOver = true,
                            showWinnerAlert = false,
                            showLooserAlert = false,
                        )
                    else {
                        lastWordToGuess = nextWordToGuess
                        gameScreenState.copy(
                            wordToGuess = nextWordToGuess,
                            lettersAlreadyGuessed = nextWordToGuess.word.map { HIDDEN_LETTER },
                            numberOfGames = gameScreenState.numberOfGames + 1,
                            numberOfTriesLeft = MAX_NUMBER_OF_TRIES,
                            showWinnerAlert = false,
                            showLooserAlert = false,
                        )
                    }
            }
        }
    }

    @MainThread
    fun resetGameStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val resetWordToGuestList =
                wordToGuessRepository
                    .getAllWordToGuess()
                    .map { it.copy(lettersAlreadyGuessed = null, alreadyGuessed = false) }

            val resetGameStatus = lastGameStatus.copy(
                numberOfGames = 0,
                numberOfVictories = 0,
                currentWordToGuessId = -1,
                currentTriesLeft = -1,
            )

            wordToGuessRepository.updateWordToGuess(resetWordToGuestList)
            gameStatusRepository.updateGameStatus(resetGameStatus)
            withContext(Dispatchers.Main) {
                initGameScreenState(resetGameStatus, resetWordToGuestList)
            }
        }
    }

    fun getGameScreenStateLiveData(): LiveData<GameScreenState> = gameScreenStateMutableLiveData

    private fun getCurrentGameScreenState() = gameScreenStateMutableLiveData.value ?: GameScreenState()

    @MainThread
    private fun initGameScreenState(
        gameStatus: GameStatus,
        allWordToGuess: List<WordToGuess>
    ) {
        val nextWordToGuess: WordToGuess? =
            if (gameStatus.currentWordToGuessId != -1L) {
                allWordToGuess.find { gameStatus.currentWordToGuessId == it.id }
            } else {
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

        gameScreenStateMutableLiveData.value =
            GameScreenState(
                nextWordToGuess,
                lettersAlreadyGuessed,
                gameStatus.numberOfGames,
                gameStatus.numberOfVictories,
                gameStatus.currentTriesLeft.takeUnless { it == -1 } ?: MAX_NUMBER_OF_TRIES,
                isGameOver
            )
    }
    data class GameScreenState(
        val wordToGuess: WordToGuess? = null,
        val lettersAlreadyGuessed: List<String>? = null,
        val numberOfGames: Int = -1,
        val numberOfVictories: Int = -1,
        val numberOfTriesLeft: Int = -1,
        val isGameOver: Boolean = false,
        val showWinnerAlert: Boolean = false,
        val showLooserAlert: Boolean = false,
    ) {
        fun toGameStatus(id: Long) = GameStatus(
            id,
            numberOfVictories,
            numberOfGames,
            wordToGuess?.id ?: -1L,
            numberOfTriesLeft
        )
    }

    companion object {
        private const val MAX_NUMBER_OF_TRIES = 10
        private const val HIDDEN_LETTER = "_"
    }
}