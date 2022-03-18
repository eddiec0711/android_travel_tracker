package uk.ac.shef.oak.com4510.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.database.data.Trip
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations
import uk.ac.shef.oak.com4510.repository.TripRepository


class TripViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: TripRepository = TripRepository(application)

    /**
     * get all trips from db
     * @return all trips with associated locations and images
     */
    fun getTripsWithLocations(): LiveData<List<TripWithLocations>>? {
        return mRepository.getTripsWithLocations()
    }

    /**
     * get single trip from db
     * @param id - trip id
     * @return single trip
     */
    fun getTrip(id: Int): LiveData<TripWithLocations>? {
        return mRepository.getTrip(id)
    }

    /**
     * get location from db
     * @param id - location id
     * @return single location
     */
    fun getLocation(id: Int): LiveData<Location>? {
        return mRepository.getLocation(id)
    }

    /**
     * get all images from db
     * @return all images
     */
    fun getImages(): LiveData<List<Image>>? {
        return mRepository.getImages()
    }

    /**
     * get single image from db
     * @param id - image id
     * @return single image
     */
    fun getImage(id: Int): LiveData<Image>? {
        return mRepository.getImage(id)
    }

    /**
     * search image from db
     * @param keyword - word to input as query, match with title or description
     * @return all match images
     */
    fun searchImage(keyword: String): LiveData<List<Image>>? {
        return mRepository.searchImage(keyword)
    }

    /**
     * insert trip with locations/images into db
     * @param trip - trip with locations object to be saved
     */
    fun insertTrip(trip: TripWithLocations) {
        viewModelScope.launch(Dispatchers.IO) { mRepository.insertTrip(trip) }
    }

    /**
     * store image change by user into db
     * @param image - image to be updated
     */
    fun updateImage(imageData: Image) {
        mRepository.updateImage(imageData)
    }

    /**
     * delete image from db
     * @param image - image to be deleted
     */
    fun deleteImage(imageData: Image) {
        mRepository.deleteImage(imageData)
    }
}

