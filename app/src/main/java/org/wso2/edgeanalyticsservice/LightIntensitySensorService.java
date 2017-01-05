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
 * This class reads the Intensity readings using the inbuilt Environment Sensor
 * of android device
 */
public class LightIntensitySensorService implements SensorEventListener {

    private boolean started;
    private static TaskManager taskManager;
    private SensorManager mSensorManager;
    private Sensor mIntensity=null;
    private static LightIntensitySensorService mLightIntensitySensorServiceInstance = null;

    //to use a single siddhi manager instance
    public static LightIntensitySensorService getInstance(TaskManager taskManager1) {
        if(mLightIntensitySensorServiceInstance == null) {
            taskManager = taskManager1;
            mLightIntensitySensorServiceInstance = new LightIntensitySensorService(taskManager.context);
        }
        return mLightIntensitySensorServiceInstance;
    }

    LightIntensitySensorService(Context context)
    {
        startLightIntensitySensorService(context);
    }

    /**
     * Start the TYPE_LIGHT sensor by passing context
     * @param mContext
     */
    void startLightIntensitySensorService(Context mContext) {

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mIntensity = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if(mIntensity==null)
        {
            Toast.makeText(mContext, "No In built Light Sensor in your device!!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mSensorManager.registerListener(this, mIntensity, SensorManager.SENSOR_DELAY_NORMAL);
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
     * update the mLightIntensitiveValue variable when there is a sensor value change.
     */
    @Override
    public void onSensorChanged(final SensorEvent event) {
        taskManager.sendLightIntensityData(event.values[0]);
    }

    /**
     * Stop the sensor by calling unregister Listner
     */
    void stopLightIntensitySensorService()
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

