package com.example.healthdiary.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthdiary.adapter.NotasAdapter
import com.example.healthdiary.adapter.RegistroPAAdapter
import com.example.healthdiary.databinding.ActivityMainBinding
import com.example.healthdiary.models.PA_item_model
import com.example.healthdiary.models.SettingsModel
import com.example.healthdiary.viewmodel.PAViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: PAViewModel
    lateinit var recyclerViewLastReg: RecyclerView
    lateinit var recyclerviewLastNotas: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        * antes de inicializar la UI me tengo que enganchar al flow para que recupere los datos (settings)
        * que esten guardados o coja los que se crean por defecto. Esto logicamente hay que hacerlo en
        * una corutina. El collect es lo que hace que nos enganchemos al flow y estemos siempre escuchando*/
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().collect { settingsModel ->
                if (settingsModel != null) {
                    //como esto va a modificar la UI debe hacerse en el hilo ppal para que no pete
                    runOnUiThread {
                        binding.tvnombre.setText(settingsModel.nombre)
                    }
                }
            }
        }

        //recycler para los ultimos registros
        recyclerViewLastReg = binding.rvregistros
        recyclerViewLastReg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val registrosAdapter =
            RegistroPAAdapter(onClickDelete = { registro -> onDeleteItem() })
        //ponemos el adapter creado al recyclerview
        recyclerViewLastReg.adapter = registrosAdapter
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(PAViewModel::class.java)

        //llamamos a la lista de registros del viewmodel para observar los cambios que se produzcan
        viewModel.listaUltimosReg.observe(this, { list ->
            list?.let {
                //actualizamos la lista
                registrosAdapter.updateList(it)
            }
        })

        //recycler para las ultimas notas
        recyclerviewLastNotas = binding.rvrnotas
        recyclerviewLastNotas.layoutManager = GridLayoutManager(this,2)
        val notasAdaapter = NotasAdapter(onClickDelete = {nota -> onDeleteItem()})
        recyclerviewLastNotas.adapter = notasAdaapter
        viewModel.listaUltimasNotas.observe(this,{list ->
            list?.let{
                notasAdaapter.updateList(it)
            }
        })


        initUI()
        initListener()
    }

    private fun initListener() {
        binding.btnperfil.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        binding.btnpresionarterial.setOnClickListener {
            val intent = Intent(this, PresionArterialActivity::class.java)
            startActivity(intent)
        }

        binding.btnmenunotas.setOnClickListener {
            val intent = Intent(this,NotasActivity::class.java)
            startActivity(intent)
        }

    }

    private fun initUI() {

    }

    private fun onDeleteItem() {
        //en esta activity no me interesa que pueda borrar los registros
    }

    private fun getSettings(): Flow<SettingsModel> {
        //la siguiente linea llama al datastore y recorre lo que haya. al ser un flow hay que recorrerlo
        return dataStore.data.map { preferences ->
            //creo un objeto de tipo SettingsModel pasandole los 6 parametros que he guardado
            //como los parametros pueden ser nulos usamos el operador elvis ?: para dar un valor por defecto si fuera nulo
            //el id del radiobutton del sexo me lo he inventado porque aqui no lo necesito (solo necesito el nombre)
            SettingsModel(
                nombre = preferences[stringPreferencesKey(Perfil.NOMBRE)] ?: "Tu nombre",
                sexo = preferences[intPreferencesKey(Perfil.SEXO)] ?: 22233,
                altura = preferences[intPreferencesKey(Perfil.ALTURA)] ?: 150,
                peso = preferences[floatPreferencesKey(Perfil.PESO)] ?: 60f,
                imc = preferences[floatPreferencesKey(Perfil.IMC)] ?: 0f,
                darkmode = preferences[booleanPreferencesKey(Perfil.DARKMODE)] ?: false
            )

        }

    }
}