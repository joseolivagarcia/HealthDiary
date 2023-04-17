package com.example.healthdiary.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.healthdiary.R
import com.example.healthdiary.databinding.ActivityNotasBinding

class NotasActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {

        binding.btnaddnota.setOnClickListener {
            Log.i("oliva","presionado añadir nota")
        }
    }
}