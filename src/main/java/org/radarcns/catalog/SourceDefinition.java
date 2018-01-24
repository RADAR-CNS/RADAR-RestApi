//package org.radarcns.catalog;
//
///*
// * Copyright 2016 King's College London and The Hyve
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import org.radarcns.catalogue.TimeWindow;
//import org.radarcns.catalogue.Unit;
//import org.radarcns.dao.mongo.data.sensor.DataFormat;
//import org.radarcns.managementportal.SourceType;
//import org.radarcns.restapi.spec.SensorSpecification;
//import org.radarcns.restapi.spec.SourceSpecification;
//
///**
// * Generic Source Definition.
// */
//public class SourceDefinition {
//
//    //private static final Logger LOGGER = LoggerFactory.getLogger(SourceDefinition.class);
//
//    /** Source name. **/
//    private final String sourceType;
//    /** Map to associate each data with the relative definition. **/
//    private final Map<String, SensorSpecification> specificationMap;
//    /** For each data, it define which data class has to be used to read data from MongoDb. **/
//    private final Map<String, DataFormat> formatMap;
//    /** For each data, it lists all the relative collection names. **/
//    private final Map<String, Map<TimeWindow, String>> collectionsMap;
//
//    /**
//     * Constructor.
//     **/
//    public SourceDefinition(SourceType sourceType) {
//        specificationMap = new HashMap<>();
//        formatMap = new HashMap<>();
//        collectionsMap = new HashMap<>();
//
//        for (SensorCatalog sensor : device.getSensors()) {
//            specificationMap.put(sensor.getName(), new SensorSpecification(sensor.getName(),
//                    sensor.getDataType(), sensor.getFrequency(), sensor.getUnit()));
//
//            formatMap.put(sensor.getName(), sensor.getDataFormat());
//
//            collectionsMap.put(sensor.getName(), convertCollectionMap(sensor.getCollections()));
//        }
//
//        this.sourceType = sourceType;
//    }
//
//    /**
//     * Returns the SourceDefinition Specification used to check whether the device is sending the
//     *      expected amount of data.
//     *
//     * @return {@code SourceSpecification} containing all data names and related frequencies
//     */
//    public SourceSpecification getSpecification() {
//        Map<String, SensorSpecification> sensors = new HashMap<>();
//
//        for (String type : specificationMap.keySet()) {
//            sensors.put(type, new SensorSpecification(type,
//                    specificationMap.get(type).getDataType(),
//                    specificationMap.get(type).getFrequency(),
//                    specificationMap.get(type).getUnit()));
//        }
//
//        return new SourceSpecification(sourceType, sensors);
//    }
//
//    public String getType() {
//        return sourceType;
//    }
//
//    /**
//     * Returns all on board Sensor Type.
//     *
//     * @return {@code Collection<SensorType>} for the given source
//     */
//    public Collection<String> getSensorTypes() {
//        return specificationMap.keySet();
//    }
//
//    /**
//     * Returns the Unit associated with the source.
//     *
//     * @return {@code Unit} for the given source
//     */
//    public Unit getMeasurementUnit(String sensor) {
//        return specificationMap.get(sensor).getUnit();
//    }
//
//    /**
//     * Returns the frequency associated with the data.
//     *
//     * @return {@code Double} stating the data frequency
//     */
//    public Double getFrequency(String sensor) {
//        return specificationMap.get(sensor).getFrequency();
//    }
//
//    /**
//     * Converts the keys of the collection map from String to TimeFrame.
//     *
//     * @see TimeWindow
//     */
//    private Map<TimeWindow, String> convertCollectionMap(Map<String, String> input) {
//        Map<TimeWindow, String> map = new HashMap<>();
//
//        for (String key : input.keySet()) {
//            switch (key) {
//                case "10sec":
//                    map.put(TimeWindow.TEN_SECOND, input.get(key));
//                    break;
//                case "1min":
//                    map.put(TimeWindow.ONE_MIN, input.get(key));
//                    break;
//                case "10min":
//                    map.put(TimeWindow.TEN_MIN, input.get(key));
//                    break;
//                case "1h":
//                    map.put(TimeWindow.ONE_HOUR, input.get(key));
//                    break;
//                case "1d":
//                    map.put(TimeWindow.ONE_DAY, input.get(key));
//                    break;
//                case "1w":
//                    map.put(TimeWindow.ONE_WEEK, input.get(key));
//                    break;
//                default: throw new UnsupportedOperationException(key + " is not a supported value."
//                    + " It is outside the domain specified by"
//                    + " org.radarcns.avro.restapi.header.TimeFrame.");
//            }
//        }
//
//        return map;
//    }
//
//    public Map<String, Map<TimeWindow, String>> getCollections() {
//        return collectionsMap;
//    }
//
//    /**
//     * Checks if a source has on-board the given sensor.
//     * @param sensor requested sensor
//     * @return true if the sensor is on-board on source, false otherwise
//     */
//    public boolean isSupported(String sensor) {
//        return specificationMap.containsKey(sensor);
//    }
//
//    public Map<String, DataFormat> getFormats() {
//        return formatMap;
//    }
//}
