package com.example.snake

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        readFileForConstants()
    }

    private fun readFileForConstants() {
        if(!textFileExists(settingsFile))
            updateSettingsFileWithStartingValues()
        updateScreenValues()
    }

    fun saveButtonPressed(view: View) {
        // 0 represents false; 1 represents true
        // sets boolean variables based on switch views
        var hidePauseButton = 0
        var startGameOnPause = 0
        var showGridSquares = 0

        if(hide_pause_button_switch.isChecked) hidePauseButton = 1
        if(start_game_on_pause_switch.isChecked) startGameOnPause = 1
        if(show_grid_squares_switch.isChecked) showGridSquares = 1

        // sets variables to submitted values
        val lengthOfGridString = length_of_grid_edit_text.text.toString()
        val startingSnakeLengthString = starting_snake_length_edit_text.text.toString()
        val snakeSpeedFpsString = snake_speed_fps_edit_text.text.toString()

        // checks if submitted text is a positive integer
        if(lengthOfGridString == "" || startingSnakeLengthString == ""
            || snakeSpeedFpsString == "" || stringIsNotPositiveInteger(lengthOfGridString) ||
            stringIsNotPositiveInteger(startingSnakeLengthString) ||
            stringIsNotPositiveInteger(snakeSpeedFpsString)) {
            Toast.makeText(this,
                getString(R.string.wrongFormatMsg),
                Toast.LENGTH_LONG).show()
        } else {
            val lengthOfGrid = lengthOfGridString.toInt()
            val startingSnakeLength = startingSnakeLengthString.toInt()
            val snakeSpeedFps = snakeSpeedFpsString.toInt()

            updateSettingsIfEntriesAreValid(lengthOfGrid, startingSnakeLength, snakeSpeedFps,
                hidePauseButton, startGameOnPause, showGridSquares)
        }
    }

    private fun stringIsNotPositiveInteger(str: String): Boolean {
        for(ch in str) {
            var charIsInteger = false
            for (i in 0..9) {
                if (ch.toString() == i.toString()) {
                    charIsInteger = true
                    break
                }
            }
            if(!charIsInteger) return true
        }
        return false
    }

    private fun updateSettingsIfEntriesAreValid(lengthOfGrid: Int, startingSnakeLength: Int,
                                                snakeSpeedFps: Int, hidePauseButton: Int,
                                                startGameOnPause: Int, showGridSquares: Int) {
        when {
            lengthOfGrid < 10 ->
                Toast.makeText(
                    this,
                    getString(R.string.gridLengthMinMsg),
                    Toast.LENGTH_LONG
                ).show()
            startingSnakeLength < 3 -> Toast.makeText(
                this,
                getString(R.string.snakeLengthMinMsg),
                Toast.LENGTH_LONG
            ).show()
            snakeSpeedFps < 1 -> Toast.makeText(
                this,
                getString(R.string.snakeSpeedMinMsg),
                Toast.LENGTH_LONG
            ).show()
            lengthOfGrid > 100 -> Toast.makeText(
                this,
                getString(R.string.gridLengthMaxMsg),
                Toast.LENGTH_LONG
            ).show()
            startingSnakeLength > lengthOfGrid - 5 -> Toast.makeText(
                this,
                getString(R.string.snakeLengthMaxMsg, lengthOfGrid - 5),
                Toast.LENGTH_LONG
            ).show()
            snakeSpeedFps > 50 -> Toast.makeText(
                this,
                getString(R.string.snakeSpeedMaxMsg),
                Toast.LENGTH_LONG
            ).show()
            else -> {
                val updatedSettingsFileContents = "$hidePauseButton\n$startGameOnPause\n" +
                        "$showGridSquares\n$lengthOfGrid\n$startingSnakeLength\n$snakeSpeedFps"
                updateOrCreateTextFileWithString(settingsFile, updatedSettingsFileContents)
                finish()
            }
        }
    }

    fun restoreDefaultSettings(view: View) {
        updateSettingsFileWithStartingValues()
        updateScreenValues()
    }

    private fun updateSettingsFileWithStartingValues() {
        val startingTextFileContents = "$startingHidePauseButtonInt\n$startingStartGameOnPauseInt\n" +
                "$startingShowGridSquaresInt\n$startingNGridSquaresPerRow\n$startingSnakeStartLength\n$startingFps"
        updateOrCreateTextFileWithString(settingsFile, startingTextFileContents)
    }

    private fun updateScreenValues() {
        val textFileContents = getOrCreateTextFileAsArrayList(settingsFile)

        hide_pause_button_switch.isChecked = false
        start_game_on_pause_switch.isChecked = false
        show_grid_squares_switch.isChecked = false

        if(textFileContents[0].toInt() == 1) hide_pause_button_switch.isChecked = true
        if(textFileContents[1].toInt() == 1) start_game_on_pause_switch.isChecked = true
        if(textFileContents[2].toInt() == 1) show_grid_squares_switch.isChecked = true

        length_of_grid_edit_text.setText(textFileContents[3])
        starting_snake_length_edit_text.setText(textFileContents[4])
        snake_speed_fps_edit_text.setText(textFileContents[5])
    }
}
