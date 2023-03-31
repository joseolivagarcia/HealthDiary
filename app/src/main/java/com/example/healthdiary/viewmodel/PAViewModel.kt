package com.example.healthdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.healthdiary.bd.PADatabase
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.repositorio.PARepositorio

class PAViewModel(aplication: Application): AndroidViewModel(aplication) {
    /*
    creamos una var donde guardaremos todos los registros (una lista)
    y una var para referenciar nuestro repositorio
    Daran error hasta que las inicialicemos
     */
    val listaregistros: LiveData<List<PA_item_model>>
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
    }
}