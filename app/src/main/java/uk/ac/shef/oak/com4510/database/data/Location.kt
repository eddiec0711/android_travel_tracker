package uk.ac.shef.oak.com4510.database.data

import androidx.room.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "location")
data class Location(
    @PrimaryKey(autoGenerate = true) var locationId: Int = 0,
    @ColumnInfo(name="longitude") var locationLong: Double,
    @ColumnInfo(name="latitude") var locationLat: Double,
    @ColumnInfo(name="temperature") var locationTemp: Float,
    @ColumnInfo(name="pressure") var locationPressure: Float,
    @ColumnInfo(name="tripId") var tripId: Int? = 0,
)
