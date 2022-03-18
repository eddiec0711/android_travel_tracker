package uk.ac.shef.oak.com4510.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations


class SectionAdapter(trips: List<TripWithLocations>) :
    RecyclerView.Adapter<SectionAdapter.ViewHolder>() {

    private var items: List<TripWithLocations> = trips
    private var sortDate: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_section,
            parent, false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // bind trip title and child images
        if (sortDate) {
            holder.sectionView.text = items[position].trip.tripStartTime
        }
        else {
            holder.sectionView.text = items[position].trip.tripTitle
        }
        holder.bindImages(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sectionView: TextView = itemView.findViewById<View>(R.id.section_title) as TextView

        // function to bind child recycler view to display image
        fun bindImages(tripWithLocations: TripWithLocations) {
            val numberOfColumns = 4
            val imageRecyclerView: RecyclerView =
                itemView.findViewById<View>(R.id.image_recycler_view) as RecyclerView
            imageRecyclerView.layoutManager = GridLayoutManager(itemView.context, numberOfColumns)

            // retrieve images for child recycler view
            val images = mutableListOf<Image>()
            for (location in tripWithLocations.locations) {
                images.addAll(location.images)
            }

            val imageAdapter = ImageAdapter(itemView.context, images)
            imageRecyclerView.adapter = imageAdapter
        }
    }

    /**
     * called in MainActivity to rearrange items
     * @param trips - trips data to display associated images in section
     * @param date - if true, sort by date; else by trip
     */
    fun setItems(trips: List<TripWithLocations>, date: Boolean) {
        items = trips
        sortDate = date
    }
}