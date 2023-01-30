package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.*
import com.udacity.asteroidradar.api.PictureApi
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val nasaApiKey = BuildConfig.NASA_API_KEY

    private val database = getDatabase(application)

    private val asteroidRepository = AsteroidRepository(database)

    private val _asteroids = MutableLiveData<List<Asteroid>?>()

    val asteroids: LiveData<List<Asteroid>?>
            get() = _asteroids

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()

    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    private val _picture = MutableLiveData<PictureOfDay?>()

    val picture: LiveData<PictureOfDay?>
        get() = _picture

    init {
        viewModelScope.launch {
            try {
                _picture.value = PictureApi.retrofitService.getPictureAsync(nasaApiKey).await()
            } catch (e: Exception) {
                _picture.value = PictureOfDay("","Connection Error","")
            }
            asteroidRepository.refreshAsteroids()
        }
        updateFilter(0)
    }

    fun updateFilter(filter: Int) {
        viewModelScope.launch {
            _asteroids.value = asteroidRepository.updateFilter(filter)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}