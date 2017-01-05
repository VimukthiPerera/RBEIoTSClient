package org.wso2.edgeanalyticsservice;

import android.util.ArrayMap;
import android.util.Log;

import org.wso2.siddhi.query.api.execution.query.Query;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 12/12/15.
 */
public class ExecutionPlanDefinition {
    Map<String, Stream> streams = new LinkedHashMap<>();
    Map<String, String> queries = new LinkedHashMap<>();
    static int unnamedQueryNo = 0;

    public void addStream(String streamDefinition){
        String streamName = "";
        Pattern pattern = Pattern.compile("define[\\s]+stream[\\s]+(([a-z]|[A-Z])+).*");
        Matcher matcher = pattern.matcher(streamDefinition);
        while (matcher.find()) {
            streamName = matcher.group(1);
        }
        Log.e("Stream", streamDefinition+" "+streamName);
        Stream s = new Stream(streamName, streamDefinition);
        streams.put(streamName, s);
    }

    //TODO figure out what to do to derived streams eg: Output Steam in queries

    public void subscribeStreamToData(String streamDefinition, int[] inputTypes){
        String streamName = "";
        Pattern pattern = Pattern.compile("define[\\s]+stream[\\s]+(([a-z]|[A-Z])+).*");
        Matcher matcher = pattern.matcher(streamDefinition);
        while (matcher.find()) {
            streamName = matcher.group(1);
        }
//        Log.e("Stream", streamDefinition+" "+streamName);
        Stream s = new Stream(streamName, streamDefinition, inputTypes);
        streams.put(streamName, s);
    }

    public Stream removeStream(String streamName){
        return streams.remove(streamName);
    }

    public void addQuery(String queryDefinition){
        String queryName = "";
        Pattern pattern = Pattern.compile("\\(.*'(.*)'\\)");
        Matcher matcher = pattern.matcher(queryDefinition);
        while (matcher.find()) {
            queryName = matcher.group(1);
        }
        if(queryName == null || queryName.isEmpty()){
            synchronized (this){
                queryName = "UnnamedQuery"+unnamedQueryNo;
                queryDefinition = "@info(name = '"+queryName+"') "+queryDefinition;
                unnamedQueryNo++;
            }
        }
        //insert[\s]+into[\s]+([a-zA-Z]+).*
        Log.e("Query", queryDefinition+" "+queryName);
        queries.put(queryName, queryDefinition);
    }

    public String removeQuery(String queryName){
        return queries.remove(queryName);
    }

    public String getExecutionPlan(){
        StringBuilder sb = new StringBuilder();
        for(Stream stream : streams.values()){
            sb.append(stream.streamDefinition);
            sb.append(" ");
        }
        for(String query : queries.values()){
            sb.append(query);
            sb.append(" ");
        }
        return sb.toString();
    }
}
