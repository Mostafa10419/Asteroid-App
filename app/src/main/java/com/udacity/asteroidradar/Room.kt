package com.udacity.asteroidradar

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from asteroid where closeApproachDate = :today order by closeApproachDate")
    suspend fun getTodayAsteroids(today: String): List<Asteroid>?

    @Query("select * from asteroid where closeApproachDate >= :today order by closeApproachDate")
    suspend fun getWeekAsteroids(today: String): List<Asteroid>?

    @Query("select * from asteroid order by closeApproachDate")
    suspend fun getAsteroids(): List<Asteroid>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(asteroids: List<Asteroid>)
}

@Database(entities = [Asteroid::class], version = 1)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}