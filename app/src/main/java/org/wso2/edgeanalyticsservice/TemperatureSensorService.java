/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.edgeanalyticsservice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This class reads the Temperature readings using the inbuilt Environment Sensor
 * of android device
 */
public class TemperatureSensorService implements SensorEventListener {

    private boolean started;
    private static TaskManager taskManager;
    private SensorManager mSensorManager;
    private Sensor mTemparature=null;
    private static TemperatureSensorService mTemperatureSensorServiceInstance = null;

    public static TemperatureSensorService getInstance(TaskManager taskManager1) {
        if(mTemperatureSensorServiceInstance == null) {
            taskManager = taskManager1;
            mTemperatureSensorServiceInstance = new TemperatureSensorService(taskManager.context);
        }
        return mTemperatureSensorServiceInstance;
    }

    TemperatureSensorService(Context context) {
        startTemperatureSensorService(context);
    }

    /**
     * Start the TYPE_AMBIENT_TEMPERATURE sensor by passing context
     * @param mContext
     */
    void startTemperatureSensorService(Context mContext) {

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mTemparature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if(mTemparature==null)
        {
            Toast.makeText(mContext, "No In built Temperature Sensor in your device!!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mSensorManager.registerListener(this, mTemparature, SensorManager.SENSOR_DELAY_NORMAL);
        }
        synchronized (this) {
            started = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo.Assumed as the accuracy is not changing here.
    }

    /**
     * update the mTemperatureValue variable when there is a sensor value change.
     */
    @Override
    public void onSensorChanged(final SensorEvent event) {
        taskManager.sendTemperatureData(event.values[0]);
    }

    /**
     * Stop the sensor
     */
    void stopTemperatureSensorService()
    {
        mSensorManager.unregisterListener(this);
        synchronized (this) {
            started = false;
        }
    }

    public boolean isStarted() {
        return started;
    }
}

