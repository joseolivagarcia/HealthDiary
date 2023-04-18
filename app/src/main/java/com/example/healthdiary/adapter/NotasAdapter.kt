package com.example.healthdiary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.models.Nota_item_model
import com.example.healthdiary.models.PA_item_model

class NotasAdapter(
    private val onClickDelete:(Nota_item_model) -> Unit
):RecyclerView.Adapter<NotasViewHolder>() {

    private val allNotas = ArrayList<Nota_item_model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotasViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        return NotasViewHolder(layoutInflater.inflate(R.layout.notas_rv_item,parent,false))

    }

    override fun onBindViewHolder(holder: NotasViewHolder, position: Int) {

        val item = allNotas[position]
        holder.render(item,onClickDelete)

    }

    override fun getItemCount() = allNotas.size

    fun updateList(newList: List<Nota_item_model>){
        allNotas.clear()
        allNotas.addAll(newList)
        notifyDataSetChanged()
    }

}
