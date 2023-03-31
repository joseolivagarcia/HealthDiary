package com.example.healthdiary.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.models.PA_item_model

class RegistroPAViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val fecha = view.findViewById<TextView>(R.id.tvfecha)
    val sistole = view.findViewById<TextView>(R.id.tvsistole)
    val diastole = view.findViewById<TextView>(R.id.tvdiastole)
    val ppp = view.findViewById<TextView>(R.id.tvpulsaciones)

    fun render(
        pamodel: PA_item_model,
        onClickDelete: (PA_item_model) -> Unit
    ){
        fecha.text = pamodel.fecha
        sistole.text = pamodel.sys.toString()
        diastole.text = pamodel.dia.toString()
        ppp.text = pamodel.ppp.toString()

        itemView.setOnLongClickListener {
            onClickDelete(pamodel)
            true
        }
    }

}