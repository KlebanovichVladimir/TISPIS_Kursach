package com.example.ctt_new_example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        // Получаем сообщение об ошибке из Intent
        val errorMessage = intent.getStringExtra("error_message")

        // Отображаем сообщение об ошибке в TextView
        val errorTextView: TextView = findViewById(R.id.error_message_text_view)
        errorTextView.text = errorMessage ?: "Неизвестная ошибка"
    }
}