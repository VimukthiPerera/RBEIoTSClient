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
 * This class reads the Humidity readings using the inbuilt Environment Sensor
 * of android device
 */
public class HumiditySensorService implements SensorEventListener {

    private boolean started;
    private static TaskManager taskManager;
    private SensorManager mSensorManager;
    private Sensor mHumidity=null;
    private static HumiditySensorService mHumiditySensorServiceInstance = null;


    //to use a single siddhi manager instance
    public static HumiditySensorService getInstance(TaskManager taskManager1) {
        if(mHumiditySensorServiceInstance == null) {
            taskManager = taskManager1;
            mHumiditySensorServiceInstance = new HumiditySensorService(taskManager.context);
        }
        return mHumiditySensorServiceInstance;
    }

    HumiditySensorService(Context context) {
        startHumiditySensorService(context);
    }

    /**
     * Start the TYPE_RELATIVE_HUMIDIITY sensor by passing context
     * @param mContext
     */
    void startHumiditySensorService(Context mContext) {

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        if(mHumidity==null)
        {
            Toast.makeText(mContext, "No In built Humidity Sensor in your device!!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);
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
     * update the mHumidityValue variable when there is a sensor value change.
     */
    @Override
    public void onSensorChanged(final SensorEvent event) {
        taskManager.sendHumidityData(event.values[0]);
    }

    /**
     * Stop the sensor
     */
    void stopHumiditySensorService()
    {
        mSensorManager.unregisterListener(this);
        synchronized (this) {
            started = false;
        }
    }

    public boolean isStarted(){
        return started;
    }
}

