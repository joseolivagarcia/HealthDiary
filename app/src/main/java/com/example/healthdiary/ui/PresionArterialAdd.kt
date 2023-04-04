package com.example.healthdiary.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthdiary.R
import com.example.healthdiary.databinding.ActivityPresionArterialAddBinding
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.viewmodel.PAViewModel
import java.text.SimpleDateFormat
import java.util.*

class PresionArterialAdd : AppCompatActivity() {

    private lateinit var binding: ActivityPresionArterialAddBinding
    private lateinit var viewModel: PAViewModel
    var id: Int = -1 //le doy este valor porque luego usare el real

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresionArterialAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()

        //inicializo el viewmodel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
                    .get(PAViewModel::class.java)
    }

    private fun initListeners() {
        binding.btnguardardatos.setOnClickListener {
            //cuando presionamos guardar, obtenemos la media de las 3 lecturas
            val mediaPAS: Int =
                (binding.etpasdato1.text.toString().toInt() +
                        binding.etpasdato2.text.toString().toInt() +
                        binding.etpasdato3.text.toString().toInt()) / 3
            val mediaPAD: Int =
                (binding.etpaddato1.text.toString().toInt() +
                        binding.etpaddato2.text.toString().toInt() +
                        binding.etpaddato3.text.toString().toInt()) / 3
            val mediaPPP: Int =
                (binding.etfcdato1.text.toString().toInt() +
                        binding.etfcdato2.text.toString().toInt() +
                        binding.etfcdato3.text.toString().toInt()) / 3
            //obtengo la fecha y hora del sistema
            val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
            val currentdatetime: String = sdf.format(Date())
            //creo un nuevo item con los datos obtenidos(las medias de lo que ha escrito el usuario)
            val newRegistro = PA_item_model(mediaPAS,mediaPAD,mediaPPP,currentdatetime)
            //y lo guardo en la bbdd a traves del viewmodel
            viewModel.addRegistro(newRegistro)
            //vuelvo a la actividad que muestra los datos en el recyclerview
            startActivity(Intent(applicationContext, PresionArterialActivity::class.java))
            this.finish()
        }


    }
}