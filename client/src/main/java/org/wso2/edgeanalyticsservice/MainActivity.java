package org.wso2.edgeanalyticsservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.wso2.vimu.client.R;

public class MainActivity extends AppCompatActivity {
    private IEdgeAnalyticsService mIEdgeAnalyticsService;
    private TextView mTextView;
    private final String packageName = getPackageName();
    private static final String temperatureStream = "define stream TemperatureStream(sensorId string, temperature float);";
    private static final String outputStream ="define stream outputStream(sensorId string)";
    private static final String newExecutionPlan = "define stream TemperatureStream(sensorId string, temperature float);\n" +
            "from TemperatureStream[temperature>98.6] \n" +
            "select sensorId \n" +
            "insert into outputStream;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        Intent serviceIntent = new Intent().setComponent(new ComponentName("org.wso2.edgeanalyticsservice",
                        "org.wso2.edgeanalyticsservice.EdgeAnalyticsService"));
        mTextView.append("Intent created..\n");
        startService(serviceIntent);
        mTextView.append("service started...\n");
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mTextView.append("Service binded!\n");
           // mIEdgeAnalyticsService = IEdgeAnalyticsService.Stub.asInterface(service);
           // receive();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mIEdgeAnalyticsService = null;
            // This method is only invoked when the service quits from the other end or gets killed
            // Invoking exit() from the AIDL interface makes the Service kill itself, thus invoking this.
            mTextView.append("Service disconnected.\n");
        }
    };

    public void receive() {
        try {
            mIEdgeAnalyticsService.addStream(temperatureStream,packageName);
            mIEdgeAnalyticsService.addStream(outputStream,packageName);
            mTextView.setText("Output: " + mIEdgeAnalyticsService.getAllStreams());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
