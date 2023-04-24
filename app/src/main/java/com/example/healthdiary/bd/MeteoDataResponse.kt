package com.example.healthdiary.bd

import com.google.gson.annotations.SerializedName

//Esta data class es para recuperar los datos que quiera del json de la api
//los nombres de las var deben ser exactamente iguales a los que haya en el json a no ser que le ponga la etiqueta @SerializedName("nombre en el json") val comoquiera llamarlo
data class MeteoDataResponse(
    @SerializedName("data") val meteoData: List<MeteoItemResponse>
)

//creo otra data class donde guardo los datos que me interesan de cada item que trae el resultado
data class MeteoItemResponse(
    @SerializedName("city_name") val meteoCity: String,
    @SerializedName("temp") val meteoTemp: Double,
    //necesito acceder a datos que no cuelgan de la raiz sino de otro padre, en este caso weather asi que lo recupero aqui
    @SerializedName("weather") val meteoWeather: MeteoWeatherResponse //creo esta data class que contendra los datos de dentro de weather
)

data class MeteoWeatherResponse(
    @SerializedName("icon") val icon: String,
    @SerializedName("description") val description: String
)

//
