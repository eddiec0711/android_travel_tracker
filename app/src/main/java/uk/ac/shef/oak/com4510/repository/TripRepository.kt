package uk.ac.shef.oak.com4510.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import uk.ac.shef.oak.com4510.database.TripRoomDatabase
import uk.ac.shef.oak.com4510.database.TripDao
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations

class TripRepository(application: Application) {
    private var tripDao: TripDao? = null

    init {
        val db: TripRoomDatabase? = TripRoomDatabase.getDatabase(application)
        if (db != null) { tripDao = db.tripDao() }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        // insert trip associated with locations
        private class InsertAsyncTask(private val dao: TripDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: TripWithLocations) {
                scope.launch {
                    // insert trip
                    val tripId: Int? = this@InsertAsyncTask.dao?.insertTrip(params[0].trip)?.toInt()

                    // insert locations
                    for (locationWithImages in params[0].locations) {
                        val location = locationWithImages.location
                        location.tripId = tripId
                        val locationId: Int? = this@InsertAsyncTask.dao?.insertLocation(location)?.toInt()

                        // insert images
                        for (image in locationWithImages.images) {
                            image.locationId = locationId
                            this@InsertAsyncTask.dao?.insertImage(image)
                        }
                    }
                }
            }
        }
    }

    /**
     * get all trips from db
     * @return all trips with associated locations and images
     */
    fun getTripsWithLocations(): LiveData<List<TripWithLocations>>? {
        return tripDao?.getTripsWithLocations()
    }

    /**
     * get single trip from db
     * @param id - trip id
     * @return single trip
     */
    fun getTrip(id: Int): LiveData<TripWithLocations>? {
        return tripDao?.getTrip(id)
    }

    /**
     * get location from db
     * @param id - location id
     * @return single location
     */
    fun getLocation(id: Int): LiveData<Location>? {
        return tripDao?.getLocation(id)
    }

    /**
     * get all images from db
     * @return all images
     */
    fun getImages(): LiveData<List<Image>>? {
        return tripDao?.getImages()
    }

    /**
     * get single image from db
     * @param id - image id
     * @return single image
     */
    fun getImage(id: Int): LiveData<Image>? {
        return tripDao?.getImage(id)
    }

    /**
     * search image from db
     * @param keyword - word to input as query, match with title or description
     * @return all match images
     */
    fun searchImage(keyword: String): LiveData<List<Image>>? {
        return tripDao?.searchImage(keyword)
    }

    /**
     * insert trip with locations/images into db
     * @param trip - trip with locations object to be saved
     */
    suspend fun insertTrip(trip: TripWithLocations) {
        InsertAsyncTask(tripDao).insertInBackground(trip)
    }

    /**
     * store image change by user into db
     * @param image - image to be updated
     */
    fun updateImage(image: Image) = runBlocking {
        var insertJob = async { tripDao?.update(image) }
        insertJob.await()
    }

    /**
     * delete image from db
     * @param image - image to be deleted
     */
    fun deleteImage(image: Image) = runBlocking {
        var insertJob = async { tripDao?.delete(image) }
        insertJob.await()
    }
}