package com.example.snake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun playGame(view: View) {
        val openPage = Intent(this, SnakeActivity::class.java)
        startActivity(openPage)
    }

    fun openSettings(view: View) {
        val openPage = Intent(this, SettingsActivity::class.java)
        startActivity(openPage)
    }

    fun viewHighScores(view: View) {
        val openPage = Intent(this, ViewHighScoresActivity::class.java)
        startActivity(openPage)
    }
}
