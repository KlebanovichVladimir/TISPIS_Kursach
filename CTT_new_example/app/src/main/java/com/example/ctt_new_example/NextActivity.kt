package com.example.ctt_new_example

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_time)

        val companyName = intent.getStringExtra("company_name") ?: ""
        val uuid = intent.getStringExtra("uuid") ?: ""

        val startDateInput: EditText = findViewById(R.id.start_date)
        val endDateInput: EditText = findViewById(R.id.end_date)
        val proceedButton: Button = findViewById(R.id.button_ready)

        proceedButton.setOnClickListener {
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                Toast.makeText(this, "UUID: $uuid\nКомпания: $companyName", Toast.LENGTH_LONG).show()
                // Здесь можно добавить переход к следующему шагу
            } else {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
