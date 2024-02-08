package com.garnier.julien.hangman.ui.letter

import androidx.recyclerview.widget.DiffUtil

class LetterDiffUtils : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem === newItem

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}