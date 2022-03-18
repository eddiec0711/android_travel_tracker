package uk.ac.shef.oak.com4510.database.data

import android.graphics.Bitmap
import androidx.room.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "image", indices = [Index(value = ["imageId","title"])])
data class Image(
    @PrimaryKey(autoGenerate = true)var imageId: Int = 0,
    @ColumnInfo(name="uri") val imageUri: String,
    @ColumnInfo(name="title") var imageTitle: String,
    @ColumnInfo(name="description") var imageDescription: String? = null,
    @ColumnInfo(name="thumbnailUri") var thumbnailUri: String? = null,
    @ColumnInfo(name="locationId") var locationId: Int? = null
)
{
    @Ignore
    var thumbnail: Bitmap? = null
}