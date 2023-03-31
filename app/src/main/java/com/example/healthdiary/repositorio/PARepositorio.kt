package com.example.healthdiary.repositorio

import androidx.lifecycle.LiveData
import com.example.healthdiary.bd.PA_DAO
import com.example.healthdiary.models.PA_item_model
/*
El repositorio es una clase desde la que se decide de donde
obtener los datos. Pueden obtenerse desde una API o desde una
bbdd local como Room.
El repositorio tiene la logica para decidir de donde obtendra los datos.
Esta clase recibe un objeto de tipo DAO
 */

class PARepositorio(val padao: PA_DAO) {
    //aqui recupero todos los registros que haya en la bbdd
    val listaPA_items: LiveData<List<PA_item_model>> = padao.getAllPA_items()

    //y creo las funciones que usare para cada operacion
    suspend fun insertPA_items(paitem: PA_item_model){
        padao.insertPA_item(paitem)
    }
    suspend fun deletepostit(paitem: PA_item_model){
        padao.deletePA_item(paitem)
    }
    suspend fun updatepostit(paitem: PA_item_model){
        padao.updatePA_item(paitem)
    }
}