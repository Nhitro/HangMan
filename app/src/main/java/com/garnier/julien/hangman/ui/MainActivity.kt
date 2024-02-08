package com.garnier.julien.hangman.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.garnier.julien.hangman.R
import com.garnier.julien.hangman.databinding.ActivityMainBinding
import com.garnier.julien.hangman.ui.letter.LetterAdapter

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

        mainViewModel
            .getCurrentGameStatusLiveData()
            .observe(this) {
                // Update view according VM state
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