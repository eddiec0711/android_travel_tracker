package uk.ac.shef.oak.com4510.view.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.view.main.MainActivity
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel

class SearchActivity: AppCompatActivity() {
    private var tripViewModel: TripViewModel? = null
    private lateinit var mAdapter: SearchImageAdapter
    private lateinit var mRecyclerView: RecyclerView
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageId = result.data?.getIntExtra("imageId", -1)!!
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]

        // set up the RecyclerView
        mRecyclerView = findViewById(R.id.search_recycler_view)
        mRecyclerView.layoutManager = GridLayoutManager(this, 1)

        this.tripViewModel!!.getImages()!!.observe(this,
            //  create observer, whenever the value is changed this func will be called
            { images ->
                mAdapter = SearchImageAdapter(this, images)
                mRecyclerView.adapter = mAdapter
            }
        )

        // back to home page
        val btnEnd: Button = findViewById(R.id.button_home)
        btnEnd.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            this.startActivity(myIntent)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val menuItem = menu.findItem(R.id.app_bar_search)
        val searchView = menuItem.actionView as SearchView

        // apply search function to search view
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query:String?): Boolean{
                if(query != null){
                    updateAdapter(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * search images using a filter query
     * update adapter to render new list of images
     *
     * @params query - keyword used to search for images
     * @return void
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(query: String) {
        this.tripViewModel!!.searchImage(query)!!.observe(this,
            //  create observer, whenever the value is changed this func will be called
            { images ->
                mAdapter.setItems(images)
                mAdapter.notifyDataSetChanged()
            }
        )
    }
}