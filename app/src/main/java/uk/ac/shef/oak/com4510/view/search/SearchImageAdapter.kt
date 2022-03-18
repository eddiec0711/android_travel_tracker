package uk.ac.shef.oak.com4510.view.search

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.view.inspect.DisplayActivity

class SearchImageAdapter(cont: Context, images: List<Image>) :
    RecyclerView.Adapter<SearchImageAdapter.ViewHolder>() {

    private var context: Context = cont
    private var items: List<Image> = images

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_image_row,
            parent, false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // get thumbnail image from uri
        var thumbnailUri = items[position].thumbnailUri
        var bitmap = BitmapFactory.decodeFile(thumbnailUri)
        items[position].thumbnail = bitmap
        holder.imageView.setImageBitmap(bitmap)

        // set image data
        holder.titleTextView.text = items[position].imageTitle
        holder.descriptionTextView.text = items[position].imageDescription

        // bind on click action
        holder.itemView.setOnClickListener(View.OnClickListener {
            val mainActivityContext = context as SearchActivity
            mainActivityContext.startForResult.launch(
                Intent(context, DisplayActivity::class.java).apply {
                    putExtra("imageId", items[position].imageId)
                }
            )
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById<View>(R.id.image_item_row) as ImageView
        var titleTextView: TextView = itemView.findViewById<View>(R.id.image_item_title) as TextView
        var descriptionTextView: TextView = itemView.findViewById<View>(R.id.image_item_description) as TextView
    }

    /**
     * call to update filtered image list
     *
     * @param images - filtered list of images
     * @return void
     */
    fun setItems(images: List<Image>) {
        items = images
    }
}