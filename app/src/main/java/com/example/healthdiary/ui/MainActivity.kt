package com.example.healthdiary.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
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
import com.example.healthdiary.R
import com.example.healthdiary.adapter.NotasAdapter
import com.example.healthdiary.adapter.RegistroPAAdapter
import com.example.healthdiary.bd.ApiServices
import com.example.healthdiary.bd.MeteoDataResponse
import com.example.healthdiary.databinding.ActivityMainBinding
import com.example.healthdiary.models.SettingsModel
import com.example.healthdiary.viewmodel.PAViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: PAViewModel
    lateinit var recyclerViewLastReg: RecyclerView
    lateinit var recyclerviewLastNotas: RecyclerView

    //variable para obtener el idioma del dispositivo
    private var idioma: String = "es"

    //variables para la geolocalizacion
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    //variable para poder usar retrofit
    private lateinit var retrofit: Retrofit

    private lateinit var imgpervia: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inicializo esta var con la ruta a la imagen que pondre por defecto como perfil
        imgpervia = "android.resource://" + packageName + "/" + R.drawable.ic_profile

        idioma =  Locale.getDefault().language
        Log.i("idioma", "$idioma")

        //variable para usar los servicios de geolocalizacion
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //inicializo la var retrofit llamando a la funcion que crea un objeto de retrofit
        retrofit = getRetrofit()
        /*
        * antes de inicializar la UI me tengo que enganchar al flow para que recupere los datos (settings) del datastore
        * que esten guardados o coja los que se crean por defecto. Esto logicamente hay que hacerlo en
        * una corutina. El collect es lo que hace que nos enganchemos al flow y estemos siempre escuchando*/
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().collect { settingsModel ->
                if (settingsModel != null) {
                    //como esto va a modificar la UI debe hacerse en el hilo ppal para que no pete
                    runOnUiThread {
                        binding.ivPerfil.setImageURI(Uri.parse(settingsModel.foto))
                        binding.tvnombre.setText(settingsModel.nombre)
                        binding.tvaltura.setText("${settingsModel.altura} cm")
                        binding.tvpeso.setText("${settingsModel.peso} kg")
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

        if (checkPermissions()) {
            if (isLocationEnable()) {
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
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null received", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Get Success", Toast.LENGTH_SHORT).show()
                        latitud = location.latitude
                        longitud = location.longitude
                        //llamo a la funcion que recoge los datos del tiempo de la API y le paso la lat y lon
                        getMeteo()
                        //binding.tvlocation.text = "$latitud / $longitud"
                    }
                }

            } else {
                //setting open here
                Toast.makeText(this, "Activa la localizacion", Toast.LENGTH_SHORT).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            //request permission here
            requestPermission()
        }
    }

    private fun isLocationEnable(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    //hay que sobreescribir este metodo que es el que analiza el resultado obtenido de si tenemos permisos o no
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "GRANTED", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //esta funcion devuelve un objeto de tipo Retrofit
    private fun getRetrofit(): Retrofit {
        //guardamos en una var el objeto Retrofit con su url correspondiente y las opciones para convertir el json
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherbit.io/v2.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit
    }

    //esta es la funcion que va a llamar a la API a traves de retrofit

    private fun getMeteo() {
        //Log.d("meteo", "$lat / $lon")
        //hacemos la llamada al retrofit (con la corutina)
        //el dispatchers especifica el hilo en donde queremos ejecutar, en este caso un hilo secundario
        CoroutineScope(Dispatchers.IO).launch {
            //guardamos en una var la llamada a nuestra Apiservice y a la funcion necesaria a la que pasamos los datos de localizacion
            val myResponse = retrofit.create(ApiServices::class.java).getMeteo(latitud, longitud,idioma)
            if (myResponse.isSuccessful) {
                Log.i("retro", "FUNCIONA!!!")
                //ahora en una val guardo lo que traiga la respuesta a traves del .body()
                val response: MeteoDataResponse? =
                    myResponse.body() //tener en cuenta que MeteoDataResponse puede ser nulo
                if (response != null) {
                    //Log.i("datosmeteo","${response.meteoData[0].meteoCity}")
                    //como voy a modificar la UI lo tengo que hacer en un hilo ppal y no en este que es secundario
                    runOnUiThread {
                        binding.tvlocation.text =
                            response.meteoData[0].meteoCity //pongo la ciudad que me devuelva la api
                        binding.tvtemp.text =
                            response.meteoData[0].meteoTemp.toString() //pongo la temp que devuelva la api
                        binding.tvdescription.text =
                            response.meteoData[0].meteoWeather.description//pongo la temp que devuelva la api
                        /*Para nostrar el icono, lo que recibo de la api es un string, asi que guardo el string y luego en otra var de tipo
                        * Drawable recupero el icono que estan en la carpeta Drawable (son png descargados). Luego se lo asigno al imageview*/
                        val iconname: String = response.meteoData[0].meteoWeather.icon
                        val icon: Drawable? = ContextCompat.getDrawable(
                            binding.tvlocation.context,
                            resources.getIdentifier(iconname, "drawable", packageName)
                        )
                        binding.iviconmeteo.setImageDrawable(icon)
                    }
                } else {
                    //Log.i("datosmeteo","no tengo datos")
                }
            } else {
//                Log.i("retro","Retro no funciona")
//                Log.i("retro","$myResponse")
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
            val intent = Intent(this, NotasActivity::class.java)
            startActivity(intent)
        }

        binding.btninfo.setOnClickListener {
            val intent = Intent(this,InfoActivity::class.java)
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
        viewModel.listaUltimosReg.observe(this) { list ->
            list?.let {
                //actualizamos la lista
                registrosAdapter.updateList(it)
            }
        }

        //recycler para las ultimas notas
        recyclerviewLastNotas = binding.rvrnotas
        recyclerviewLastNotas.layoutManager = GridLayoutManager(this, 2)
        val notasAdaapter = NotasAdapter(onClickDelete = { nota -> onDeleteItem() })
        recyclerviewLastNotas.adapter = notasAdaapter
        viewModel.listaUltimasNotas.observe(this) { list ->
            list?.let {
                notasAdaapter.updateList(it)
            }
        }

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
                foto = preferences[stringPreferencesKey((Perfil.IMAGEN))] ?: imgpervia,
                nombre = preferences[stringPreferencesKey(Perfil.NOMBRE)] ?: "Your name",
                sexo = preferences[intPreferencesKey(Perfil.SEXO)] ?: 22233,
                altura = preferences[intPreferencesKey(Perfil.ALTURA)] ?: 0,
                peso = preferences[floatPreferencesKey(Perfil.PESO)] ?: 0f,
                imc = preferences[floatPreferencesKey(Perfil.IMC)] ?: 0f,
                darkmode = preferences[booleanPreferencesKey(Perfil.DARKMODE)] ?: false
            )

        }

    }
}