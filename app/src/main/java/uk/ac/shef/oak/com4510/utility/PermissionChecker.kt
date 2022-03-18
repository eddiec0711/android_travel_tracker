package uk.ac.shef.oak.com4510.utility

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import uk.ac.shef.oak.com4510.view.trip.MapActivity

class PermissionChecker {

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 2987
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 7829
        private const val REQUEST_CAMERA_CODE = 100

        const val ACCESS_FINE_LOCATION = 123

        /**
         * when start the map, ask for get the permission for access location
         */
        fun getLocationPermission(activity: Activity) {
            if (ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
                return
            }
        }

        /**
         * check permissions are necessary starting from Android 6
         * if you do not set the permissions, the activity will simply not work and you will be probably baffled for some hours
         * until you find a note on StackOverflow
         * @param context the calling context
         */
        fun checkPermissions(context: Context, activity: Activity) {
            val currentAPIVersion = Build.VERSION.SDK_INT
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        val alertBuilder: AlertDialog.Builder =
                            AlertDialog.Builder(context)
                        alertBuilder.setCancelable(true)
                        alertBuilder.setTitle("Permission necessary")
                        alertBuilder.setMessage("External storage permission is necessary")
                        alertBuilder.setPositiveButton(android.R.string.ok,
                            DialogInterface.OnClickListener { _, _ ->
                                ActivityCompat.requestPermissions(
                                    context as Activity, arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ), REQUEST_READ_EXTERNAL_STORAGE
                                )
                            })
                        val alert: AlertDialog = alertBuilder.create()
                        alert.show()
                    } else {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                        alertBuilder.setCancelable(true)
                        alertBuilder.setTitle("Permission necessary")
                        alertBuilder.setMessage("Writing external storage permission is necessary")
                        alertBuilder.setPositiveButton(android.R.string.ok,
                            DialogInterface.OnClickListener { _, _ ->
                                ActivityCompat.requestPermissions(
                                    context as Activity, arrayOf(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ), REQUEST_WRITE_EXTERNAL_STORAGE
                                )
                            })
                        val alert: AlertDialog = alertBuilder.create()
                        alert.show()
                    } else {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_CODE
                    );
                }
            }
        }
    }
}