package com.example.healthdiary.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.healthdiary.R
import com.example.healthdiary.databinding.ActivityPresionArterialBinding

class PresionArterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresionArterialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresionArterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.fabadd.setOnClickListener {
            val intent = Intent(this,PresionArterialAdd::class.java)
            startActivity(intent)
        }
    }
}