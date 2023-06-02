package com.example.healthdiary.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.widget.addTextChangedListener
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.healthdiary.R
import com.example.healthdiary.databinding.ActivityPerfilBinding
import com.example.healthdiary.models.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File


/*
* Creamos una funcion de extension que se llamara dataStore del tipo DataStore.
* El by indica que es un delegado de preferenceDataStore y en el name ponemos el
* nombre que queramos dar a nuestra base de datos.
* Ojo al importar el Preferences que sea el de datastore.
* El delegado nos permite crear una unica instancia de la base de datos*/

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Perfil : AppCompatActivity() {
    //hace falta crear un launcher para llamar a la "actividad" que abre la galeria
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        //aqui necesitamos una funcion lambda que nos devolvera la uri de la imagen seleccionada
            uri->
        if(uri !=null){
            //tenemos imagen
            binding.imageperfil.setImageURI(uri) //traigo la uri (imagen seleccionada) despues de lanzar el launch desde el "boton"
            //guardamos la foto escogida(pasandola a file) como string en la base de datos datastore. Hay que hacerlo en Corutina
            CoroutineScope(Dispatchers.IO).launch {
                //llamo a la fun guardar imagen y le paso la uri ya que la necesito para convertirla a file y guardar la imagen en el dispositivo
                saveImagen(uri)
            }

        }else{
            //no tenemos imagen
        }
    }

    //creo un companion object para guardarme las constantes que utilizare como clave donde lo necesite
    companion object {
        const val IMAGEN = "imagen"
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

    private lateinit var imgprevia: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inicializo esta var con la ruta a la imagen que pondre por defecto como perfil
        imgprevia = "android.resource://" + packageName + "/" + R.drawable.ic_profile
        /*
        * antes de inicializar la UI me tengo que enganchar al flow para que recupere los datos (settings)
        * que esten guardados o coja los que se crean por defecto. Esto logicamente hay que hacerlo en
        * una corutina. El collect es lo que hace que nos enganchemos al flow y estemos siempre escuchando*/
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firsttime }.collect { settingsModel ->
                if (settingsModel != null) {
                    //como esto va a modificar la UI debe hacerse en el hilo ppal para que no pete
                    runOnUiThread {
                        //binding.imageperfil.setImageResource(R.drawable.ic_profile) //mientras arreglo lo de los permisos
                        binding.imageperfil.setImageURI(Uri.parse(settingsModel.foto))
                        binding.etnombre.setText(settingsModel.nombre)
                        binding.rsAltura.setValues(settingsModel.altura.toFloat())
                        binding.rgsexo.check(settingsModel.sexo)
                        binding.rsPeso.setValues(settingsModel.peso)
                        binding.tvIMC.setText(settingsModel.imc.toString())
                        binding.switchdark.isChecked = settingsModel.darkmode

                        firsttime = !firsttime
                    }
                }else{
                    binding.imageperfil.setImageResource(R.drawable.ic_profile)
                }
            }
        }
        initUI()
    }

    private fun initUI() {

        //para escoger una foto de la galeria y traerla al imageview pulsamos sobre el
        binding.imageperfil.setOnClickListener {
            //aqui llamamos al launcher que creamos arriba y le pasamos el tipo de recurso que queremos traer (en este caso una foto)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }

        /*hacemos que cuando cambie el slider de la altura y/o el peso pase algo.
        Solo necesitamos el valor de value, por eso las _ en los otros parametros.
        Lo que voy a hacer es guardar el valor llamando a saveAltura y savePeso, pero ojo! hay que
        hacerlo con una corutina
        */
        binding.rsAltura.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                saveAltura(value.toInt())
            }
        }

        binding.rsPeso.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                savePeso(value)
            }
        }

        /* Tambien hacemos lo mismo para los switchs. Estos su metodo para cuando lo cambiamos
        * recibe un boton(que no nos interesa en este caso) y el boolean
        * */
        binding.switchdark.setOnCheckedChangeListener { _, value ->
            if(value){
                enableDarkMode()
            }else{
                disableDarkMode()
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

        //para guardar el sexo, obtenemos la id del radiobutton seleccionado a traves del radiogroup
        //si no cambiamos la seleccion, sexo valdra lo que estuviera marcado por defecto
        binding.rgsexo.setOnCheckedChangeListener{_, value ->
            CoroutineScope(Dispatchers.IO).launch {
            saveSexo(value)
            }
        }

    }

    //creo una funcion para guardar la imagen
    private suspend fun saveImagen(uri: Uri){
        //guardo el uri de la imagen convertido a file y a su vez a bytes para guardarlo en el dispositivo
        val file = File(applicationContext.filesDir,"foto")
        val bytes = applicationContext.contentResolver.openInputStream(uri)?.readBytes()!!
        file.writeBytes(bytes)
        //vuelvo a obtener la uri pero ahora del file que he guardado en el dispositivo(no el de la galeria)
        val uridisp = Uri.fromFile(file)
        //y la guardo en los settings como un string
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(IMAGEN)] = uridisp.toString()
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

    private suspend fun saveSexo(sexo: Int){
        dataStore.edit{preferences ->
            preferences[intPreferencesKey(SEXO)] = sexo
            Log.i("sexo","He guardado $sexo")
        }
    }

    //Creamos una funcion para calcular el IMC en funcion de la altura y el peso
    //tengo que hacerla en el hilo ppal porque modifica la UI

    private fun calcularIMC(){
        runOnUiThread {
            imc = binding.rsPeso.values.get(0)/((binding.rsAltura.values.get(0) / 100) * (binding.rsAltura.values.get(0) / 100))
            if(imc < 19){
                binding.tvIMC.setTextColor(Color.parseColor("#2196F3"))
            }else if(imc > 19 && imc < 25){
                binding.tvIMC.setTextColor(Color.parseColor("#0BDD14"))
            }else{
                binding.tvIMC.setTextColor(Color.parseColor("#F11707"))
            }
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
            //creo un objeto de tipo SettingsModel pasandole los 6 parametros que he guardado
            //como los parametros pueden ser nulos usamos el operador elvis ?: para dar un valor por defecto si fuera nulo
            SettingsModel(
                foto = preferences[stringPreferencesKey(IMAGEN)] ?: imgprevia,
                nombre = preferences[stringPreferencesKey(NOMBRE)] ?: "",
                sexo = preferences[intPreferencesKey(SEXO)] ?: binding.rbhombre.id,
                altura = preferences[intPreferencesKey(ALTURA)] ?: 150,
                peso = preferences[floatPreferencesKey(PESO)] ?: 60f,
                imc = preferences[floatPreferencesKey(IMC)] ?: 0f,
                darkmode = preferences[booleanPreferencesKey(DARKMODE)] ?: false
            )
        }
    }

    private fun enableDarkMode(){

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()

    }

    private fun disableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }

    override fun onBackPressed() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}