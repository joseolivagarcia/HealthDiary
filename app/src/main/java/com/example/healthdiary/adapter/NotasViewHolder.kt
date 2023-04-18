package com.example.healthdiary.adapter

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.R
import com.example.healthdiary.models.Nota_item_model

class NotasViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val nota = view.findViewById<TextView>(R.id.tvnota)

    fun render(
        notamodel: Nota_item_model,
        onClickDelete: (Nota_item_model) -> Unit)
    {
        nota.text = notamodel.nota

        itemView.setOnLongClickListener {
            onClickDelete(notamodel)
            true
        }
    }
}