/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.edgeanalyticsservice;

import org.wso2.edgeanalyticsservice.IEdgeAnalyticsCallback;
import org.wso2.edgeanalyticsservice.Stream;

interface IEdgeAnalyticsService {
    void addStream(String streamDefinition, String packageName);
    void removeStream(String streamName, String packageName);
    void addQuery(String queryDefinition, String packageName);
    void removeQuery(String queryName, String packageName);
    List<Stream> getAllStreams();
    void validateExecutionPlan(String packageName);
    void startExecutionPlan(String packageName);
    void addQueryCallback(String queryName, String receiver, String packageName, String ownPackageName);
    void addDynamicQueryCallback(String queryName, String packageName, String receiver);
    void addStreamCallback(String stream, String receiver, String packageName, String ownPackageName);
    void addDynamicStreamCallback(String stream, String packageName, String receiver);
    void sendData(String id, String stream, in List<String> values,in List<String> types);
    void subscribeStreamToData(String packageName, String streamDefinition, in int [] inputTypes);
    void subscribeExecutionPlan(String packageName);
    void stop();
}
