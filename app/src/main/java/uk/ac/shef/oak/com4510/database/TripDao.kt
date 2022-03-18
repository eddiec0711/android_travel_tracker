package uk.ac.shef.oak.com4510.database

import androidx.lifecycle.LiveData
import androidx.room.*
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.database.data.Trip
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations

/**
 * Database access object to access the Inventory database
 */
@Dao
interface TripDao {

    // Access data
    @Transaction
    @Query("SELECT * FROM trip ORDER BY tripId ASC")
    fun getTripsWithLocations(): LiveData<List<TripWithLocations>>

    @Query("SELECT * FROM trip WHERE tripId = :id")
    fun getTrip(id: Int): LiveData<TripWithLocations>

    @Query("SELECT * FROM image")
    fun getImages(): LiveData<List<Image>>

    @Query("SELECT * FROM image WHERE imageId = :id")
    fun getImage(id: Int): LiveData<Image>

    @Query("SELECT * FROM location WHERE locationId = :id")
    fun getLocation(id: Int): LiveData<Location>

    // Search function
    @Query("SELECT * FROM image WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%'")
    fun searchImage(keyword: String): LiveData<List<Image>>

    // Insert data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Image): Long

    // Modifying image data
    @Update
    suspend fun update(imageData: Image)

    @Delete
    suspend fun delete(imageData: Image)
}