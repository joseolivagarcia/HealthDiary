package com.example.healthdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.healthdiary.bd.PADatabase
import com.example.healthdiary.models.Nota_item_model
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.repositorio.PARepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PAViewModel(aplication: Application): AndroidViewModel(aplication) {
    /*
    creamos una var donde guardaremos todos los registros (una lista)
    y una var para referenciar nuestro repositorio
    Daran error hasta que las inicialicemos
     */
    val listaregistros: LiveData<List<PA_item_model>>
    val listaUltimosReg: LiveData<List<PA_item_model>>
    val listaNotas: LiveData<List<Nota_item_model>>
    val listaUltimasNotas: LiveData<List<Nota_item_model>>
    val repositorio: PARepositorio
    init{
        /* el dao lo obtengo desde la clase PADatabase llamando a la fun getDatabase
            y pasandola el contexto que estoy cogiendo de esta misma clase
            y llamando a la fun getPositDao de la misma clase PADatabase
         */
        val dao = PADatabase.getDatabase(aplication).getPADao()
        //inicializo el repo pasandole el dao que acabo de obtener
        repositorio = PARepositorio(dao)
        //y obtengo todos los registros en la var que creé arriba
        listaregistros = repositorio.listaPA_items
        listaUltimosReg = repositorio.listaUltimosReg
        listaNotas = repositorio.listaNotas
        listaUltimasNotas = repositorio.listaUltNotas
    }
    /*
     Me creo las funciones para insertat,borrar o editar registros. LLamare a las funciones que
     hay en el repositorio.
     Lo hago con viewModelScope para no hacerlo en el hilo ppal y no bloquear la app
     */
    fun addRegistro (paitem: PA_item_model) = viewModelScope.launch(Dispatchers.IO) {
        repositorio.insertPA_items(paitem)
    }
    fun deleteRegistro(paitem: PA_item_model) = viewModelScope.launch(Dispatchers.IO) {
        repositorio.deletePA_items(paitem)
    }
    fun updateRegistro(paitem: PA_item_model) = viewModelScope.launch(Dispatchers.IO) {
        repositorio.updatePA_items(paitem)
    }

    fun addNota (notaitem: Nota_item_model) = viewModelScope.launch(Dispatchers.IO){
        repositorio.insertNota(notaitem)
    }

    fun deleteNota(notaitem: Nota_item_model) = viewModelScope.launch (Dispatchers.IO){
        repositorio.deleteNota(notaitem)
    }
}