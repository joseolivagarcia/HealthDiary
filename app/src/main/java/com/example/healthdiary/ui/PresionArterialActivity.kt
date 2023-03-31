package com.example.healthdiary.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.adapter.RegistroPAAdapter
import com.example.healthdiary.databinding.ActivityPresionArterialBinding
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.viewmodel.PAViewModel

class PresionArterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresionArterialBinding
    lateinit var viewModel: PAViewModel
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresionArterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvPA
        //creamos el manager del recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        //inicializamos el adapter
        val registrosAdapter = RegistroPAAdapter(onClickDelete = {registro -> onDeleteItem(registro)})
        //ponemos el adapter creado al recyclerview
        recyclerView.adapter = registrosAdapter

        //inicializamos el viewModel con un provider y le pasamos la clase PAViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(PAViewModel::class.java)

        //llamamos a la lista de registros del viewmodel para observar los cambios que se produzcan
        viewModel.listaregistros.observe(this,{list -> list?.let{
            //actualizamos la lista
            registrosAdapter.updateList(it)
        }})

        initListener()
    }

    private fun initListener() {
        binding.fabadd.setOnClickListener {
            val intent = Intent(this,PresionArterialAdd::class.java)
            startActivity(intent)
        }
    }

    //creo la funcion que borra un registro
    private fun onDeleteItem(registro: PA_item_model){
        val dialog = AlertDialog.Builder(this)
            .setMessage("¿Quieres eliminar el registro?")
            .setNegativeButton("NO"){
                view, _ -> view.dismiss()
            }
            .setPositiveButton("SI"){
                view, _ -> view.dismiss()
                viewModel.deleteRegistro(registro)
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }
}