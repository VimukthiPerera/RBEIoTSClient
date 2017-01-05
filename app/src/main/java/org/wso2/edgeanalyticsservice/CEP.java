package org.wso2.edgeanalyticsservice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 12/12/15.
 */
public class CEP {
    /**
     * The indices of the Object array which stores the data that is sent from the TaskManager
     */
    public static final int LIGHT_INTENSITY = 0;
    public static final int LATITUDE = 1;
    public static final int LONGITUDE = 2;
    public static final int HUMIDITY = 3;
    public static final int TEMPERATURE = 4;
    /**
     * Object array to store the data that is sent from the TaskManager
     */
    private static final Object[] data = new Object[10];
    private static Context context;
    private static CEP instance;
    private static SiddhiManager mSiddhiManager;

    private String TAG = "CEP";
    /**
     * Maps to store the packages which use the execution plans
     */
    private Map<String, ExecutionPlanRuntime> packageVsExecutionPlan = new Hashtable<>();
    private Map<ExecutionPlanRuntime, Collection<Stream>> executionPlanRuntimes = new Hashtable<>();
    private Map<ExecutionPlanRuntime, Collection<Stream>> subscribedExecutionPlanRuntimes = new Hashtable<>();
    //    private List<Stream> allStreams = new ArrayList<>();
    private Map<String, Map<String, Stream>> packageVsStreams = new Hashtable<>();
    private Map<String, Set<Integer>> packageVsInputTypes = new Hashtable<>();
    private Map<String, List<Callback>> packageVsCallback = new Hashtable<>();

    /**
     * To get only one instance of the CEP
     */
    public static synchronized CEP getInstance(Context context1) {
        if (instance == null) {
            context = context1;
            mSiddhiManager = new SiddhiManager();
            instance = new CEP();
        }
        return instance;
    }

    /**
     * Send data to all respective execution plan runtimes
     */
    private void sendToStreams() {
        for (ExecutionPlanRuntime e : subscribedExecutionPlanRuntimes.keySet()) {
            for (Stream stream : subscribedExecutionPlanRuntimes.get(e)) {
                String streamName = stream.streamName;
                try {
                    Object[] values = new Object[stream.inputTypes.length];
                    int index = 0;
                    boolean dataReady = true;
                    for (int input : stream.inputTypes) {
                        if (data[input] == null) {
                            dataReady = false;
                            break;
                        } else {
                            values[index] = data[input];
                        }
                        index++;
                    }
                    if (dataReady) {
                        try {
                            Log.d(TAG, "Sending Data to Stream " + streamName + Arrays.deepToString(values));
                            e.getInputHandler(streamName).send(values);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (NullPointerException npe) {
                    //TODO add proper catch for input type-less clients
                }
            }
        }
    }

    /**
     * Various sensor inputs from the TaskManager
     */
    public void receiveLightIntensityData(double data) {
        this.data[LIGHT_INTENSITY] = data;
        try {
            sendToStreams();
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, e.toString());
        }
    }

    public void receiveLocationData(double[] data) {
        this.data[LATITUDE] = data[0];
        this.data[LONGITUDE] = data[1];
        try {
            sendToStreams();
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, e.toString());
        }
    }

    public void receiveHumidityData(double data) {
        this.data[HUMIDITY] = data;
        try {
            sendToStreams();
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, e.toString());
        }
    }

    public void receiveTemperatureData(double data) {
        this.data[TEMPERATURE] = data;
        try {
            sendToStreams();
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, e.toString());
        }
    }

    /**
     * Recieve data from custom streams
     */
    public void receiveCustomData(String id, String stream, Object[] data) {
        try {
            packageVsExecutionPlan.get(id).getInputHandler(stream).send(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create and start the execution plan runtime based on the definitions
     * and stream mappings are done
     */
    public void createExecutionPlanRuntime(String id, String executionPlan, Collection<Stream> streams) {
        ExecutionPlanRuntime e = packageVsExecutionPlan.get(id);
        if (e != null) {
            e.shutdown();
        }
        //TODO remove log
        Log.d(TAG, id + " " + executionPlan);
        ExecutionPlanRuntime executionPlanRuntime = mSiddhiManager.createExecutionPlanRuntime(executionPlan);
        packageVsExecutionPlan.put(id, executionPlanRuntime);
        subscribedExecutionPlanRuntimes.put(executionPlanRuntime, streams);
        executionPlanRuntimes.put(executionPlanRuntime, streams);
        executionPlanRuntime.start();
        reInitializeCallbacks(id);
    }

    private synchronized void reInitializeCallbacks(String id) {
        List<Callback> callbackList = packageVsCallback.get(id);
        if (callbackList != null) {
            List<Callback> callbacks = new ArrayList<>();
            callbacks.addAll(callbackList);
            callbackList.clear();
            for (Callback c : callbacks) {
                Log.e(TAG, c.toString());
                String type = c.getType();
                String source = c.getSource();
                String target = c.getTarget();
                String receiver = c.getReceiver();
                String receiverPkg = c.getReceiverPkg();
                switch (type) {
                    case Callback.STREAM_STATIC:
                        addStreamCallback(source, target, receiver, receiverPkg);
                        break;
                    case Callback.STREAM_DYNAMIC:
                        addDynamicStreamCallback(source, target, receiver);
                        break;
                    case Callback.QUERY_STATIC:
                        addQueryCallback(source, target, receiver, receiverPkg);
                        break;
                    case Callback.QUERY_DYNAMIC:
                        addDynamicQueryCallback(source, target, receiver);
                        break;
                }
            }
        }
    }

    /**
     * Create and start the execution plan runtime based on the definitions
     * and stream mappings are done
     */
    public void subscribeExecutionPlanRuntime(String id, String executionPlan, Collection<Stream> streams) {
        ExecutionPlanRuntime e = packageVsExecutionPlan.get(id);
        if (e != null) {
            e.shutdown();
        }
        //TODO remove log
        Log.d("CEP", executionPlan);
        ExecutionPlanRuntime executionPlanRuntime = mSiddhiManager.createExecutionPlanRuntime(executionPlan);
        packageVsExecutionPlan.put(id, executionPlanRuntime);
        subscribedExecutionPlanRuntimes.put(executionPlanRuntime, streams);
        executionPlanRuntimes.put(executionPlanRuntime, streams);
        packageVsInputTypes.put(id, new HashSet<Integer>());
        executionPlanRuntime.start();
        startServices(streams, id);
        reInitializeCallbacks(id);
    }

    public void addStream(Stream stream, String packageName) {
        if (packageVsStreams.get(packageName) == null) {
            packageVsStreams.put(packageName, new Hashtable<String, Stream>());
        }
        packageVsStreams.get(packageName).put(stream.streamName, stream);
    }

    public void removeStream(Stream stream, String packageName) {
        try {
            packageVsStreams.get(packageName).remove(stream.streamName);
        } catch (NullPointerException e) {
            Log.e(TAG, packageName + " has no streams defined");
        }
    }

    public List<Stream> getAllStreams() {
        List<Stream> allStreams = new ArrayList<>();
        for (Map m : packageVsStreams.values()) {
            allStreams.addAll(m.values());
        }
        return allStreams;
    }

    private void startServices(Collection<Stream> streams, String id) {
        for (Stream s : streams) {
            if (s.inputTypes != null) {
                for (int input : s.inputTypes) {
                    packageVsInputTypes.get(id).add(input);
                    switch (input) {
                        case LIGHT_INTENSITY:
                            TaskManager.startLightIntensitySensorService(context);
                            break;
                        case LATITUDE:
                            TaskManager.startLocationSensorService(context);
                            break;
                        case LONGITUDE:
                            TaskManager.startLocationSensorService(context);
                            break;
                        case TEMPERATURE:
                            TaskManager.startTemperatureSensorService(context);
                            break;
                        case HUMIDITY:
                            TaskManager.startHumiditySensorService(context);
                            break;
                        default:
                            continue;
                    }
                }
            }
        }
    }

    //TODO find a way to persist Callbacks even when execution plan restarts. (Almost Done)

    /**
     * Add callback to a stream
     */
    public void addStreamCallback(final String id, String stream, final String receiver, final String receiverPkg) {
        if (packageVsCallback.get(id) == null) {
            packageVsCallback.put(id, new ArrayList<Callback>());
        }
        packageVsCallback.get(id).add(new Callback(Callback.STREAM_STATIC, id, stream, receiver, receiverPkg));
        ExecutionPlanRuntime executionPlanRuntime = packageVsExecutionPlan.get(id);
        executionPlanRuntime.addCallback(stream, new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                Intent intent = new Intent(receiver);
                intent.setPackage(receiverPkg);
                intent.putExtra("event", Arrays.deepToString(events));
                context.sendBroadcast(intent);
            }
        });
    }

    /**
     * Add dynamic callback to a stream
     */
    public void addDynamicStreamCallback(final String source, final String stream, final String receiver) {
        if (packageVsCallback.get(source) == null) {
            packageVsCallback.put(source, new ArrayList<Callback>());
        }
        packageVsCallback.get(source).add(new Callback(Callback.STREAM_DYNAMIC, source, stream, receiver, receiver));
        ExecutionPlanRuntime executionPlanRuntime = packageVsExecutionPlan.get(source);
        executionPlanRuntime.addCallback(stream, new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                Intent intent = new Intent(stream);
                intent.setPackage(receiver);
                intent.putExtra("event", Arrays.deepToString(events));
                context.sendBroadcast(intent);
            }
        });
    }

    /**
     * Add callback to a query
     */
    public void addQueryCallback(final String id, String queryName, final String receiver, final String receiverPkg) {
        if (packageVsCallback.get(id) == null) {
            packageVsCallback.put(id, new ArrayList<Callback>());
        }
        packageVsCallback.get(id).add(new Callback(Callback.QUERY_STATIC, id, queryName, receiver, receiverPkg));
        ExecutionPlanRuntime executionPlanRuntime = packageVsExecutionPlan.get(id);
        Log.d(TAG, "Source: " + id + ", Receiver: " + receiver + ", Query Name: " + queryName);
        executionPlanRuntime.addCallback(queryName, new QueryCallback() {
            @Override
            public void receive(long l, Event[] events, Event[] events1) {
                Intent intent = new Intent(receiver);
                intent.setPackage(receiverPkg);
                StringBuilder sb = new StringBuilder();
                sb.append("Events{ @timeStamp = ").append(l).append(", inEvents = ").append(Arrays.deepToString(events)).append(", RemoveEvents = ").append(Arrays.deepToString(events1)).append(" }");
                intent.putExtra("event", sb.toString());
                context.sendBroadcast(intent);
                Log.d(TAG, sb.toString());
            }
        });
    }

    /**
     * Add dynamic callback to a query
     */
    public void addDynamicQueryCallback(final String source, final String queryName, final String receiver) {
        if (packageVsCallback.get(source) == null) {
            packageVsCallback.put(source, new ArrayList<Callback>());
        }
        packageVsCallback.get(source).add(new Callback(Callback.QUERY_DYNAMIC, source, queryName, receiver, receiver));
        ExecutionPlanRuntime executionPlanRuntime = packageVsExecutionPlan.get(source);
        Log.d(TAG, "Source: " + source + ", Receiver: " + receiver + ", Query Name: " + queryName);
        executionPlanRuntime.addCallback(queryName, new QueryCallback() {
            @Override
            public void receive(long l, Event[] events, Event[] events1) {
                Intent intent = new Intent(queryName);
                intent.setPackage(receiver);
                StringBuilder sb = new StringBuilder();
                sb.append("Events{ @timeStamp = ").append(l).append(", inEvents = ").append(Arrays.deepToString(events)).append(", RemoveEvents = ").append(Arrays.deepToString(events1)).append(" }");
                intent.putExtra("event", sb.toString());
                context.sendBroadcast(intent);
                Log.d(TAG, sb.toString());
            }
        });
    }

    /**
     * Shutdown CEP instance for a certain package
     */
    public void shutdown(String id) {
        try {
            packageVsExecutionPlan.get(id).shutdown();
            TaskManager.stop(packageVsInputTypes.get(id).toArray(new Integer[packageVsInputTypes.get(id).size()]));
        } catch (NullPointerException e) {
            Log.e(TAG, "Something went wrong shutting down " + id);
            Log.e(TAG, e.getMessage());
        }
        packageVsExecutionPlan.remove(id);
        subscribedExecutionPlanRuntimes.remove(id);
        executionPlanRuntimes.remove(id);
        packageVsCallback.remove(id);
        packageVsInputTypes.remove(id);
    }

    /**
     * Shutdown all streams and the siddhi manager
     */
    public void shutdown() {
        for (ExecutionPlanRuntime e : executionPlanRuntimes.keySet()) {
            e.shutdown();
            TaskManager.shutdown();
            mSiddhiManager.shutdown();
            instance = null;
        }
    }

}
