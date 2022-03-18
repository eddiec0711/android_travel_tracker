package uk.ac.shef.oak.com4510.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import uk.ac.shef.oak.com4510.database.data.Location
import androidx.room.Room.databaseBuilder
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Trip

@Database(entities = [Trip::class, Location::class, Image::class], exportSchema = false, version = 1)
abstract class TripRoomDatabase: RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        // marking the instance as volatile to ensure atomic access to the variable
        private var INSTANCE: TripRoomDatabase? = null
        private val mutex = Mutex()

        fun getDatabase(context: Context): TripRoomDatabase? {
            if (INSTANCE == null) {
                runBlocking {
                    withContext(Dispatchers.Default) {
                        // add lock to MyRoomDatabase class
                        mutex.withLock(TripRoomDatabase::class) {
                            INSTANCE = databaseBuilder(
                                context.applicationContext,
                                TripRoomDatabase::class.java, "trip_database"
                            )   // Wipes and rebuilds instead of migrating if no Migration object.
                                // Migration is not part of this codelab.
                                .fallbackToDestructiveMigration()
                                .addCallback(sRoomDatabaseCallback)
                                .build()
                        }
                    }
                }
            }
            return INSTANCE
        }

        /**
         * Override the onOpen method to populate the database.
         * For this sample, we clear the database every time it is created or opened.
         *
         * If you want to populate the database only when the database is created for the 1st time,
         * override RoomDatabase.Callback()#onCreate
         */
        private val sRoomDatabaseCallback: RoomDatabase.Callback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // do any init operation about any initialisation here
            }
        }
    }
}