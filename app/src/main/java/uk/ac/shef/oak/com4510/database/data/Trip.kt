package uk.ac.shef.oak.com4510.database.data

import androidx.room.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "trip")
data class Trip(
    @PrimaryKey(autoGenerate = true) var tripId: Int = 0,
    @ColumnInfo(name="title") var tripTitle: String,
    @ColumnInfo(name="startTime") var tripStartTime: String
)
