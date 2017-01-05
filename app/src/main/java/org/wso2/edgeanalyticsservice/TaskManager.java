package org.wso2.edgeanalyticsservice;

import android.content.Context;

/**
 * Created by root on 12/12/15.
 */
public class TaskManager {
    public static Context context;
    private static TaskManager instance;
    private static CEP mCep;
    private static LightIntensitySensorService lightIntensitySensorService;
    private static LocationSystemService locationSystemService;
    private static HumiditySensorService humiditySensorService;
    private static TemperatureSensorService temperatureSensorService;
    private static int lightIntensityClientsCount = 0, locationClientCount = 0, humidityClientCount = 0, temperatureClientCount = 0;


    public static synchronized TaskManager getInstance(Context context1) {
        if (instance == null) {
            context = context1;
            instance = new TaskManager();
            mCep = CEP.getInstance(context1);
        }
        return instance;
    }

    public static void startLightIntensitySensorService(Context context1) {
        lightIntensityClientsCount++;
        lightIntensitySensorService = LightIntensitySensorService.getInstance(getInstance(context1));
        if (!lightIntensitySensorService.isStarted()) {
            lightIntensitySensorService.startLightIntensitySensorService(context1);
        }
    }

    public static void startLocationSensorService(Context context1) {
        locationClientCount++;
        locationSystemService = LocationSystemService.getInstance(getInstance(context1));
        if (!locationSystemService.isStarted()) {
            locationSystemService.startLocationService(context1);
        }
    }

    public static void startHumiditySensorService(Context context1) {
        humidityClientCount++;
        humiditySensorService = HumiditySensorService.getInstance(getInstance(context1));
        if (!humiditySensorService.isStarted()) {
            humiditySensorService.startHumiditySensorService(context1);
        }
    }

    public static void startTemperatureSensorService(Context context1) {
        temperatureClientCount++;
        temperatureSensorService = TemperatureSensorService.getInstance(getInstance(context1));
        if (!temperatureSensorService.isStarted()) {
            temperatureSensorService.startTemperatureSensorService(context1);
        }
    }

    public static void stop(Integer[] services) {
        for (int service : services) {
            switch (service) {
                case CEP.LIGHT_INTENSITY:
                    if (lightIntensitySensorService.isStarted()) {
                        lightIntensityClientsCount--;
                        if (lightIntensityClientsCount <= 0) {
                            lightIntensitySensorService.stopLightIntensitySensorService();
                            lightIntensityClientsCount = 0;
                        }
                    }
                    break;
                case CEP.LONGITUDE:
                    if (locationSystemService.isStarted()) {
                        locationClientCount--;
                        if (locationClientCount <= 0) {
                            locationSystemService.stopLocationService();
                            locationClientCount = 0;
                        }
                    }
                    break;
                case CEP.LATITUDE:
                    if (humiditySensorService.isStarted()) {
                        humidityClientCount--;
                        if (humidityClientCount <= 0) {
                            humiditySensorService.stopHumiditySensorService();
                            humidityClientCount = 0;
                        }
                    }
                    break;
                case CEP.TEMPERATURE:
                    if (temperatureSensorService.isStarted()) {
                        temperatureClientCount--;
                        if (temperatureClientCount <= 0) {
                            temperatureSensorService.stopTemperatureSensorService();
                            temperatureClientCount = 0;
                        }
                    }
                    break;
            }
        }
    }

    public static void shutdown() {
        if (lightIntensitySensorService.isStarted())
            lightIntensitySensorService.stopLightIntensitySensorService();
        if (locationSystemService.isStarted())
            locationSystemService.stopLocationService();
        if (humiditySensorService.isStarted())
            humiditySensorService.stopHumiditySensorService();
        if (temperatureSensorService.isStarted())
            temperatureSensorService.stopTemperatureSensorService();
    }

    public void sendLightIntensityData(double data) {
        mCep.receiveLightIntensityData(data);
    }

    public void sendLocationData(double[] data) {
        mCep.receiveLocationData(data);
    }

    public void sendHumidityData(double data) {
        mCep.receiveHumidityData(data);
    }

    public void sendTemperatureData(double data) {
        mCep.receiveTemperatureData(data);
    }

}
