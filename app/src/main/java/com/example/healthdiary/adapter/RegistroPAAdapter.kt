package com.example.healthdiary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.models.PA_item_model

class RegistroPAAdapter(
    private val onClickDelete:(PA_item_model) -> Unit
): RecyclerView.Adapter<RegistroPAViewHolder>(){

    //creo una lista donde guardo todos los registros
    private val allRegistros = ArrayList<PA_item_model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroPAViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RegistroPAViewHolder(layoutInflater.inflate(R.layout.presion_rv_item,parent,false))
    }

    override fun onBindViewHolder(holder: RegistroPAViewHolder, position: Int) {
        val item = allRegistros[position]
        holder.render(item,onClickDelete)
    }

    override fun getItemCount(): Int = allRegistros.size

    //para actualizar la lista cuando haya registros nuevos

    fun updateList(newList: List<PA_item_model>){
        allRegistros.clear()
        allRegistros.addAll(newList)
        notifyDataSetChanged()
    }

}