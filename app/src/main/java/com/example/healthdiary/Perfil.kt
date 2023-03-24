package com.example.healthdiary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.healthdiary.databinding.ActivityPerfilBinding
import com.example.healthdiary.models.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/*
* Creamos una funcion de extension que se llamara dataStore del tipo DataStore.
* El by indica que es un delegado de preferenceDataStore y en el name ponemos el
* nombre que queramos dar a nuestra base de datos.
* Ojo al importar el Preferences que sea el de datastore.
* El delegado nos permite crear una unica instancia de la base de datos*/

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Perfil : AppCompatActivity() {

    //creo un companion object para guardarme las constantes que utilizare como clave donde lo necesite
    companion object {
        const val NOMBRE = "nombre"
        const val SEXO = "sexo"
        const val ALTURA = "altura"
        const val PESO = "peso"
        const val IMC = "imc"
        const val DARKMODE = "darkmode"
    }

    private lateinit var binding: ActivityPerfilBinding
    private var firsttime = true
    private var imc: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        * antes de inicializar la UI me tengo que enganchar al flow para que recupere los datos (settings)
        * que esten guardados o coja los que se crean por defecto. Esto logicamente hay que hacerlo en
        * una corutina. El collect es lo que hace que nos enganchemos al flow y estemos siempre escuchando*/
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firsttime }.collect { settingsModel ->
                if (settingsModel != null) {
                    //como esto va a modificar la UI debe hacerse en el hilo ppal para que no pete
                    runOnUiThread {
                        binding.etnombre.setText(settingsModel.nombre)
                        binding.rsAltura.setValues(settingsModel.altura.toFloat())
                        binding.rsPeso.setValues(settingsModel.peso)
                        binding.tvIMC.setText(settingsModel.imc.toString())
                        binding.switchdark.isChecked = settingsModel.darkmode

                        firsttime = !firsttime
                    }
                }
            }
        }
        initUI()
    }

    private fun initUI() {

        /*hacemos que cuando cambie el slider de la altura y/o el peso pase algo.
        Solo necesitamos el valor de value, por eso las _ en los otros parametros.
        Lo que voy a hacer es guardar el valor llamando a saveVolume, pero ojo! hay que
        hacerlo con una corutina
        */
        binding.rsAltura.addOnChangeListener { _, value, _ ->
            Log.i("oliva", "La altura es $value")
            CoroutineScope(Dispatchers.IO).launch {
                saveAltura(value.toInt())
            }
        }

        binding.rsPeso.addOnChangeListener { _, value, _ ->
            Log.i("oliva", "El peso es $value")
            CoroutineScope(Dispatchers.IO).launch {
                savePeso(value)
            }
        }

        /* Tambien hacemos lo mismo para los switchs. Estos su metodo para cuando lo cambiamos
        * recibe un boton(que no nos interesa en este caso) y el boolean
        * */
        binding.switchdark.setOnCheckedChangeListener { _, value ->
            if(value){
                //enableDarkMode()
            }else{
                //disableDarkMode()
            }
            CoroutineScope(Dispatchers.IO).launch {
                saveDarkmode(value)
            }
        }

        //guardamos tambien el nombre que escriba el usuario
        binding.etnombre.addTextChangedListener {
            CoroutineScope(Dispatchers.IO).launch {
            saveNombre()
        }
        }

    }

    //creo una funcion donde almacenare la altura y otra para el peso que haya seleccionado el usuario que recibe un int
    //esta funcion se debera llamar desde una corutina, por eso es suspend
    private suspend fun saveAltura(value: Int) {
        //con esta linea editamos la base de datos, la datastore del Context
        dataStore.edit { preferences ->
            //guardamos como un entero en la clave ALTURA el valor que recibios por parametro
            preferences[intPreferencesKey(ALTURA)] = value
            calcularIMC()
        }
    }
    private suspend fun savePeso(value: Float){
        dataStore.edit{preferences ->
            preferences[floatPreferencesKey(PESO)] = value
            calcularIMC()
        }
    }
    //creo tambien la funcion para almacenar si hemos seleccionado o no el modo oscuro
    private suspend fun saveDarkmode(value: Boolean){
        dataStore.edit{ preferences ->
            preferences[booleanPreferencesKey(DARKMODE)] = value
        }
    }

    private suspend fun saveNombre(){
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(NOMBRE)] = binding.etnombre.text.toString()
        }
    }

    private suspend fun saveIMC(){
        dataStore.edit { preferences ->
            preferences[floatPreferencesKey(IMC)] = imc
        }
    }

    //Creamos una funcion para calcular el IMC en funcion de la altura y el peso
    //tengo que hacerla en el hilo ppal porque modifica la UI
    private fun calcularIMC(){
        runOnUiThread {
            imc = binding.rsPeso.values.get(0)/((binding.rsAltura.values.get(0) / 100) * (binding.rsAltura.values.get(0) / 100))
            binding.tvIMC.text = imc.toInt().toString()
        }
        //guardo el valor en la bbdd de datastore
        CoroutineScope(Dispatchers.IO).launch {
            saveIMC()
        }

    }

    /*
    * Hemos guardaddo datos en la base de datos con las funciones anteriores, pero logicamente
    * necesito recuperar esos datos. Lo hacemos con Flows. Flow es una especie de canal que
    * mantiene el contacto constante para que se actualice lo que sea cada vez que haya algun
    * cambio. Creamos una funcion que recuperara los datos cada vez que sea llamada
    *Los flow devuelven un solo tipo de dato, asi que para devolver todos (hay varios tipos en nuestro xml
    * creamos una data class para encapsular todos los valores y devolvemos un objeto del tipo
    * de esa data class*/
    private fun getSettings(): Flow<SettingsModel> {
        //la siguiente linea llama al datastore y recorre lo que haya. al ser un flow hay que recorrerlo
        return dataStore.data.map { preferences ->
            //creo un objeto de tipo SettingsModel pasandole los 4 parametros que he guardado
            //como los parametros pueden ser nulos usamos el operador elvis ?: para dar un valor por defecto si fuera nulo
            SettingsModel(
                nombre = preferences[stringPreferencesKey(NOMBRE)] ?: "Tu nombre",
                //sexo = preferences[booleanPreferencesKey(SEXO)] ?: false,
                altura = preferences[intPreferencesKey(ALTURA)] ?: 150,
                peso = preferences[floatPreferencesKey(PESO)] ?: 60f,
                imc = preferences[floatPreferencesKey(IMC)] ?: 0f,
                darkmode = preferences[booleanPreferencesKey(DARKMODE)] ?: false
            )

        }
    }
}