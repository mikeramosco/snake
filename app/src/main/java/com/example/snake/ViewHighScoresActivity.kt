package com.example.snake

import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_view_high_scores.*
import kotlinx.android.synthetic.main.score_entry.view.*
import java.text.NumberFormat
import java.util.*

class ViewHighScoresActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_high_scores)
        displayHighScores()
    }

    private fun displayHighScores() {
        if(!textFileExists(highScoresFile))
            no_scores_message.isVisible = true
        else {
            displayScore("PLAYER NAMES:", "PLAYER SCORES:", true)
            val highScoresList = getOrCreateTextFileAsArrayList(highScoresFile)
            var rank = 1

            // Gets locale (english, french, etc.)
            val loc = Locale.getDefault()
            for(scoreElement in highScoresList) {
                val elements = scoreElement.split(nameScoreSplitterString)
                val name = "${rank++}. ${elements[0]}"

                // en-US Int: 3000 is converted to 3,000
                // fr    Int: 3.01 is converted to 3,01 (french)
                val score = NumberFormat.getInstance(loc).format(elements[elements.lastIndex].toInt())
                displayScore(name, score, false)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun displayScore(name: String, score: String, entryIsLabel: Boolean) {
        val entry = layoutInflater.inflate(R.layout.score_entry, null)
        entry.name_text.text = name
        entry.score_text.text = score
        if(entryIsLabel) updateLabelSettings(entry)
        high_scores_layout.addView(entry)
    }

    private fun updateLabelSettings(entry: View) {
        entry.offset_left.text = "   "
        entry.offset_right.text = "   "
        entry.name_text.textSize = 18f
        entry.score_text.textSize = 18f
        entry.name_text.typeface = Typeface.DEFAULT_BOLD
        entry.score_text.typeface = Typeface.DEFAULT_BOLD
    }
}
