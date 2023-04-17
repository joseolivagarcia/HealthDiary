package com.example.healthdiary.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_Notas")
data class Nota_item_model(
    @ColumnInfo(name = "nota")
    val nota: String
)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
