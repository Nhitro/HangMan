package com.garnier.julien.hangman.ui.letter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.garnier.julien.hangman.databinding.LetterCellBinding

class LetterViewHolder(view: View): ViewHolder(view) {

    private val binding = LetterCellBinding.bind(view)

    fun updateContent(letter: String) {
        binding.letterToGuess.text = letter
    }

}