package com.example.healthdiary.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//al usar room, este modelo hara de tabla de la bbdd
@Entity(tableName = "table_PA")
data class PA_item_model(
    @ColumnInfo(name = "SYS")
    val sys:Int,
    @ColumnInfo(name = "DIA")
    val dia:Int,
    @ColumnInfo(name = "ppp")
    val ppp:Int,
    @ColumnInfo(name = "fecha")
    val fecha:String
)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
