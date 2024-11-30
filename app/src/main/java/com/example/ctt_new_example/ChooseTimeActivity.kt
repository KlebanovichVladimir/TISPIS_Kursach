package com.example.ctt_new_example

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChooseTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_time)

        val companyNameTextView: TextView = findViewById(R.id.company_name_text_view)
        val startDateTextView: TextView = findViewById(R.id.start_date)
        val endDateTextView: TextView = findViewById(R.id.end_date)
        val ewaybillListTextView: TextView = findViewById(R.id.ewaybill_list_text_view)
        val ewaybillListOutput: LinearLayout = findViewById(R.id.ewaybill_buttons_layout)
        val button: Button = findViewById(R.id.button_ready)

        // Получаем переданное название компании
        val companyName = intent.getStringExtra("company_name") ?: "Неизвестная компания"
        companyNameTextView.text = companyName

        // Устанавливаем обработчик выбора даты начала
        startDateTextView.setOnClickListener {
            showDatePickerDialog { date ->
                startDateTextView.text = date
            }
        }

        // Устанавливаем обработчик выбора даты конца
        endDateTextView.setOnClickListener {
            showDatePickerDialog { date ->
                endDateTextView.text = date
            }
        }

        // Устанавливаем обработчик кнопки
        button.setOnClickListener {
            val startDate = startDateTextView.text.toString()
            val endDate = endDateTextView.text.toString()

            // Проверяем, что даты заполнены
            if (startDate.isEmpty() || endDate.isEmpty() ||
                startDate == "Выберите дату начала" || endDate == "Выберите дату конца") {
                Toast.makeText(this, "Введите обе даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверяем, что дата начала не позже даты конца
            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            try {
                val start = inputFormat.parse(startDate)
                val end = inputFormat.parse(endDate)

                if (start != null && end != null && start.after(end)) {
                    Toast.makeText(this, "Дата начала не может быть позже даты конца", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка в формате даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Скрываем список накладных перед запросом
            ewaybillListTextView.visibility = TextView.INVISIBLE
            ewaybillListOutput.removeAllViews()

            // Выполняем запрос в фоновом потоке
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uuid = authenticate()
                    if (uuid != null) {
                        val ewaybills = getEwaybillsList(uuid, startDate, endDate, companyName)
                        // Отображаем результат на экране
                        withContext(Dispatchers.Main) {
                            ewaybillListOutput.removeAllViews() // Полная очистка перед обновлением
                            if (ewaybills.isNotEmpty()) {
                                ewaybillListTextView.visibility = TextView.VISIBLE
                                ewaybills.forEach { id ->
                                    val button = Button(this@ChooseTimeActivity)

                                    val layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        120 // Высота кнопки в пикселях
                                    ).apply {
                                        topMargin = 16
                                        bottomMargin = 16
                                    }

                                    button.layoutParams = layoutParams
                                    button.text = "Накладная ID: $id"
                                    button.textSize = 16f
                                    button.setTextColor(ContextCompat.getColor(this@ChooseTimeActivity, android.R.color.white))
                                    button.setBackgroundResource(R.drawable.custom_button_background)

                                    button.setOnClickListener {
                                        Toast.makeText(this@ChooseTimeActivity, "Накладная ID: $id", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@ChooseTimeActivity, ChossingActionsActivity::class.java)
                                        intent.putExtra("ewaybill_id", id)
                                        startActivity(intent)
                                    }
                                    ewaybillListOutput.addView(button)
                                }
                            } else {
                                ewaybillListTextView.visibility = TextView.VISIBLE
                                val noDataText = TextView(this@ChooseTimeActivity)
                                noDataText.text = "Накладные не найдены."
                                ewaybillListOutput.addView(noDataText)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ChooseTimeActivity, "Ошибка аутентификации", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChooseTimeActivity, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d.%02d.%d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun authenticate(): String? {
        val authUrl = "https://edi-pub.ctt.by/api/v1/authentication/authenticate"
        val authPayload = JSONObject().apply {
            put("username", "zhuk")
            put("password", "Ctttest1")
        }
        val connection = URL(authUrl).openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(authPayload.toString().toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val responseJson = JSONObject(response)
                responseJson.getString("uuid")
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun getEwaybillsList(
        uuid: String,
        startDate: String,
        endDate: String,
        companyName: String
    ): List<String> {
        val apiUrl = "https://edi-pub.ctt.by/api/v1/EWAYBILL/ALL/filteredList?page=1&size=50"
        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        val apiPayload = JSONObject().apply {
            put("documentDateStart", convertDateFormat(startDate))
            put("documentDateEnd", convertDateFormat(endDate, isEndDate = true))
        }

        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", uuid)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(apiPayload.toString().toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                val ewaybills = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val ewaybill = jsonArray.getJSONObject(i)
                    val sender = ewaybill.optString("sender", "")
                    val receiver = ewaybill.optString("receiver", "")
                    val id = ewaybill.optString("id", "")
                    if (companyName in sender || companyName in receiver) {
                        ewaybills.add(id)
                    }
                }

                ewaybills.sorted()
            } else {
                emptyList()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun convertDateFormat(dateStr: String, isEndDate: Boolean = false): String {
        val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        return outputFormat.format(date).let {
            if (isEndDate) it.replace("00:00:00", "23:59:59") else it
        }
    }
}
