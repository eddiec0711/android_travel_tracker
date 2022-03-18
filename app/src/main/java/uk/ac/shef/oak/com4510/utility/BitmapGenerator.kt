package uk.ac.shef.oak.com4510.utility

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import uk.ac.shef.oak.com4510.database.data.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BitmapGenerator {

    companion object {
        /**
         * helper function to generate a bitmap object of a given size from an image's file path.
         */
        fun decodeSampledBitmapFromResource(
            filePath: String,
            reqWidth: Int,
            reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()

            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(filePath, options);
        }


        // Method to save an bitmap to a file
        fun bitmapToFile(bitmap: Bitmap, applicationContext: Context, imageTitle: String): String {
            // Get the context wrapper
            val wrapper = ContextWrapper(applicationContext)

            // Initialize a new file instance to save bitmap object
            var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
            file = File(file, imageTitle)

            try{
                // Compress the bitmap and save in jpg format
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
                stream.flush()
                stream.close()
            } catch (e: IOException){
                e.printStackTrace()
            }

            // Return the saved bitmap uri
            return file.absolutePath
        }

        /**
         * helper function to calculate an inSampleSize for resampling to achieve the right picture
         * density for a smaller size file.
         * See inSampleFile: https://developer.android.com/reference/android/graphics/BitmapFactory.Options#inSampleSize
         */
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight;
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = (height / 2).toInt()
                val halfWidth = (width / 2).toInt()

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth
                ) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize.toInt();
        }
    }
}