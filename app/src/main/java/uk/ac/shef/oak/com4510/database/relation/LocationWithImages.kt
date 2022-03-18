package uk.ac.shef.oak.com4510.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Location

data class LocationWithImages (
    @Embedded var location: Location,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "locationId"
    )
    var images: MutableList<Image>
)