package com.example.geoquiz

import android.app.Activity
import android.app.sdksandbox.RequestSurfacePackageException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var  cheatButton: Button

    private var enabledIndex = emptyArray<Int>()
    private var trueIndexCount: Double = 0.0

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this)[QuizViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatButton = findViewById(R.id.cheat_button)

        questionTextView.setOnClickListener{view: View ->
            quizViewModel.moveToNext()
            updateQuestion()
        }

        trueButton.setOnClickListener {view: View ->
            if(quizViewModel.questionBank[quizViewModel.currentIndex].usedCheat==false) {
                checkAnswer(true)
                procentOfCorrect(true)
            }
            else{
                Toast.makeText(this, R.string.judgment_toast, Toast.LENGTH_SHORT).show()
            }
            buttonIsPressed(quizViewModel.currentIndex)
        }
        falseButton.setOnClickListener{view: View ->
            if(quizViewModel.questionBank[quizViewModel.currentIndex].usedCheat==false) {
                checkAnswer(false)
                procentOfCorrect(false)
            }
            else{
                Toast.makeText(this, R.string.judgment_toast, Toast.LENGTH_SHORT).show()
            }
            buttonIsPressed(quizViewModel.currentIndex)
        }

        nextButton.setOnClickListener{
            quizViewModel.moveToNext()
            updateQuestion()
        }

        prevButton.setOnClickListener{
            quizViewModel.moveToPrev()
            updateQuestion()
        }

        cheatButton.setOnClickListener{
            // Начало CheatActivity
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.questionBank[quizViewModel.currentIndex].usedCheat = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
        checkIsEnabled(quizViewModel.currentIndex)
    }

    private fun checkAnswer(userAnswer: Boolean){
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this,messageResId,Toast.LENGTH_SHORT).show()
    }

    private fun buttonIsPressed(currentIndex: Int){
        enabledIndex += currentIndex
        trueButton.isEnabled = false
        falseButton.isEnabled = false
    }

    private fun checkIsEnabled(currentIndex: Int){
        if (currentIndex in enabledIndex){
            trueButton.isEnabled = false
            falseButton.isEnabled = false
        } else {
            trueButton.isEnabled = true
            falseButton.isEnabled = true
        }
    }

    private fun procentOfCorrect(userAnswer: Boolean){
        val correctAnswer = quizViewModel.currentQuestionAnswer
        if (userAnswer == correctAnswer){
            trueIndexCount += 1
        }
        if (enabledIndex.size == quizViewModel.questionBank.size){
            val messageResId = ((trueIndexCount/quizViewModel.questionBank.size)*100)
            val roundedMessageResId = String.format("%.2f",messageResId)
            Toast.makeText(this, "$roundedMessageResId%",Toast.LENGTH_LONG).show()
        }

    }
}
