package uk.ac.shef.oak.com4510.utility

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import uk.ac.shef.oak.com4510.R


class MapPlotter {

    companion object {
        private const val COLOR_BLACK_ARGB = -0x1000000
        private const val POLYLINE_STROKE_WIDTH_PX = 12

        /**
         * Styles the polyline, based on type.
         * @param polyline The polyline object that needs styling.
         */
        fun stylePolyline(polyline: Polyline) {
            // Get the data object stored with the polyline.
            val type = polyline.tag?.toString() ?: ""
            when (type) {
                "A" -> {
                    // Use a custom bitmap as the cap at the start of the line.
                    polyline.startCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f
                    )
                }
                "B" -> {
                    // Use a round cap at the start of the line.
                    polyline.startCap = RoundCap()
                }
            }
            polyline.endCap = RoundCap()
            polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
            polyline.color = COLOR_BLACK_ARGB
            polyline.jointType = JointType.ROUND
        }

        /**
         * it draws the polyline by the given location
         * @param location
         */
        fun updateRoute(location : LatLng, polyline: Polyline): Polyline {
            // Add polylines to the map
            val points: MutableList<LatLng> = polyline.points
            points.add(location)
            polyline.points = points

            return polyline
        }

        fun startRoute(location: LatLng, mMap: GoogleMap): Polyline {
            val polyline = mMap.addPolyline(
                    PolylineOptions().add(location)
                )
                polyline.tag = "A"
                // Style the polyline.
                stylePolyline(polyline)

            return polyline
        }
    }
}