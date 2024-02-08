package com.garnier.julien.hangman.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.garnier.julien.hangman.R
import com.garnier.julien.hangman.databinding.ActivityMainBinding
import com.garnier.julien.hangman.ui.letter.LetterAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding =
            ActivityMainBinding
                .inflate(layoutInflater)
                .also { setContentView(it.root) }

        val adapter = LetterAdapter()

        // Init view
        binding.lettersToGuessRecyclerView.adapter = adapter

        // Init listeners
        binding.textInputGuesser.setEndIconOnClickListener {
            val letter = binding.textInputGuesserEditText.text?.toString()
            if (letter != null)
                mainViewModel.guessLetter(letter)
            else
                Toast
                    .makeText(this, getString(R.string.no_letter_to_guess), Toast.LENGTH_SHORT)
                    .show()
        }

        // Update view according VM state
        mainViewModel
            .getCurrentGameStatusLiveData()
            .observe(this) {
                adapter.submitList(it.lettersAlreadyGuessed)
                binding.victoryNumber.text =
                    resources.getString(R.string.number_of_victories, it.numberOfVictories)
                binding.gamesNumber.text =
                    resources.getString(R.string.number_of_games, it.numberOfGames)
                binding.gameDescription.text =
                    if (it.isGameOver) getString(R.string.game_is_over)
                    else resources.getString(R.string.game_state_description, it.numberOfTriesLeft)
            }
    }
}