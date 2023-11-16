package com.example.snake

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_snake.*

class SnakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)
        snakecanvas.passActivity(this)
        readFileForConstants()
    }

    private fun readFileForConstants() {

        if(!textFileExists(settingsFile)) {
            val startingTextFileContents = "$startingHidePauseButtonInt\n$startingStartGameOnPauseInt\n" +
                    "$startingShowGridSquaresInt\n$startingNGridSquaresPerRow\n$startingSnakeStartLength\n$startingFps"
            updateOrCreateTextFileWithString(settingsFile, startingTextFileContents)
        }

        // settings file as array list
        val textFileContents = getOrCreateTextFileAsArrayList(settingsFile)

        // sets boolean to correct value
        var hidePauseButton = false
        var startGameOnPause = false
        var showGridSquares = false
        if(textFileContents[0].toInt() == 1) hidePauseButton = true
        if(textFileContents[1].toInt() == 1) startGameOnPause = true
        if(textFileContents[2].toInt() == 1) showGridSquares = true

        // sets int constants
        val nGridSquaresPerRow = textFileContents[3].toInt()
        val snakeStartLength = textFileContents[4].toInt()
        val fps = textFileContents[5].toInt()

        // updates constants on SnakeCanvas
        snakecanvas.updateConstants(hidePauseButton, startGameOnPause, showGridSquares,
            nGridSquaresPerRow, snakeStartLength, fps)
    }

    @SuppressLint("InflateParams")
    fun openGameOverDialog(score: Int) {
        val mView = layoutInflater.inflate(R.layout.activity_game_over_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setCancelable(false)
        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        setupViewFunctions(score, mView, dialog)
        dialog.show()
    }

    private fun setupViewFunctions(score: Int, mView: View, dialog: AlertDialog) {
        // text views
        val scoreText = mView.findViewById<TextView>(R.id.score)
        val nameField = mView.findViewById<EditText>(R.id.player_name)
        val scoreSubmittedMessage = mView.findViewById<TextView>(R.id.score_submitted_message)

        // buttons
        val exitButton = mView.findViewById<Button>(R.id.exit_b)
        val retryButton = mView.findViewById<Button>(R.id.retry_b)
        val submitButton = mView.findViewById<Button>(R.id.submit_b)

        // update score text view
        scoreText.text = score.toString()

        // exits activity
        exitButton.setOnClickListener {
            finish()
            dialog.cancel()
        }

        // resets snake game
        retryButton.setOnClickListener {
            snakecanvas.retry()
            dialog.cancel()
        }

        // adds score to high scores file
        submitButton.setOnClickListener {
            val playerName = nameField.text.toString()
            if(playerName == "") {
                Toast.makeText(
                    this,
                    "'Player Name' field cannot be blank",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                updateHighScoreFile(nameField.text.toString(), score)
                submitButton.isEnabled = false
                nameField.isVisible = false
                val scoreSubmittedMessageText = "Thanks $playerName!\nYour score has been submitted."
                scoreSubmittedMessage.text = scoreSubmittedMessageText
                scoreSubmittedMessage.isVisible = true
            }
        }
    }

    private fun updateHighScoreFile(playerName: String, newScore: Int) {
        val highScoresList = getOrCreateTextFileAsArrayList(highScoresFile)
        val newEntry = "$playerName$nameScoreSplitterString$newScore"

        var indexOfEntry = 0
        for(line in highScoresList) {
            val elements = line.split(nameScoreSplitterString)
            val score = elements[elements.lastIndex]
            if(newScore > score.toInt()) {
                highScoresList.add(indexOfEntry, newEntry)
                break
            }
            if(indexOfEntry == highScoresList.lastIndex)
                highScoresList.add(newEntry)
            indexOfEntry++
        }

        if(highScoresList.size == 0) highScoresList.add(newEntry)
        updateOrCreateTextFileWithArrayList(highScoresFile, highScoresList)
    }
}
