package com.example.healthdiary.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
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
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: PAViewModel
    lateinit var recyclerViewLastReg: RecyclerView
    lateinit var recyclerviewLastNotas: RecyclerView

    //variables para la geolocalizacion
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

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

        getCurrentLocation() //este metodo se llama para saber si tenemos permisos de ubicacion
        initUI()
        initListener()
    }

    /*
    * con esta funcion comprobamos si los permisos estan dados o no.
    * Si estan dados hacemos una cosa y si no tendremos que pedirlos
    * */

    private fun getCurrentLocation() {

        if (checkPermissions()){
            if (isLocationEnable()){
                //final latitud y longitud code here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task ->
                    val location: Location? = task.result
                    if(location == null){
                        Toast.makeText(this,"Null received",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Get Success",Toast.LENGTH_SHORT).show()
                        val latitud = location.latitude
                        val longitud = location.longitude
                        binding.tvlocation.text = "${latitud} / $longitud"
                    }
                }

            }
            else{
                //setting open here
                Toast.makeText(this,"Activa la localizacion",Toast.LENGTH_SHORT).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else{
            //request permission here
            requestPermission()
        }
    }

    private fun isLocationEnable(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions(): Boolean{
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            return true
        }

        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"GRANTED", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext,"DENIED", Toast.LENGTH_SHORT).show()
            }
        }
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