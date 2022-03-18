/*
 * Copyright (c) 2021. This code has been developed by Temitope Adeosun, The University of Sheffield.
 * All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.oak.com4510.sensor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SensorViewModel(application: Application): AndroidViewModel(application) {

    private var barometer: Barometer = Barometer(application)
    private var temperature: Temperature = Temperature(application)
    private var accelerometer: Accelerometer = Accelerometer(application, barometer, temperature)

    /**
     * Calls the needed sensor class to start monitoring the sensor data
     */
    fun startSensing() {
        accelerometer.startAccelerometerSensing()
        barometer.startBarometerSensing(accelerometer)
        temperature.startTemperatureSensing(accelerometer)
    }

    /**
     * Calls the needed sensor class to stop monitoring the sensor data
     */
    fun stopSensing() {
        accelerometer.stopAccelerometerSensing()
        barometer.stopBarometerSensing()
        temperature.stopTemperatureSensing()
    }

    /**
     * Func that exposes the pressure as LiveData to the View object
     * @return
     */
    fun retrievePressureData(): LiveData<Float>{
        return barometer.pressureReading
    }

    fun retrieveTemperatureData(): LiveData<Float>{
        return temperature.temperatureReading
    }
}