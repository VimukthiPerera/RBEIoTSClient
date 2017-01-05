package org.wso2.edgeanalyticsservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EdgeAnalyticsService extends Service {

    private CEP mCep;
    private TaskManager taskManager;
    private Map<String, ExecutionPlanDefinition> executionPlanDefinitionMap = new Hashtable<>();
    IEdgeAnalyticsService edgeAnalyticsService = new IEdgeAnalyticsService.Stub() {

        @Override
        public void addStream(String streamDefinition, String packageName) throws RemoteException {
            if (executionPlanDefinitionMap.get(packageName) == null) {
                executionPlanDefinitionMap.put(packageName, new ExecutionPlanDefinition());
            }
//            mCep.removeStream(executionPlanDefinitionMap.get(packageName).removeStream(Stream.parseStream(streamDefinition).streamName));
            mCep.addStream(Stream.parseStream(streamDefinition), packageName);
            executionPlanDefinitionMap.get(packageName).addStream(streamDefinition);
        }

        @Override
        public void removeStream(String streamName, String packageName) throws RemoteException {
            try {
                mCep.removeStream(executionPlanDefinitionMap.get(packageName).removeStream(streamName), packageName);
            } catch (NullPointerException e) {
                throw new RemoteException(packageName + " has not added any streams");
            }
        }

        @Override
        public void subscribeStreamToData(String packageName, String streamDefinition, int[] inputTypes) throws RemoteException {
            if (executionPlanDefinitionMap.get(packageName) == null) {
                executionPlanDefinitionMap.put(packageName, new ExecutionPlanDefinition());
            }
            mCep.addStream(Stream.parseStream(streamDefinition), packageName);
            executionPlanDefinitionMap.get(packageName).subscribeStreamToData(streamDefinition, inputTypes);
        }

        @Override
        public void addQuery(String queryDefinition, String packageName) throws RemoteException {
            if (executionPlanDefinitionMap.get(packageName) == null) {
                executionPlanDefinitionMap.put(packageName, new ExecutionPlanDefinition());
            }
            executionPlanDefinitionMap.get(packageName).addQuery(queryDefinition);
        }

        @Override
        public void removeQuery(String queryName, String packageName) throws RemoteException {
            try {
                executionPlanDefinitionMap.get(packageName).removeQuery(queryName);
            } catch (NullPointerException e) {
                throw new RemoteException(packageName + " has not added any queries");
            }
        }

        @Override
        public List<Stream> getAllStreams() throws RemoteException {
            return mCep.getAllStreams();
        }

        @Override
        public void validateExecutionPlan(String packageName) throws RemoteException {
            //TODO figure something out
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void startExecutionPlan(String packageName) throws RemoteException {
            mCep.createExecutionPlanRuntime(packageName,
                    executionPlanDefinitionMap.get(packageName).getExecutionPlan(),
                    executionPlanDefinitionMap.get(packageName).streams.values());
        }

        @Override
        public void subscribeExecutionPlan(String packageName) throws RemoteException {
            mCep.subscribeExecutionPlanRuntime(packageName,
                    executionPlanDefinitionMap.get(packageName).getExecutionPlan(),
                    executionPlanDefinitionMap.get(packageName).streams.values());
        }

        @Override
        public void addQueryCallback(String queryName, String receiver, String packageName, String ownPackageName) throws RemoteException {
            mCep.addQueryCallback(packageName, queryName, receiver, ownPackageName);
        }

        @Override
        public void addDynamicQueryCallback(String queryName, String packageName, String receiver) throws RemoteException {
            mCep.addDynamicQueryCallback(packageName, queryName, receiver);
        }

        @Override
        public void addStreamCallback(String stream, String receiver, String packageName, String ownPackageName) throws RemoteException {
            mCep.addStreamCallback(packageName, stream, receiver, ownPackageName);
        }

        @Override
        public void addDynamicStreamCallback(String stream, String packageName, String reveiver) throws RemoteException {
            mCep.addDynamicStreamCallback(packageName, stream, reveiver);
        }

        @Override
        public void sendData(String id, String stream, List<String> values, List<String> types) throws RemoteException {
            Object[] x = new Object[values.size()];

            /** Identify the data types which the client sends. */
            for (int i = 0; i < values.size(); i++) {
                x[i] = 0;
                switch (types.get(i)) {
                    case "double":
                        x[i] = Double.parseDouble(values.get(i));
                        break;
                    case "float":
                        x[i] = Float.parseFloat(values.get(i));
                        break;
                    case "int":
                        x[i] = Integer.parseInt(values.get(i));
                        break;
                    case "string":
                        x[i] = values.get(i);
                        break;
                    default:
                        break;
                }
            }
            mCep.receiveCustomData(id, stream, x);
        }

        @Override
        public void stop() {
            mCep.shutdown();
        }
    };

    public EdgeAnalyticsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        mCep = CEP.getInstance(getBaseContext());
        taskManager = TaskManager.getInstance(getBaseContext());
        executionPlanDefinitionMap.put(intent.getStringExtra("package"), new ExecutionPlanDefinition());
//        executionPlanDefinition = new ExecutionPlanDefinition();
        return edgeAnalyticsService.asBinder();
    }

    @Override
    public void onDestroy() {
        mCep.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        String packageName = intent.getStringExtra("package");
        try {
            mCep.shutdown(packageName);
        } catch (Exception e) {
            //TODO Add proper try catch
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }
}
