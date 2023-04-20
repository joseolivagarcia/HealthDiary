package com.example.healthdiary.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.healthdiary.models.SettingsModel
import com.example.healthdiary.viewmodel.PAViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: PAViewModel
    lateinit var recyclerViewLastReg: RecyclerView
    lateinit var recyclerviewLastNotas: RecyclerView

    //variables para la geolocalizacion
    companion object{
        const val REQUEST_CODE: Int = 1
    }
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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

        enableLocation() //este metodo se llama para saber si tenemos permisos de ubicacion
        initUI()
        initListener()
    }

    /*
    * con esta funcion comprobamos si los permisos estan dados o no.
    * Si estan dados hacemos una cosa y si no tendremos que pedirlos
    * */
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if (isLocationPermissionGranted()){
            //si hemos aceptado los permisos
            Log.i("mapa","hemos aceptado los permisos")
            getLocation()
        }else{
            //si no hemos aceptado los permisos, los pedimos en la siguiente funcion
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this,"ve a ajustes y acepta los permisos de localización para una experiencia completa ",Toast.LENGTH_SHORT).show()

        }else{

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_CODE)
        }
    }

    //sobreescribimos el metodo que nos da la respuesta a si tenemos permisos o no
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLocation()
            }else{
                Toast.makeText(this,"Para activar la localizacion ve a ajustes y acepta los permisos",Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    /*//sobreescribimos este metodo por si cambiamos de app y volvemos a esta y hemos cambiado los permisos
    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        if(!isLocationPermissionGranted()){
            Toast.makeText(this,"Para activar la localizacion ve a ajustes y acepta los permisos",Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun getLocation() {
        binding.tvlocation.text = "Boadilla del Monte"
        Toast.makeText(this,"Localizacion Activada",Toast.LENGTH_SHORT).show()
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