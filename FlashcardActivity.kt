package com.example.hskflashcards

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream

// Data class to store flashcard information
data class Flashcard(val character: String, val pinyin: String, val english: String)

class FlashcardActivity : AppCompatActivity() {
    private lateinit var flashcards: List<Flashcard>
    private var currentIndex = 0
    private var showingAnswer = false
    private lateinit var characterView: TextView
    private lateinit var pinyinView: TextView
    private lateinit var englishView: TextView
    private lateinit var progressTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        characterView = findViewById(R.id.characterView)
        pinyinView = findViewById(R.id.pinyinView)
        englishView = findViewById(R.id.englishView)
        progressTextView = findViewById(R.id.progressTextView)

        // Set up the refresh button
        val refreshButton: ImageButton = findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            resetFlashcards()
        }

        // Set up the HSK level selector Spinner
        val hskSpinner: Spinner = findViewById(R.id.hskSpinner)
        hskSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLevel = when (position) {
                    0 -> "hsk1_words"
                    1 -> "hsk2_words"
                    2 -> "hsk3_words"
                    3 -> "hsk4_words"
                    4 -> "hsk5_words"
                    5 -> "hsk6_words"
                    6 -> "radical_words"
                    else -> "hsk1_words"
                }
                loadWordList(selectedLevel)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Set click listeners for left and right half views
        findViewById<View>(R.id.leftHalf).setOnClickListener { moveBackward() }
        findViewById<View>(R.id.rightHalf).setOnClickListener { moveForward() }

        // Load the initial HSK 1 word list
        loadWordList("hsk1_words")
    }

    private fun loadWordList(filename: String) {
        try {
            val resourceId = resources.getIdentifier(filename, "raw", packageName)
            if (resourceId == 0) {
                throw Exception("Resource not found")
            }

            val inputStream: InputStream = resources.openRawResource(resourceId)
            val json = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Flashcard>>() {}.type

            // Parse JSON and handle empty list case
            val loadedList: List<Flashcard> = Gson().fromJson(json, type) ?: emptyList()
            if (loadedList.isEmpty()) {
                throw Exception("No words found in $filename")
            }

            flashcards = loadedList.shuffled()
            currentIndex = 0
            showingAnswer = false
            showFlashcard()
            updateProgress()

        } catch (e: Exception) {
            // Handle the exception by displaying an error message in the flashcard view
            flashcards = emptyList()
            currentIndex = 0
            updateProgress()
            characterView.text = "Error loading words: ${e.message}"
            pinyinView.text = ""
            englishView.text = ""
        }
    }

    private fun showFlashcard() {
        val flashcard = flashcards.getOrNull(currentIndex)
        characterView.text = flashcard?.character ?: "No words available"
        pinyinView.text = if (showingAnswer && flashcard != null) flashcard.pinyin else ""
        englishView.text = if (showingAnswer && flashcard != null) flashcard.english else ""
        updateProgress()
    }

    private fun updateProgress() {
        progressTextView.text = String.format("%02d/%02d", currentIndex + 1, flashcards.size)
    }

    private fun resetFlashcards() {
        flashcards = flashcards.shuffled()
        currentIndex = 0
        showingAnswer = false
        showFlashcard()
    }

    // Move forward in flashcards
    private fun moveForward() {
        if (showingAnswer) {
            currentIndex = (currentIndex + 1) % flashcards.size
            showingAnswer = false
        } else {
            showingAnswer = true
        }
        showFlashcard()
    }

    // Move backward in flashcards
    private fun moveBackward() {
        if (showingAnswer) {
            currentIndex = if (currentIndex - 1 < 0) flashcards.size - 1 else currentIndex - 1
            showingAnswer = false
        } else {
            showingAnswer = true
        }
        showFlashcard()
    }
}