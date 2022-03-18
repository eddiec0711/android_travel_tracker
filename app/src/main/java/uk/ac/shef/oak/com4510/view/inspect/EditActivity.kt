package uk.ac.shef.oak.com4510.view.inspect

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import uk.ac.shef.oak.com4510.R
import kotlinx.coroutines.*
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel


class EditActivity : AppCompatActivity() {

    private var tripViewModel: TripViewModel? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val bundle: Bundle? = intent.extras
        this.tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]

        if (bundle != null) {
            val image = tripViewModel!!.getImage(bundle.getInt("imageId"))
            image!!.observe(this,
                { imageData ->
                    displayData(imageData)
                }
            )
        }
    }

    /**
     * display the image and populate input columns with predefined data
     *
     * @param image - image object to be displayed and edit
     * @return void
     */
    private fun displayData(image: Image) {
        // access view objects for display
        val imageView = findViewById<ImageView>(R.id.image_edit)
        val titleEditToolbar = findViewById<Toolbar>(R.id.toolbar_edit)
        val titleTextInput = findViewById<TextInputEditText>(R.id.editText_image_title)
        val descriptionTextInput =
            findViewById<TextInputEditText>(R.id.editText_image_description)

        // initialise button listeners
        makeButtonListeners(image)

        // retrieve image data and display
        image.let {
            val bitmap = BitmapFactory.decodeFile(it.thumbnailUri)
            imageView.setImageBitmap(bitmap)

            titleEditToolbar.title = it.imageTitle
            titleTextInput.setText(it.imageTitle)
            descriptionTextInput.setText(it.imageDescription ?: "N/A")
        }
    }

    /**
     * add click listeners to all button and bind them to image db actions as follow
     * 1. cancel - quit interface
     * 2. delete - delete image
     * 3. save - update image
     *
     * @param image - image to be bind with edit actions
     * @return void
     */
    private fun makeButtonListeners(image: Image) {
        // Cancel button listener
        val cancelButton: Button = findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            this@EditActivity.finish()
        }

        // Delete button listener
        val deleteButton: Button = findViewById(R.id.button_delete)
        deleteButton.setOnClickListener {
            scope.launch(Dispatchers.IO) {
                async { tripViewModel?.deleteImage(image) }
                    .invokeOnCompletion {
                        val intent = Intent()
                            .putExtra("imageId", image.imageId)
                        this@EditActivity.setResult(Activity.RESULT_OK, intent)
                        this@EditActivity.finish()
                    }
            }
        }

        // Save button listener
        val saveButton: Button = findViewById(R.id.button_save)
        saveButton.setOnClickListener {
            val descriptionTextInput =
                findViewById<TextInputEditText>(R.id.editText_image_description)
            image.imageDescription = descriptionTextInput.text.toString()

            val titleTextInput = findViewById<TextInputEditText>(R.id.editText_image_title)
            image.imageTitle = titleTextInput.text.toString()

            scope.launch(Dispatchers.IO) {
                async { tripViewModel?.updateImage(image) }
                    .invokeOnCompletion {
                        val intent = Intent()
                            .putExtra("imageId", image.imageId)
                        this@EditActivity.setResult(Activity.RESULT_OK, intent)
                        this@EditActivity.finish()
                    }
            }
        }
    }
}