package com.example.healthdiary.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.healthdiary.R
import com.example.healthdiary.databinding.ActivityPresionArterialAddBinding

class PresionArterialAdd : AppCompatActivity() {

    private lateinit var binding: ActivityPresionArterialAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresionArterialAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}