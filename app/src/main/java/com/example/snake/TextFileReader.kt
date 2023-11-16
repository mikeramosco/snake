package com.example.snake

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList

fun Activity.textFileExists(filename: String) : Boolean {
    val file = getFileStreamPath(filename)
    if(file != null && file.exists()) return true
    return false
}

fun Activity.getOrCreateTextFileAsString(filename: String) : String {

    // if file exists, file contents are returned as String
    val file = getFileStreamPath(filename)
    if(file != null && file.exists()) {
        val scan = Scanner(openFileInput(filename))
        var contents = ""
        while (scan.hasNextLine()) {
            if(contents != "") contents += "\n"
            val line = scan.nextLine()
            contents += line
        }
        return contents
    }

    // if file does not exists, file is created with an empty line which is returned
    val out = PrintStream(openFileOutput(filename, AppCompatActivity.MODE_PRIVATE))
    out.println("")
    out.close()
    return ""
}

fun Activity.getOrCreateTextFileAsArrayList(filename: String) : ArrayList<String> {
    val contents = ArrayList<String>()

    // if file exists, file contents are returned as an ArrayList
    val file = getFileStreamPath(filename)
    if(file != null && file.exists()) {
        val scan = Scanner(openFileInput(filename))
        while (scan.hasNextLine()) {
            val line = scan.nextLine()
            contents.add(line)
        }
        return contents
    }

    // if file does not exists, file is created with an empty line
    // returns an empty ArrayList
    val out = PrintStream(openFileOutput(filename, AppCompatActivity.MODE_PRIVATE))
    out.println("")
    out.close()
    return contents
}

fun Activity.updateOrCreateTextFileWithString(filename: String, contents: String) {
    val out = PrintStream(openFileOutput(filename, AppCompatActivity.MODE_PRIVATE))
    out.println(contents)
    out.close()
}

fun Activity.updateOrCreateTextFileWithArrayList(filename: String, contents: ArrayList<String>) {
    val out = PrintStream(openFileOutput(filename, AppCompatActivity.MODE_PRIVATE))
    for(item in contents) out.println(item)
    out.close()
}