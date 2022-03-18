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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Accelerometer(context: Context, barometer: Barometer, temperature: Temperature) {

    private var timePhoneWasLastRebooted: Long = 0
    private var lastReportTime: Long = 0

    private val barometer: Barometer
    private val temperature: Temperature

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null

    var accelerometerReading = MutableLiveData<Pair<String, Map<String, Float>>>()
    var isStarted = MutableLiveData<Boolean>()

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    init {
        isStarted.value = true
        // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
        timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime()

        this.barometer = barometer
        this.temperature = temperature
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        /**
         * this inits the listener and establishes the actions to take when a sensor is available
         * It is not registere to listen at this point, but makes sure the object is available to
         * listen when registered.
         */
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val actualTimeInMseconds =
                    timePhoneWasLastRebooted + (event.timestamp / 1000000.0).toLong()

                // Remove the Acceleration due to gravity component.
                // The alternative to this is to use Sensor.TYPE_LINEAR_ACCELERATION
                val x = abs(event.values[0]).let { if(it >=9.8) it-9.8 else it }.toFloat()
                val y = abs(event.values[1]).let { if(it >=9.8) it-9.8 else it }.toFloat()
                val z = abs(event.values[2]).let { if(it >=9.8) it-9.8 else it }.toFloat()
                val timeString = Utilities.mSecsToString(actualTimeInMseconds)

                val angularMagnitude =sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                var deltaX = abs(lastX - x)
                var deltaY = abs(lastY - y)
                var deltaZ = abs(lastZ - y)

                // if the change is below 2, it is just plain noise
                if (deltaX < 2) deltaX = 0f
                if (deltaY < 2) deltaY = 0f
                if (deltaZ < 2) deltaZ = 0f
                if (deltaX > 0 && deltaY > 0 && deltaZ > 0) {
                    Log.i(
                        TAG,
                        "$timeString: significant motion detected - x: $deltaX, y: $deltaY, z:$deltaZ"
                    )
                    if (!barometer.isStarted) barometer.startBarometerSensing(this@Accelerometer)
                    setLastReportTime(actualTimeInMseconds)

                    if (!temperature.isStarted) temperature.startTemperatureSensing(this@Accelerometer)
                    setLastReportTime(actualTimeInMseconds)
                }
                lastX = x
                lastY = y
                lastZ = z
                val accelerometerTriple= mapOf("x" to lastX, "y" to lastY, "z" to lastZ, "magnitude" to angularMagnitude)
                accelerometerReading.value = Pair(timeString, accelerometerTriple)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    companion object {
        val TAG = Accelerometer::class.java.simpleName
    }

    /**
     * it starts the accelerometer monitoring
     */
    fun startAccelerometerSensing() {
        // if the sensor is null,then mSensorManager is null and we get a crash
        sensorManager?.also{
            Log.d(TAG, "starting listener")
            // THE ACCELEROMETER receives as frequency a predefined subset of timing
            // https://developer.android.com/reference/android/hardware/SensorManager
            it.registerListener(
                sensorEventListener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        isStarted.value = true
    }

    /**
     *  it stops the accelerometer monitorning and pressure monitoring if it is still running
     */
    fun stopAccelerometerSensing() {
        sensorManager?.also {
            Log.d(TAG, "Stopping listener")
            try {
                it.unregisterListener(sensorEventListener)
            } catch (e: Exception) {
                // probably already unregistered
                Log.d(TAG, "failed to unregister sensor, probably not running already")
            }
        }
        isStarted.value = false
        // remember to stop the barometer
        this@Accelerometer.barometer.stopBarometerSensing()
    }

    fun getLastReportTime(): Long {
        return lastReportTime
    }

    fun setLastReportTime(lastReportTime: Long) {
        this.lastReportTime = lastReportTime
    }
}