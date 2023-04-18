package com.example.healthdiary.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.adapter.NotasAdapter
import com.example.healthdiary.databinding.ActivityNotasBinding
import com.example.healthdiary.models.Nota_item_model
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.viewmodel.PAViewModel

class NotasActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotasBinding
    private lateinit var viewModel: PAViewModel
    lateinit var recyclerview: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerview = binding.rvNotas
        recyclerview.layoutManager = GridLayoutManager(this,2)
        val notasAdapter = NotasAdapter(onClickDelete = {nota -> onItemDelete(nota)})
        recyclerview.adapter = notasAdapter


        //inicializo el viewmodel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(PAViewModel::class.java)

        viewModel.listaNotas.observe(this,{list -> list?.let{
            //actualizamos la lista
            notasAdapter.updateList(it)
        }})

        initListeners()
    }

    private fun initListeners() {

        binding.btnaddnota.setOnClickListener {
            val nota = binding.etnota.text.toString()
            val newNota = Nota_item_model(nota)
            viewModel.addNota(newNota)
            binding.etnota.setText("")

        }
    }

    private fun onItemDelete(nota: Nota_item_model){
        val dialog = AlertDialog.Builder(this)
            .setMessage("¿Quieres eliminar ésta nota?")
            .setNegativeButton("NO"){
                    view, _ -> view.dismiss()
            }
            .setPositiveButton("SI"){
                    view, _ -> view.dismiss()
                viewModel.deleteNota(nota)
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }
}