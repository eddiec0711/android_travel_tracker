package uk.ac.shef.oak.com4510.view.main

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.view.inspect.DisplayActivity


class ImageAdapter(cont: Context, images: List<Image>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var context: Context = cont
    private var items: List<Image> = images

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_image,
            parent, false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // get thumbnail image from uri
        val thumbnailUri = items[position].thumbnailUri
        val bitmap = BitmapFactory.decodeFile(thumbnailUri)
        holder.imageView.setImageBitmap(bitmap)

        // bind onClick to display image
        holder.itemView.setOnClickListener(View.OnClickListener {
            val mainActivityContext = context as MainActivity
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
        var imageView: ImageView = itemView.findViewById<View>(R.id.image_item) as ImageView
    }
}