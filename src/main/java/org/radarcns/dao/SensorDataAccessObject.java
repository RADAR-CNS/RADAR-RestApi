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

package org.radarcns.dao;

import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.catalogue.Unit;
import org.radarcns.dao.mongo.data.sensor.DataFormat;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.radarcns.domain.managementportal.SourceData;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.restapi.header.EffectiveTimeFrame;
import org.radarcns.restapi.header.Header;
import org.radarcns.restapi.source.Source;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Data Accesss Object database independent.
 */
public class SensorDataAccessObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataAccessObject.class);

    /** Map containing actual implementations of each data DAO. **/
    private final Map<String, MongoSensor> mongoSensorMap = new HashMap<>();

    /** Constructor. **/
    public SensorDataAccessObject(SourceCatalog sourceCatalog) throws IOException {

        sourceCatalog.getSourceTypes().forEach(sourceType -> {
            List<SourceData> sourceTypeConsumer = sourceType.getSourceData();
            sourceTypeConsumer.forEach(sourceData -> {
                mongoSensorMap.put(sourceData.getSourceDataName(), DataFormat.getMongoSensor
                        (sourceData));
            });
        });

        LOGGER.info("SensorDataAccessObject successfully loaded.");
    }

    /**
     * Returns the singleton Data Access Object associated with the sensor for the given source.
     *
     * @param sensorType sensor of interest
     * @return {@code MongoSensor} associated with the requested sensor for the given source
     */
    public MongoSensor getInstance(String sensorType) {
        return this.mongoSensorMap.get(sensorType);
    }

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @param sourceData is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return the last seen data value stat for the given subject and source, otherwise
     *      empty dataset
     *
     * @see Dataset
     */
    public Dataset getLastReceivedSample(String subject, String source, DescriptiveStatistic stat,
            TimeWindow timeWindow, SourceData sourceData, ServletContext context , String
            sourceType)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        Header header = getHeader(subject, source, sourceData, stat,
                timeWindow, client, sourceType);

        if (header == null) {
            return new Dataset(null, new LinkedList<>());
        }

        MongoSensor sensorDao = mongoSensorMap.get(sourceData.getSourceDataName());

        return sensorDao.valueRTByUserSource(subject, source, header,
                    RadarConverter.getMongoStat(stat), MongoHelper.getCollection(context,
                        sensorDao.getCollectionName(header.getSource(), timeWindow)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @param sourceData is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return data dataset for the given subject and source, otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset getSamples(String subject, String source, DescriptiveStatistic stat,
            TimeWindow timeWindow, SourceData sourceData, ServletContext context , String
            sourceType)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        Header header = getHeader(subject, source, sourceData, stat,
                timeWindow, client, sourceType);

        if (header == null) {
            return new Dataset(null, new LinkedList<>());
        }

        MongoSensor sensorDao = mongoSensorMap.get(sourceData.getSourceDataName());

        return sensorDao.valueByUserSource(subject, source, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(context,
                    sensorDao.getCollectionName(header.getSource(), timeWindow)));
    }

    /**
     * Returns a {@link Dataset} containing alla available values for the couple subject surce.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sourceData is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return data dataset for the given subject and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset getSamples(String subject, String source,
            DescriptiveStatistic stat, TimeWindow timeWindow, Long start, Long end,
            SourceData sourceData, ServletContext context , String sourceType) throws
            ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        Header header = getHeader(subject, source, sourceData, stat,
                timeWindow, client , sourceType);

        if (header == null) {
            return new Dataset(null, new LinkedList<>());
        }

        MongoSensor sensorDao = mongoSensorMap.get(sourceData.getSourceDataName());

        return sensorDao.valueByUserSourceWindow(subject, source, header,
                RadarConverter.getMongoStat(stat), start, end, MongoHelper.getCollection(context,
                    sensorDao.getCollectionName(header.getSource(), timeWindow)));
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple subject
     *      source.
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param sourceType is the required source type
     * @return the number of received messages within the time-window [start-end].
     */
    public double count(String subject, String source, Long start,
            Long end, String sensorType, String sourceType, MongoClient client)
            throws ConnectException {
        MongoSensor sensorDao = mongoSensorMap.get(sensorType);

        return sensorDao.countSamplesByUserSourceWindow(subject, source, start, end,
                MongoHelper.getCollection(client,
                sensorDao.getCollectionName(sourceType, TimeWindow.TEN_SECOND)));
    }

    /**
     * Finds all subjects checking all available collections.
     *
     * @param client MongoDB client
     * @return a {@code Set<String>} containing all Subject Identifier
     * @throws ConnectException if MongoDB is not available
     *
     */
    public Set<String> getAllSubject(MongoClient client) throws ConnectException {
        Set<String> subjects = new HashSet<>();

        for (MongoSensor mongoSensor : mongoSensorMap.values()) {
            subjects.addAll(mongoSensor.findAllUser(client));
        }

        return subjects;
    }

    /**
     * Returns all available sources for the given subject.
     *
     * @param subject subject identifier.
     * @param client MongoDb client
     * @return a {@code Set<Source>} containing all {@link Source} used by the given {@code subject}
     * @throws ConnectException if MongoDB is not available
     */
    public Set<Source> getAllSources(String subject, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        for (MongoSensor mongoSensor : mongoSensorMap.values()) {
            sources.addAll(mongoSensor.findAllSourcesByUser(subject, client));
        }

        return sources;
    }

    /**
     * Finds the source type for the given sourceID.
     *
     * @param source source identifier
     * @param client {@link MongoClient} used to connect to the database
     * @return a study {@code SourceType}
     *
     * @throws ConnectException if MongoDB is not available
     */
    public String getSourceType(String source, MongoClient client) throws ConnectException {
        for (MongoSensor mongoSensor : mongoSensorMap.values()) {
            String type = mongoSensor.findSourceType(source, client);

            if (type != null) {
                return type;
            }
        }

        return null;
    }

    /**
     * Returns {@link EffectiveTimeFrame} during which the {@code subject} have sent data.
     *
     * @param subject subject identifier
     * @param client {@link MongoClient} used to connect to the database
     *
     * @return {@link EffectiveTimeFrame} reporting the interval during which the subject has sent
     *      data into the platform
     *
     * @throws ConnectException if the connection with MongoDb cannot be established
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(String subject, MongoClient client)
            throws ConnectException {
        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;

        Set<Source> sources = getAllSources(subject, client);

        for (MongoSensor mongoSensor : mongoSensorMap.values()) {
            for (Source source : sources) {
                start = Math.min(start,
                        mongoSensor.getTimestamp(subject, source.getId(), true, client).getTime());
                end = Math.max(end,
                    mongoSensor.getTimestamp(subject, source.getId(), false, client).getTime());
            }
        }

        return new EffectiveTimeFrame(RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} INSTANCE
     */
    public String getCollectionName(String sourceType, String sensorType,
            TimeWindow timeWindow) {
        return mongoSensorMap.get(sensorType).getCollectionName(sourceType, timeWindow);
    }

    /**
     * Returns all supported {@code SensorType}s.
     * @return collection of all {@code SensorType} for which a Data Access Object has been
     *      defined.
     */
    public Collection<String> getSupportedSensor() {
        return mongoSensorMap.keySet();
    }

    /**
     * Either returns the {@link Unit} specified in the
     *      {@link org.radarcns.config.catalog.DeviceCatalog} for the given source type and
     *      sensor type or overrides the default {@link Unit} for the given
     *      {@link DescriptiveStatistic}.
     *
     * @param sourceType source type where the sensor is hosted
     * @param sensorType sensor type of interest
     * @param statistic {@link DescriptiveStatistic} for which the {@link Unit} is required
     *
     * @return a {@link Unit}
     */
    public static Unit getUnit(String sourceType, String sensorType,
            DescriptiveStatistic statistic) {

        switch (statistic) {
            case RECEIVED_MESSAGES: return Unit.PERCENTAGE;
            default: return null;
//            default: return SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);
        }
    }

    /**
     * Returns a {@link Header} that can be used to constract a {@link Dataset}.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sourceData is sourceData involved in the operation
     * @param stat {@link DescriptiveStatistic} stating the required statistical value
     * @param timeWindow time window is the time interval between two consecutive samples
     * @param client {@link MongoClient} used to connect to the database
     *
     * @return {@link Header} related to the given inputs
     *
     * @throws ConnectException if the connection with MongoDb cannot be established
     *
     * @see Dataset
     */
    private Header getHeader(String subject, String source, SourceData sourceData,
            DescriptiveStatistic stat, TimeWindow timeWindow, MongoClient client , String
            sourceType)
            throws ConnectException {

        return new Header(subject, source, sourceType, sourceData.getSourceDataType(), stat,
                sourceData.getUnit(), timeWindow, this.getEffectiveTimeFrame(subject, client));
    }
}
