package uk.ac.shef.oak.com4510.view.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.utility.PermissionChecker
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel
import kotlinx.coroutines.*
import pl.aprilapps.easyphotopicker.*
import uk.ac.shef.oak.com4510.view.inspect.MapBrowseActivity
import uk.ac.shef.oak.com4510.view.search.SearchActivity
import uk.ac.shef.oak.com4510.view.trip.MapActivity
import android.widget.Spinner
import android.widget.AdapterView


class MainActivity : AppCompatActivity() {

    private var tripViewModel: TripViewModel? = null
    private lateinit var mAdapter: SectionAdapter
    private lateinit var mRecyclerView: RecyclerView
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageId = result.data?.getIntExtra("imageId", -1)!!
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        this.tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]

        renderImages()
        addButtonListeners()
        setupSpinner()

        // required by Android 6.0 +
        PermissionChecker.checkPermissions(applicationContext, this@MainActivity)
    }

    /**
     * set up the RecyclerView
     * and populate with data from db
     * @return void
     */
    private fun renderImages() {
        mRecyclerView = findViewById(R.id.section_recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        this.tripViewModel!!.getTripsWithLocations()!!.observe(this,
            //  create observer, whenever the value is changed this func will be called
            { trips ->
                mAdapter = SectionAdapter(trips)
                mRecyclerView.adapter = mAdapter
            }
        )
    }

    /**
     * add button navigating listeners
     * 1. add trip to start recording new trip
     * 2. view map to view images on map
     * 3. search to search images with keyword
     * @return void
     */
    private fun addButtonListeners() {
        val btnAddTrip: Button = findViewById(R.id.button_add_trip)
        btnAddTrip.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(this, MapActivity::class.java)
            this.startActivity(myIntent)
        })

        val btnViewMap: Button = findViewById(R.id.button_view_map)
        btnViewMap.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(this, MapBrowseActivity::class.java)
            this.startActivity(myIntent)
        })

        val btnSearch: FloatingActionButton = findViewById(R.id.button_search_image)
        btnSearch.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(this, SearchActivity::class.java)
            this.startActivity(myIntent)
        })
    }

    /**
     * call when sort item selected
     * sort adapters based on item selected
     * @param position - position of the menu item selected, 0 - trip; 1 - date
     * @return void
     */
    private fun selectSort(position: Int) {
        this@MainActivity.tripViewModel!!.getTripsWithLocations()!!.observe(this@MainActivity,
            //  create observer, whenever the value is changed this func will be called
            { trips ->
                if (trips != null) {
                    when (position) {
                        0 -> {
                            this@MainActivity.mAdapter.setItems(trips, false)
                            mAdapter.notifyDataSetChanged()
                        }
                        1 -> {
                            val sortedList = mutableListOf(trips[0])
                            for (i in 1 until trips.size) {
                                if (trips[i].trip.tripStartTime == sortedList.last().trip.tripStartTime) {
                                    sortedList.last().locations += trips[i].locations
                                } else {
                                    sortedList.add(trips[i])
                                }
                            }
                            this@MainActivity.mAdapter.setItems(sortedList, true)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        )
    }

    /**
     * setup dropdown menu for sorting grid
     * @return void
     */
    private fun setupSpinner() {
        val dropdown: Spinner = findViewById(R.id.spinner)
        val items = listOf("trip", "date")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter

        dropdown.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectSort(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        })
    }
}