package com.example.healthdiary.bd

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.healthdiary.models.PA_item_model

//en el DAO tendremos las querys para operar con la bbdd
@Dao
interface PA_DAO {
    //query para obtener todas los registros ordenados por id
    @Query("Select * from table_PA Order By id Asc")
    fun getAllPA_items(): LiveData<List<PA_item_model>>

    //operaciones de insertar,borrar y actualizar, son suspend fun porque se ejecutan fuera del hilo ppal
    @Insert
    suspend fun insertPA_item(pa_item: PA_item_model)
    @Delete
    suspend fun deletePA_item(pa_item: PA_item_model)
    @Update
    suspend fun updatePA_item(pa_item: PA_item_model)
}