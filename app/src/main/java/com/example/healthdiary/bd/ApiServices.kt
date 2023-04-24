package com.example.healthdiary.bd

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//creo una interface que me servira para usar la API, para preparar la llamada al ApiRest
//En este caso la llamada es por GET
//vamos a usar una corutina para no saturar el hilo principal
interface ApiServices {
    @GET("current")
    //@GET("?lat=latitud&lon=longitud&key=4228cda124a341a6976cbcb6def30c07")
    suspend fun getMeteo(@Query("lat")lat: Double,@Query("lon")lon: Double,
                         @Query("Key")key:String = "4228cda124a341a6976cbcb6def30c07",
                         @Query("lang")lang:String = "es"): Response<MeteoDataResponse>
}