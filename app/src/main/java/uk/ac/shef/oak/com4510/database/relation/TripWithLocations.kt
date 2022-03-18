package uk.ac.shef.oak.com4510.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.database.data.Trip

data class TripWithLocations (
    @Embedded var trip: Trip,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "tripId",
        entity = Location::class
    )
    var locations: List<LocationWithImages>
)