package com.example.ctt_new_example

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class EwaybillInformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ewaybill_information)

        val ewaybillId = intent.getStringExtra("ewaybill_id")
        val authUuid = intent.getStringExtra("auth_uuid")

        if (ewaybillId != null && authUuid != null) {
            fetchEwaybillDetails(ewaybillId, authUuid)
        } else {
            Toast.makeText(this, "Данные для запроса отсутствуют", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEwaybillDetails(ewaybillId: String, authUuid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiUrl = "https://edi-pub.ctt.by/api/v1/EWAYBILL/$ewaybillId"
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", authUuid)

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()

                if (responseCode == 200) {
                    val responseJson = JSONObject(response)
                    withContext(Dispatchers.Main) {
                        displayEwaybillDetails(responseJson)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EwaybillInformationActivity, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EwaybillInformationActivity, "Произошла ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayEwaybillDetails(data: JSONObject) {
        val detailsTextView: TextView = findViewById(R.id.ewaybill_details_text_view)

        // Основная информация
        val documentNumber = data.optString("documentNumber", "Отсутствует")
        val documentDate = data.optString("documentDate", "Отсутствует")
        val shipperName = data.optString("shipperName", "Отсутствует")
        val receiverName = data.optString("receiverName", "Отсутствует")
        val totalAmountWithVat = data.optString("totalAmountWithVat", "Отсутствует")
        val currency = data.optJSONObject("currency")?.optString("code", "Отсутствует")
        val totalGrossWeight = data.optString("totalGrossWeight", "Отсутствует")

        // Дополнительная информация
        val contractNumber = data.optString("contractNumber", "Отсутствует")
        val contractName = data.optString("contractName", "Отсутствует")
        val contractDate = data.optString("contractDate", "Отсутствует")
        val transportNumber = data.optString("transportNumber", "Отсутствует")
        val shipperContact = data.optString("shipperContact", "Отсутствует")
        val receiverContact = data.optString("receiverContact", "Отсутствует")
        val transporterName = data.optString("transporterName", "Отсутствует")

        // Продукты
        val productList = data.optJSONArray("msgEwaybillProductList") ?: JSONArray()

        val builder = StringBuilder()

        builder.append("Основная информация:\n")
        builder.append("Номер накладной: $documentNumber\n")
        builder.append("Дата накладной: $documentDate\n")
        builder.append("Отправитель: $shipperName\n")
        builder.append("Получатель: $receiverName\n")
        builder.append("Сумма с НДС: $totalAmountWithVat $currency\n")
        builder.append("Общий вес: $totalGrossWeight кг\n\n")

        builder.append("Дополнительная информация:\n")
        builder.append("Номер контракта: $contractNumber\n")
        builder.append("Название контракта: $contractName\n")
        builder.append("Дата контракта: $contractDate\n")
        builder.append("Номер транспорта: $transportNumber\n")
        builder.append("Контакт отправителя: $shipperContact\n")
        builder.append("Контакт получателя: $receiverContact\n")
        builder.append("Транспортировщик: $transporterName\n\n")

        builder.append("Список продуктов:\n")
        for (i in 0 until productList.length()) {
            val product = productList.getJSONObject(i)
            val productName = product.optString("fullName", "Отсутствует")
            val productCode = product.optString("gtin", "Отсутствует")
            val quantity = product.optString("quantityDespatch", "Отсутствует")
            val uom = product.optJSONObject("uom")?.optString("name", "не указано")
            val priceNet = product.optString("priceNet", "Отсутствует")
            val amountWithoutVat = product.optString("amountWithoutVat", "Отсутствует")
            val amountWithVat = product.optString("amountWithVat", "Отсутствует")
            val grossWeight = product.optString("grossWeight", "Отсутствует")

            builder.append("Продукт: $productName (Код товара: $productCode)\n")
            builder.append("Количество: $quantity $uom\n")
            builder.append("Цена без НДС: $priceNet $currency\n")
            builder.append("Сумма без НДС: $amountWithoutVat $currency\n")
            builder.append("Сумма с НДС: $amountWithVat $currency\n")
            builder.append("Вес: $grossWeight кг\n\n")
        }

        detailsTextView.text = builder.toString()
    }
}
