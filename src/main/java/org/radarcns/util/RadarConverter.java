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

package org.radarcns.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.radarcns.domain.restapi.ServerStatus;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of converting functions.
 */
public final class RadarConverter {

    private static final Logger logger = LoggerFactory.getLogger(RadarConverter.class);
    /**
     * Global JSON factory. If the reader and writer functions in this class are not sufficient, use
     * this factory to create a new ObjectMapper.
     */
    public static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * Global ObjectMapper. It is kept private to prevent further configuration.
     */
    private static final ObjectMapper OBJECT_MAPPER;

    /**
     * Generic Avro SpecificRecord to JSON writer.
     */
    public static final ObjectWriter AVRO_JSON_WRITER;

    static {
        OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY);
        OBJECT_MAPPER
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        AVRO_JSON_WRITER = OBJECT_MAPPER.writer();

        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
    }

    private RadarConverter() {
        // utility class
    }

    /**
     * Converts a {@code MongoHelper.Stat} to {@code DescriptiveStatistic}.
     **/
    public static DescriptiveStatistic getDescriptiveStatistic(MongoHelper.Stat stat) {
        switch (stat) {
            case avg:
                return DescriptiveStatistic.AVERAGE;
            case count:
                return DescriptiveStatistic.COUNT;
            case iqr:
                return DescriptiveStatistic.INTERQUARTILE_RANGE;
            case max:
                return DescriptiveStatistic.MAXIMUM;
            case min:
                return DescriptiveStatistic.MINIMUM;
            case sum:
                return DescriptiveStatistic.SUM;
            case quartile:
                return DescriptiveStatistic.QUARTILES;
            case median:
                return DescriptiveStatistic.MEDIAN;
            case receivedMessage:
                return DescriptiveStatistic.RECEIVED_MESSAGES;
            default:
                throw new IllegalArgumentException("MongoHelper.Stat type cannot be"
                        + "converted. " + stat.name() + " is unknown");
        }
    }

    /**
     * Converts a {@code DescriptiveStatistic} to {@code MongoHelper.Stat}.
     **/
    public static MongoHelper.Stat getMongoStat(DescriptiveStatistic stat) {
        switch (stat) {
            case AVERAGE:
                return MongoHelper.Stat.avg;
            case COUNT:
                return MongoHelper.Stat.count;
            case INTERQUARTILE_RANGE:
                return MongoHelper.Stat.iqr;
            case MAXIMUM:
                return MongoHelper.Stat.max;
            case MEDIAN:
                return MongoHelper.Stat.median;
            case MINIMUM:
                return MongoHelper.Stat.min;
            case QUARTILES:
                return MongoHelper.Stat.quartile;
            case RECEIVED_MESSAGES:
                return Stat.receivedMessage;
            case SUM:
                return MongoHelper.Stat.sum;
            default:
                throw new IllegalArgumentException("DescriptiveStatistic type cannot be"
                        + "converted. " + stat.name() + "is unknown");
        }
    }

    /**
     * Rounds a double input.
     *
     * @param value input
     * @param places the required decimal places precision
     **/
    public static double roundDouble(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Converts the input String in Server Status.
     **/
    public static ServerStatus getServerStatus(String value) {
        if (value == null) {
            return ServerStatus.UNKNOWN;
        }
        try {
            return ServerStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warn("Unsupported ServerStatus. Value is {}", value);
            return ServerStatus.UNKNOWN;
        }
    }

    /**
     * Converts the SensorType to the related data name used to convert AVRO to JSON.
     **/
    public static String getSensorName(String sensor) {
        switch (sensor) {
            case "THERMOMETER":
                return "temperature";
            case "ACCELEROMETER":
                return "acceleration";
            default:
                return sensor.toLowerCase(Locale.US);
        }
    }

    /**
     * Converts a String to the related source type.
     **/
    public static String getSourceType(String value) {
        return value.toUpperCase();
    }

    /**
     * Returns the expected number of records.
     *
     * @param timeWindow {@link TimeWindow} to provide data context
     * @param frequency double to provide data context
     * @return the number of expected messages
     */
    public static double getExpectedMessages(TimeWindow timeWindow, double frequency) {
        return TimeScale.getSeconds(timeWindow) * frequency;
    }

    /**
     * Create a writer that writes given class.
     */
    public static ObjectWriter writerFor(Class<?> cls) {
        return OBJECT_MAPPER.writerFor(cls);
    }

    /**
     * Create a reader that reads given class.
     */
    public static ObjectReader readerFor(Class<?> cls) {
        return OBJECT_MAPPER.readerFor(cls);
    }

    /**
     * Create a reader that reads given collection type containing given class.
     */
    public static ObjectReader readerForCollection(Class<? extends Collection> collCls,
            Class<?> cls) {
        try {
            return OBJECT_MAPPER.readerFor(
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(collCls, cls));
        } catch (RuntimeException ex) {
            logger.error("Failed to construct object reader for collection", ex);
            throw ex;
        }
    }

    /**
     * Whether a given temporal threshold is passed, compared to given time.
     */
    public static boolean isThresholdPassed(Temporal time, Duration duration) {
        return Duration.between(time, Instant.now()).compareTo(duration) > 0;
    }

    public static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
