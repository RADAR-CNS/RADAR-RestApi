/*
 * Copyright 2017 King's College London and The Hyve
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

package org.radarcns.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceData;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.mongo.data.sensor.DataFormat;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MongoDB Data Access Object for data generated by sensor.
 */
public abstract class MongoSourceDataWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoSourceDataWrapper.class);

    private final Map<TimeWindow, String> timeWindowToCollectionsMap;

    private final SourceData sourceData;

    /**
     * Constructs a MongoSourceDataWrapper able to query the collections of the sensor for the given
     * sourceType.
     *
     * @param sourceData of the given sourceType that will be consume from this instance
     */
    public MongoSourceDataWrapper(SourceData sourceData) {

        timeWindowToCollectionsMap = createCollectionsForTimeWindow(sourceData.getTopic());

        this.sourceData = sourceData;
    }

    /**
     * Returns the {@code SensorType} related to this instance.
     */
    public String getSourceDataType() {
        return sourceData.getSourceDataType();
    }

    /**
     * Returns the {@code DataFormat} related to this instance.
     */
    public abstract DataFormat getDataFormat();

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple subject sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param header information used to provide the data context
     * @param collection is the mongoDb collection that has to be queried
     * @return the last seen data value stat for the given subject and sourceType, otherwise empty
     * dataset
     * @see Dataset
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset getLatestRecord(String projectName, String subject, String source, Header
            header, Stat stat, MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectName, subject, source, MongoHelper
                        .END, -1, 1, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param collection is the mongoDb collection that has to be queried
     * @return data dataset for the given subject and sourceType, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllRecords(String projectName, String subject, String source, Header
            header, MongoHelper.Stat stat, MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectName, subject, source, MongoHelper
                        .START, 1, null, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return data dataset for the given subject and sourceType within the start and end time
     * window, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(String projectName, String subject, String source, Header
            header, MongoHelper.Stat stat, Long start, Long end,
            MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentsByProjectAndSubjectAndSourceInWindow(projectName, subject, source,
                        start, end, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return the number of received messages within the time-window [start-end].
     */
    public double countSamplesByUserSourceWindow(String subject, String source, Long start,
            Long end, MongoCollection<Document> collection) {
        double count = 0;
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSourceWindow(subject, source, start, end, collection);

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            count += extractCount(doc);
        }

        cursor.close();

        return count;
    }

    /**
     * Builds the required {@link Dataset}. It adds the {@link EffectiveTimeFrame} to the given
     * {@link Header}.
     *
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param header information to provide the context of the data set
     * @param cursor the mongoD cursor
     * @return data dataset for the given input, otherwise empty dataset
     * @see Dataset
     */
    private Dataset getDataSet(String field, DescriptiveStatistic stat, Header header,
            MongoCursor<Document> cursor) {
        Date start = null;
        Date end = null;

        LinkedList<DataItem> list = new LinkedList<>();

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return new Dataset(null, list);
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();

            Date localStart = doc.getDate(MongoHelper.START);
            Date localEnd = doc.getDate(MongoHelper.END);

            if (start == null) {
                start = localStart;
                end = localEnd;
            } else {
                if (start.after(localStart)) {
                    start = localStart;
                }
                if (end.before(localEnd)) {
                    end = localEnd;
                }
            }

            DataItem item = new DataItem(docToAvro(doc, field, stat, header),
                    RadarConverter.getISO8601(doc.getDate(MongoHelper.START)));

            list.addLast(item);
        }

        cursor.close();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(
                RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));

        header.setEffectiveTimeFrame(etf);

        Dataset hrd = new Dataset(header, list);

        LOGGER.debug("Found {} value", list.size());

        return hrd;
    }

    /**
     * Returns the required mongoDB collection name for the given sourceType type.
     *
     * @param interval useful to identify which collection has to be queried. A sensor has a
     * collection for each time frame or time window
     * @return the MongoDB Collection name
     */
    public String getCollectionName(TimeWindow interval) {
        if (timeWindowToCollectionsMap.containsKey(interval)) {
            return timeWindowToCollectionsMap.get(interval);
        }

        throw new IllegalArgumentException("Unknown sourceType type. " + sourceData
                + "is not yest supported.");
    }

    /**
     * Convert a {@link Document} to the corresponding {@link org.apache.avro.specific.SpecificRecord}.
     * This function must be override by the subclass
     *
     * @param doc {@link Document} storing data used to create the related {@link DataItem}
     * @param field key of the value that has to be extracted from the {@link Document}
     * @param stat {@link DescriptiveStatistic} represented by the resulting {@link DataItem}
     * @param header {@link Header} used to provide the data context
     * @return the {@link DataFormat} related to the sensor
     */
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * Extract the count information for the given MongoDB document. This function should be
     * overridden by the subclass.
     *
     * @param doc is the Bson Document from which we extract the required value to compute the count
     * value
     * @return the count value
     */
    protected abstract int extractCount(Document doc);

    /**
     * Converts the keys of the collection map from String to TimeFrame.
     *
     * @see TimeWindow
     */
    private Map<TimeWindow, String> createCollectionsForTimeWindow(String topicName) {
        Map<TimeWindow, String> map = new HashMap<>();

        map.put(TimeWindow.TEN_SECOND, topicName);
        map.put(TimeWindow.ONE_MIN, topicName.concat("_1min"));
        map.put(TimeWindow.TEN_MIN, topicName.concat("_10min"));
        map.put(TimeWindow.ONE_HOUR, topicName.concat("_1h"));
        map.put(TimeWindow.ONE_DAY, topicName.concat("_1d"));
        map.put(TimeWindow.ONE_WEEK, topicName.concat("_1w"));
        return map;
    }
}