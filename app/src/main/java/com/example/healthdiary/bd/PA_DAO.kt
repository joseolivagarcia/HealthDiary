package com.example.healthdiary.bd

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.healthdiary.models.Nota_item_model
import com.example.healthdiary.models.PA_item_model

//en el DAO tendremos las querys para operar con la bbdd
@Dao
interface PA_DAO {
    //query para obtener todas los registros ordenados por id
    @Query("Select * from table_PA Order By id Desc")
    fun getAllPA_items(): LiveData<List<PA_item_model>>

    //query para obtener los ultimos registros limitados a 3
    @Query("select * from table_PA Order By fecha Desc Limit 3")
    fun getLastPA_items(): LiveData<List<PA_item_model>>

    //query para obtener todas las notas
    @Query("Select * from table_Notas Order By id Desc")
    fun getAllNotas(): LiveData<List<Nota_item_model>>

    //query para coger las ultimas notas
    @Query("Select * from table_Notas Order by id Desc Limit 3")
    fun getLastNotas(): LiveData<List<Nota_item_model>>


    //operaciones de insertar,borrar y actualizar, son suspend fun porque se ejecutan fuera del hilo ppal
    @Insert
    suspend fun insertPA_item(pa_item: PA_item_model)
    @Delete
    suspend fun deletePA_item(pa_item: PA_item_model)
    @Update
    suspend fun updatePA_item(pa_item: PA_item_model)

    //para insertar nota
    @Insert
    suspend fun insertNota(notaitem: Nota_item_model)
    //para borrar nota
    @Delete
    suspend fun deleteNota(nota_item: Nota_item_model)
}