package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.AsteroidsDatabase
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidsDatabase) {

    private val nasaApiKey = BuildConfig.NASA_API_KEY

    private val calendar = Calendar.getInstance()
    private val currentTime = calendar.time
    private val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    private val dateString = dateFormat.format(currentTime)

    suspend fun updateFilter(filter: Int): List<Asteroid>? {
        return when (filter) {
            0 -> database.asteroidDao.getWeekAsteroids(dateString)
            1 -> database.asteroidDao.getTodayAsteroids(dateString)
            else -> database.asteroidDao.getAsteroids()
        }
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = AsteroidApi.retrofitService.getAsteroidsAsync(nasaApiKey).await()
                val jsonObject = JSONObject(jsonString)
                val asteroids = parseAsteroidsJsonResult(jsonObject)
                database.asteroidDao.insertAll(asteroids)
            } catch (e: Exception) {
                e.message?.let { Log.e("AsteroidRepository", it) }
            }
        }
    }
}