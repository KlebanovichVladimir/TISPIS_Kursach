package com.example.ctt_new_example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChooseCompanyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_company)

        val first_button: Button = findViewById(R.id.button_Savushkin)
        val second_button: Button = findViewById(R.id.button_test_Savushkin)

        // Устанавливаем обработчик для кнопки "ОАО ''Савушкин продукт''"
        first_button.setOnClickListener {
            openCompanyDetails("ОАО ''Савушкин продукт''")
        }

        // Устанавливаем обработчик для кнопки "Тестовый партнер для Савушкин Продукт"
        second_button.setOnClickListener {
            openCompanyDetails("Тестовый партнер для Савушкин Продукт")
        }
    }

    private fun openCompanyDetails(companyName: String) {
        val intent = Intent(this, ChooseTimeActivity::class.java).apply {
            putExtra("company_name", companyName)
        }
        startActivity(intent)
    }
}
