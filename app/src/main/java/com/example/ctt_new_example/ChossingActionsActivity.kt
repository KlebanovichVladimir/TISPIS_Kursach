package com.example.ctt_new_example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ChossingActionsActivity : AppCompatActivity() {
    private var authUuid: String? = null // Сохраняем токен аутентификации

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chossing_actions)

        // Извлекаем ID накладной из Intent
        val ewaybillId = intent.getStringExtra("ewaybill_id")
        val ewaybillIdTextView: TextView = findViewById(R.id.ewaybill_id_text_view)

        // Обновляем TextView с ID накладной (если он присутствует)
        if (ewaybillId != null) {
            ewaybillIdTextView.text = "ID: $ewaybillId"
            ewaybillIdTextView.visibility = TextView.VISIBLE
        } else {
            ewaybillIdTextView.text = "ID накладной отсутствует"
        }

        // Выполняем аутентификацию
        authenticate { uuid ->
            if (uuid != null) {
                authUuid = uuid
                // Обновляем TextView с ID накладной после успешной аутентификации
                if (ewaybillId != null) {
                    ewaybillIdTextView.text = "Накладная ID: $ewaybillId"
                }
            } else {
                // Если аутентификация не удалась, показываем сообщение об ошибке
                ewaybillIdTextView.text = "Ошибка аутентификации"
            }
        }

        // Настраиваем кнопку для отображения информации о накладной
        val ewaybillDetailsButton: Button = findViewById(R.id.ewaybill_details_button)
        ewaybillDetailsButton.setOnClickListener {
            if (ewaybillId != null && authUuid != null) {
                // Переход в новое окно с деталями накладной
                val intent = Intent(this, EwaybillInformationActivity::class.java)
                intent.putExtra("ewaybill_id", ewaybillId)
                intent.putExtra("auth_uuid", authUuid)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Не удалось открыть детали накладной", Toast.LENGTH_SHORT).show()
            }
        }

        // Настраиваем кнопку для скачивания накладной
        val ewaybillDownloadButton: Button = findViewById(R.id.ewaybill_download_button)
        ewaybillDownloadButton.setOnClickListener {
            if (ewaybillId != null && authUuid != null) {
                downloadEwaybillPdf(ewaybillId, authUuid!!)
            } else {
                Toast.makeText(this, "Ошибка: ID накладной или токен отсутствует", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функция для аутентификации
    private fun authenticate(callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val authUrl = "https://edi-pub.ctt.by/api/v1/authentication/authenticate"
            val payload = JSONObject()
            payload.put("username", "zhuk") // Замените на реальные данные
            payload.put("password", "Ctttest1") // Замените на реальные данные

            try {
                val url = URL(authUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write(payload.toString().toByteArray()) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()

                if (responseCode == 200) {
                    val responseJson = JSONObject(response)
                    val uuid = responseJson.optString("uuid")
                    withContext(Dispatchers.Main) {
                        callback(uuid) // Возвращаем UUID
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null) // Возвращаем ошибку
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null) // Возвращаем ошибку
                }
            }
        }
    }

    // Функция для скачивания накладной в формате PDF
    private fun downloadEwaybillPdf(ewaybillId: String, uuid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("https://edi-pub.ctt.by/api/v1/export/createFormatDocuments?exportFormatType=PDF&printEds=false&shortForm=true")
            val body = JSONObject()
            body.put("id", JSONArray().put(ewaybillId.toInt())) // Преобразуем ID в число
            body.put("msgTypeForGenerator", "EWAYBILL")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", uuid)
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                // Сохраняем файл в папке Download
                val pdfFile = File(getExternalFilesDir(null), "ewaybill.pdf")
                connection.inputStream.use { input ->
                    FileOutputStream(pdfFile).use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChossingActionsActivity, "Накладная скачана", Toast.LENGTH_SHORT).show()
                    // Открываем PDF файл
                    openPdf(pdfFile)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChossingActionsActivity, "Ошибка скачивания накладной", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Метод для открытия PDF файла
    private fun openPdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Даем временный доступ для чтения файла
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
