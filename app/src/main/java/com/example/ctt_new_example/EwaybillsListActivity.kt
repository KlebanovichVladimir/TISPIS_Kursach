package com.example.ctt_new_example

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class EwaybillsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ewaybills_list)

        val ewaybillsList: ListView = findViewById(R.id.ewaybills_list_view)
        val ewaybills = intent.getStringArrayListExtra("ewaybills") ?: ArrayList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ewaybills)
        ewaybillsList.adapter = adapter
    }
}
