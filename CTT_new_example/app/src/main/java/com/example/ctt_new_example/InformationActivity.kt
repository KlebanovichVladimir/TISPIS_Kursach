package com.example.ctt_new_example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class EwaybillDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val ewaybillId = intent.getStringExtra("ewaybill_id")
        val uuid = intent.getStringExtra("auth_uuid")
        val detailsTextView: TextView = findViewById(R.id.ewaybill_details_text_view)

        if (ewaybillId != null && uuid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val details = fetchEwaybillDetails(ewaybillId, uuid)
                runOnUiThread {
                    detailsTextView.text = details ?: "Не удалось загрузить информацию"
                }
            }
        } else {
            detailsTextView.text = "ID накладной отсутствует"
        }
    }

    private fun fetchEwaybillDetails(ewaybillId: String, uuid: String): String? {
        val url = "https://edi-pub.ctt.by/api/v1/EWAYBILL/$ewaybillId"
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", uuid)
            connection.connect()

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)

                // Форматируем результат
                val documentNumber = jsonObject.optString("documentNumber", "N/A")
                val shipperName = jsonObject.optString("shipperName", "N/A")
                val receiverName = jsonObject.optString("receiverName", "N/A")
                val totalAmountWithVat = jsonObject.optString("totalAmountWithVat", "N/A")
                val currency = jsonObject.optJSONObject("currency")?.optString("code", "N/A") ?: "N/A"

                """
                Номер накладной: $documentNumber
                Отправитель: $shipperName
                Получатель: $receiverName
                Общая сумма с НДС: $totalAmountWithVat $currency
                """.trimIndent()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
