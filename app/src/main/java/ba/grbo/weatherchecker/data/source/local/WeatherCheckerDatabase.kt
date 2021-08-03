package ba.grbo.weatherchecker.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ba.grbo.weatherchecker.data.models.local.Place

@Database(entities = [Place::class], version = 1)
@TypeConverters(Converters::class)
abstract class WeatherCheckerDatabase : RoomDatabase() {
    abstract val placeDao: PlaceDao

    companion object {
        @Volatile
        private lateinit var INSTANCE: WeatherCheckerDatabase

        fun getInstance(context: Context) = if (::INSTANCE.isInitialized) INSTANCE
        else synchronized(this) { buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(
                context,
                WeatherCheckerDatabase::class.java,
                "weather_checker_database"
            )
            .build()
    }
}