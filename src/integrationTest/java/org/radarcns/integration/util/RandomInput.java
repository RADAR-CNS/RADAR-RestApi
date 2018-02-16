/*
 * Copyright 2016 King's College London and The Hyve
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

package org.radarcns.integration.util;

import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusRecordCounter.RECORD_COLLECTION;
import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusServerStatus.STATUS_COLLECTION;
import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusUpTime.UPTIME_COLLECTION;
import static org.radarcns.mongo.util.MongoHelper.ID;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.PROJECT_ID;
import static org.radarcns.mongo.util.MongoHelper.SOURCE_ID;
import static org.radarcns.mongo.util.MongoHelper.USER_ID;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.mock.model.ExpectedArrayValue;
import org.radarcns.mock.model.ExpectedDoubleValue;
import org.radarcns.monitor.application.ServerStatus;
import org.radarcns.util.RadarConverter;

/**
 * All supported sources specifications.
 */
public class RandomInput {

    public static final String DATASET = "dataset";
    public static final String DOCUMENTS = "documents";
    private static final String SUPPORTED_SOURCE_TYPE = "empatica_e4_v1";

    private static Dataset dataset = null;
    private static List<Document> documents = null;

    private static final ExpectedDocumentFactory expectedDocumentFactory =
            new ExpectedDocumentFactory();
    private static final ExpectedDataSetFactory expectedDataSetFactory =
            new ExpectedDataSetFactory();

    private static void randomDoubleValue(String project, String user, String source,
            String sourceType, String sensorType, DescriptiveStatistic stat, TimeWindow timeWindow,
            int samples, boolean singleWindow) {
        ObservationKey key = new ObservationKey(project, user, source);
        ExpectedDoubleValue instance = new ExpectedDoubleValue();

        int numberOfRecords = samples;
        if (singleWindow) {
            numberOfRecords = 1;
        }
        long now = Instant.now().toEpochMilli();
        for (int i = 0; i < numberOfRecords; i++) {
            instance.add(key, now, ThreadLocalRandom.current().nextDouble());
            now += TimeUnit.SECONDS.toMillis(RadarConverter.getSecond(timeWindow));
        }

        dataset = expectedDataSetFactory.getDataset(instance, project, user, source, sourceType,
                sensorType, stat, timeWindow);
        documents = expectedDocumentFactory.produceExpectedDocuments(instance, timeWindow);
    }

    private static void randomArrayValue(String project, String user, String source,
            String sourceType, String sensorType, DescriptiveStatistic stat, TimeWindow timeWindow,
            int samples, boolean singleWindow) {

        ExpectedArrayValue instance = new ExpectedArrayValue();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        ObservationKey key = new ObservationKey(project, user, source);

        int numberOfRecords = samples;
        if (singleWindow) {
            numberOfRecords = 1;
        }
        long now = Instant.now().toEpochMilli();
        for (int i = 0; i < numberOfRecords; i++) {
            instance.add(key, now, random.nextDouble(), random.nextDouble(), random.nextDouble());
            now += TimeUnit.SECONDS.toMillis(RadarConverter.getSecond(timeWindow));
        }

        dataset = expectedDataSetFactory.getDataset(instance, project, user, source, sourceType,
                sensorType, stat, timeWindow);
        documents = expectedDocumentFactory.produceExpectedDocuments(instance, timeWindow);
    }

    /**
     * Returns a Map containing a {@code Dataset} and a {@code Collection<Document>} randomly
     * generated mocking the behaviour of the RADAR-CNS Platform.
     */
    public static Map<String, Object> getDatasetAndDocumentsRandom(String project, String user,
            String source, String sourceType, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow, int samples, boolean singleWindow) {
        if (SUPPORTED_SOURCE_TYPE.equals(sourceType)) {
            return getBoth(project, user, source, sourceType, sourceDataName, stat,
                    timeWindow, samples, singleWindow);
        }

        throw new UnsupportedOperationException(sourceType + " is not"
                + " currently supported.");
    }

    /**
     * Returns a {@code Collection<Document>} randomly generated that mocks the behaviour of the
     * RADAR-CNS Platform.
     */
    public static List<Document> getDocumentsRandom(String project, String user, String source,
            String sourceType, String sensorType, DescriptiveStatistic stat,
            TimeWindow timeWindow, int samples, boolean singleWindow) {
        switch (sourceType) {
            case SUPPORTED_SOURCE_TYPE:
                return getDocument(project, user, source, sourceType, sensorType, stat,
                        timeWindow, samples, singleWindow);
            default:
                throw new UnsupportedOperationException(sourceType + " is not"
                        + " currently supported.");
        }
    }

    private static List<Document> getDocument(String project, String user, String source,
            String sourceType, String sensorType, DescriptiveStatistic stat, TimeWindow timeWindow,
            int samples, boolean singleWindow) {
        nextValue(project, user, source, sourceType, sensorType, stat, timeWindow, samples,
                singleWindow);
        return documents;
    }

    private static Map<String, Object> getBoth(String project, String user, String source,
            String sourceType, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow,
            int samples, boolean singleWindow) {
        nextValue(project, user, source, sourceType, sourceDataName, stat, timeWindow, samples,
                singleWindow);

        Map<String, Object> map = new HashMap<>();
        map.put(DATASET, dataset);
        map.put(DOCUMENTS, documents);
        return map;
    }

    private static void nextValue(String project, String user, String source, String sourceType,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow, int samples,
            boolean singleWindow) {
        switch (sourceDataName) {
            case "EMPATICA_E4_v1_ACCELEROMETER":
                randomArrayValue(project, user, source, sourceType, sourceDataName, stat,
                        timeWindow,
                        samples, singleWindow);
                break;
            default:
                randomDoubleValue(project, user, source, sourceType, sourceDataName, stat,
                        timeWindow, samples, singleWindow);
                break;
        }
    }

    /**
     * Generates and returns a randomly generated {@code ApplicationStatus} mocking data sent by
     * RADAR-CNS pRMT.
     **/
    public static Map<String, Document> getRandomApplicationStatus(String project, String user,
            String source) {
        String ipAdress = getRandomIpAddress();
        ServerStatus serverStatus = ServerStatus.values()[
                ThreadLocalRandom.current().nextInt(0, ServerStatus.values().length)];
        Double uptime = ThreadLocalRandom.current().nextDouble();
        Double timestamp = ThreadLocalRandom.current().nextDouble();
        int recordsCached = ThreadLocalRandom.current().nextInt();
        int recordsSent = ThreadLocalRandom.current().nextInt();
        int recordsUnsent = ThreadLocalRandom.current().nextInt();

        return getRandomApplicationStatus(project, user, source, ipAdress, serverStatus, uptime,
                recordsCached, recordsSent, recordsUnsent, timestamp);
    }

    /**
     * Generates and returns a ApplicationStatus using the given inputs.
     **/
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public static Map<String, Document> getRandomApplicationStatus(String project, String user,
            String source, String ipAddress, ServerStatus serverStatus, Double uptime,
            int recordsCached, int recordsSent, int recordsUnsent, double timeStamp) {
        Document uptimeDoc = new Document()
                .append("time", timeStamp)
                .append("uptime", uptime);

        Document statusDoc = new Document()
                .append("time", timeStamp)
                .append("clientIP", ipAddress)
                .append("serverStatus", serverStatus.toString());

        Document recordsDoc = new Document()
                .append("time", timeStamp)
                .append("recordsCached", recordsCached)
                .append("recordsSent", recordsSent)
                .append("recordsUnsent", recordsUnsent);

        Map<String, Document> documents = new HashMap<>();
        documents.put(STATUS_COLLECTION, buildAppStatusDocument(project, user, source, statusDoc));
        documents.put(RECORD_COLLECTION, buildAppStatusDocument(project, user, source, recordsDoc));
        documents.put(UPTIME_COLLECTION, buildAppStatusDocument(project, user, source, uptimeDoc));
        return documents;
    }

    private static Document buildKeyDocument(String projectName, String subjectId, String sourceId) {
        return new Document().append(PROJECT_ID, projectName)
                .append(USER_ID, subjectId)
                .append(SOURCE_ID, sourceId);
    }

    private static Document buildAppStatusDocument(String projectName, String subjectId, String sourceId,
            Document value) {
        return new Document().append(ID, "{"
                + PROJECT_ID + ":" + projectName + ","
                + USER_ID + ":" + subjectId + ","
                + SOURCE_ID + ":" + sourceId + "}")
                .append(KEY, buildKeyDocument(projectName, subjectId, sourceId))
                .append(VALUE, value);
    }

    /**
     * Returns a String representing a random IP address.
     **/
    private static String getRandomIpAddress() {
        long ip = ThreadLocalRandom.current().nextLong();
        StringBuilder result = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            result.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                result.insert(0, '.');
            }
            ip = ip >> 8;
        }
        return result.toString();
    }

}
