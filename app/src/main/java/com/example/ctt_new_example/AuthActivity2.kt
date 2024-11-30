package com.example.ctt_new_example

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity2 : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth2)

        val userLogin: EditText = findViewById(R.id.user_login_auth)
        val userPass: EditText = findViewById(R.id.user_pass_auth)
        val button: Button = findViewById(R.id.button_auth)
        val linkToReg: TextView = findViewById(R.id.link_to_reg)
        val showPasswordButton: ImageView = findViewById(R.id.showPasswordButton)

        // Обработчик для перехода на экран регистрации
        linkToReg.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Закрыть текущую Activity
        }

        // Обработчик для отображения/скрытия пароля
        showPasswordButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                userPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                showPasswordButton.setImageResource(R.drawable.open_eye)
            } else {
                userPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                showPasswordButton.setImageResource(R.drawable.close_eye)
            }
            userPass.setSelection(userPass.text.length)
        }

        // Обработчик для авторизации
        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val pass = userPass.text.toString().trim()

            if (login == "" || pass == "") {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            } else {
                val db = DbHelper(this, null)
                val isAuth = db.getUser(login, pass)

                if (isAuth) {
                    Toast.makeText(this, "Пользователь $login авторизован", Toast.LENGTH_LONG).show()
                    userLogin.text.clear()
                    userPass.text.clear()

                    // Переход в ChooseCompanyActivity после успешной авторизации
                    val intent = Intent(this, ChooseCompanyActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Пользователь $login не авторизован", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
