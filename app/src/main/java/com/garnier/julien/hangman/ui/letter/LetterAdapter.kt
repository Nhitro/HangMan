package com.garnier.julien.hangman.ui.letter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.garnier.julien.hangman.R

class LetterAdapter : ListAdapter<String, LetterViewHolder>(LetterDiffUtils()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LetterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.letter_cell, parent, false)
        )

    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) =
        holder.updateContent(getItem(position))
}