package com.example.healthdiary.bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthdiary.models.Nota_item_model
import com.example.healthdiary.models.PA_item_model

//La clase para la base de datos debe ser abstracta y hereda de Room
@Database(
    entities = arrayOf(PA_item_model::class,Nota_item_model::class),
    version = 1
)
abstract class PADatabase: RoomDatabase() {
    abstract fun getPADao(): PA_DAO
    /*
    Metemos el companion object para prevenir que se abran
    multiples instancias de la bbdd al mismo tiempo
     */
    companion object{
        @Volatile
        private var INSTANCE: PADatabase? = null
        fun getDatabase(context: Context): PADatabase{
            //si la instancia no es nula la retorna
            //si es nula, crea la bbdd
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PADatabase::class.java,
                    "pa_database"
                ).build()
                INSTANCE = instance
                //devolvemos la instance
                instance
            }
        }
    }
}