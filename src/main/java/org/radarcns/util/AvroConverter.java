package org.radarcns.util;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 11/11/2016.
 */
public class AvroConverter {

    private static Logger logger = LoggerFactory.getLogger(AvroConverter.class);

    /**
     * Returns an encoded JSON object for the given Avro object.
     *
     * @param record is the record to encode
     * @return the JSON object representing this Avro object.
     *
     * @throws IOException if there is an error.
     */
    public static JsonNode avroToJsonNode(SpecificRecord record) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(record.toString());
        } catch (IOException exec) {
            logger.error("Impossible to generate error message", exec);
        }

        return null;
    }

    /**
     * Returns an encoded JSON object for the given Avro object.
     *
     * @param record is the record to encode
     * @param sensor name used to fix the json field name
     * @return the JSON object representing this Avro object.
     *
     * @throws IOException if there is an error.
     */
    public static JsonNode avroToJsonNode(SpecificRecord record, String sensor) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataset = mapper.readTree(record.toString());

        setSensorName(dataset, sensor);

        return dataset;
    }

    /**
     * Returns an encoded JSON object for the given Avro object. The automatic item names are
     * replaced with the input String.
     *
     * @param node object serialised in JSON
     * @param sensor name that has to be replaced
     * @return the JSON object representing this Avro object
     */
    private static void setSensorName(JsonNode node, String sensor) {
        if (node.has("dataset")) {
            JsonNode dataset = node.get("dataset");

            Iterator<JsonNode> it = dataset.elements();
            while (it.hasNext()) {
                JsonNode son = it.next();

                ((ObjectNode) son).set(sensor,son.get("value"));
                ((ObjectNode) son).remove("value");
            }
        }
    }

    /**
     * Returns a byte array version of the given record.
     *
     * @param record is the record to encode
     * @return the byte array representing this Avro object.
     *
     * @throws IOException due to {@code DatumWriter}
     *
     * @see {@link org.apache.avro.io.DatumWriter}
     */
    public static <K extends SpecificRecord> byte[] avroToAvroByte(K record) throws IOException {
        DatumWriter<K> writer = new SpecificDatumWriter<>(record.getSchema());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);

        writer.write(record, encoder);

        encoder.flush();
        return output.toByteArray();
    }

    /**
     * Returns a byte array version of the given record.
     *
     * @param input is the byte array that has to be deserialised
     * @param schema to serialise
     * @return the deserialised record
     *
     * @throws IOException due to {@code DatumReader}
     *
     * @see {@link org.apache.avro.io.DatumReader}
     */
    public static <K extends SpecificRecord> K avroByteToAvro(byte[] input, Schema schema)
            throws IOException {
        DatumReader<K> reader = new SpecificDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(input, null);

        return reader.read(null, decoder);
    }

}
