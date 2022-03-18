/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield.
 * Updated 2021 by Temitope Adeosun, using Kotlin with MVVM and LiveData implementation
 * All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.oak.com4510.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class Temperature(context: Context) {
    private val TEMPERATURE_READING_FREQ_MICRO_SEC: Int = 30000
    private var samplingRateInMicroSec: Long = TEMPERATURE_READING_FREQ_MICRO_SEC.toLong()
    private var samplingRateInNanoSec: Long = samplingRateInMicroSec * 10000
    private var timePhoneWasLastRebooted: Long = 0
    private var lastReportTime: Long = 0

    private lateinit var accelerometer: Accelerometer
    private var sensorManager: SensorManager?
    private var temperatureSensor: Sensor
    private var temperatureEventListener: SensorEventListener? = null
    private var _isStarted = true
    val isStarted: Boolean
        get() {return _isStarted}

    var temperatureReading: MutableLiveData<Float> = MutableLiveData<Float>()


    init{
        // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
        timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime()

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!!

        /**
         * this inits the listener and establishes the actions to take when a sensor is available
         * It is not registere to listen at this point, but makes sure the object is available to
         * listen when registered.
         */
        temperatureEventListener  = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val diff = event.timestamp - this@Temperature.lastReportTime
                // time is in nanoseconds it represents the set reference times the first time we come here
                // set event timestamp to current time in milliseconds
                // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
                // the following operation avoids reporting too many events too quickly - the sensor may always
                // misbehave and start sending data very quickly

                if (diff >= this@Temperature.samplingRateInNanoSec) {
                    val actualTimeInMseconds =
                        this@Temperature.timePhoneWasLastRebooted + (event.timestamp / 1000000.0).toLong()
                    // In this example, only updating the LiveData (hence UI gets update),
                    // when there is a new pressue value - no need for a UI update otherwise
                    if(temperatureReading.value != event.values[0]){temperatureReading.value = event.values[0]}
                    this@Temperature.lastReportTime = event.timestamp
                    // if we have not see any movement on the side of the accelerometer, let's stop
                    val timeLag = actualTimeInMseconds - accelerometer.getLastReportTime()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    companion object {
        private val TAG = Temperature::class.java.simpleName

        /**
         * this is used to stop the temperature if we have not seen any movement in the last 20 seconds
         */
        private const val STOPPING_THRESHOLD = 20000.toLong()
    }

    /**
     * it starts the pressure monitoring and updates the _isStarted status flag
     * @param accelerometer
     */
    fun startTemperatureSensing(accelerometer: Accelerometer) {
        this.accelerometer = accelerometer
        sensorManager?.let {
            // if the sensor is null,then mSensorManager is null and we get a crash
            Log.d(TAG, "Starting listener")
            // delay is in microseconds (1millisecond=1000 microseconds)
            // it does not seem to work though
            //stopTemperature();
            // otherwise we stop immediately because
            it.registerListener(
                temperatureEventListener,
                temperatureSensor,
                samplingRateInMicroSec.toInt()
            )
            _isStarted = true
        }
    }

    /**
     * this stops the temperature and updates the _isStarted status flag
     */
    fun stopTemperatureSensing() {
        sensorManager?.let {
            Log.d(TAG, "Stopping listener")
            try {
                it.unregisterListener(temperatureEventListener)
                _isStarted = false
            } catch (e: Exception) {
                // probably already unregistered
                Log.d(Accelerometer.TAG, "failed to unregister sensor, probably not running already")
            }
        }
    }


}