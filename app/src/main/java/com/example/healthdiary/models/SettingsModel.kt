package com.example.healthdiary.models

import android.net.Uri
import android.widget.RadioButton

data class SettingsModel(
    var foto: String,
    var nombre: String,
    var sexo: Int,
    var altura: Int,
    var peso: Float,
    var darkmode: Boolean,
    var imc: Float
)
