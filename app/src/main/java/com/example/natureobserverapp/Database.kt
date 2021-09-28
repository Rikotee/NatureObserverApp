package com.example.natureobserverapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class NatureObservation(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val picturePath: String,
    val dateAndTime: String,
    val locationLat: Double,
    val locationLon: Double,
    val lightValue: Double
)

@Entity(foreignKeys = [ForeignKey(
    entity = NatureObservation::class,
    onDelete = ForeignKey.CASCADE,
    parentColumns = ["id"],
    childColumns = ["observationId"])])
data class WeatherInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val observationId: Long,
    val description: String,
    val icon: String,
    val temp: Double,
    val pressure: Long,
    val humidity: Long,
    val windSpeed: Double,
    val windDeg: Long,
    val country: String,
    val placeName: String
)

class NatureObservationWithWeatherInfo {
    @Embedded
    var natureObservation: NatureObservation? = null
    @Relation(parentColumn = "id", entityColumn = "observationId")
    var weatherInfo: WeatherInfo? = null
}

@Dao
interface NatureObservationDao {
    @Query("SELECT * FROM natureobservation")
    fun getAll(): LiveData<List<NatureObservation>>

    @Query("SELECT * FROM natureobservation WHERE natureobservation.id = :natureObservationId")
    fun getNatureObservationWithWeatherInfo(natureObservationId: Long): NatureObservationWithWeatherInfo

    @Query("SELECT * FROM natureobservation")
    fun getAllNatureObservationsWithWeatherInfo(): LiveData<List<NatureObservationWithWeatherInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(natureobservation: NatureObservation): Long

    @Update
    fun update(natureobservation: NatureObservation)

    @Delete
    fun delete(natureobservation: NatureObservation)
}

@Dao
interface WeatherInfoDao {
    @Query("SELECT * FROM weatherinfo")
    fun getAll(): LiveData<List<WeatherInfo>>

    @Query("SELECT * FROM weatherinfo WHERE weatherinfo.id = :weatherInfoId")
    fun getWeatherInfosOfNatureObservation(weatherInfoId: Long): LiveData<List<WeatherInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weatherinfo: WeatherInfo): Long

    @Update
    fun update(weatherinfo: WeatherInfo)

    @Delete
    fun delete(weatherinfo: WeatherInfo)
}

@Database(entities = [(NatureObservation::class), (WeatherInfo::class)], version = 1)
abstract class NatureObservationDB: RoomDatabase() {
    abstract fun natureObservationDao(): NatureObservationDao
    abstract fun weatherInfoDao(): WeatherInfoDao

    companion object {
        private var sInstance: NatureObservationDB? = null
        @Synchronized
        fun get(context: Context): NatureObservationDB {
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.applicationContext, NatureObservationDB::class.java,
                    "nature_observations.db").build()
            }
            return sInstance!!
        }
    }
}