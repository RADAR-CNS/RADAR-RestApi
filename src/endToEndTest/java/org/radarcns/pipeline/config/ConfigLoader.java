package org.radarcns.pipeline.config;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

public class ConfigLoader {
    private final ObjectMapper mapper;

    public ConfigLoader() {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        // only serialize fields, not getters, etc.
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public <T> T load(File file, Class<T> configClass) throws IOException {
        return mapper.readValue(file, configClass);
    }

    public void store(File file, Object config) throws IOException {
        mapper.writeValue(file, config);
    }

    public String prettyString(Object config) {
        // pretty print
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // make ConfigRadar the root element
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        try {
            return mapper.writeValueAsString(config);
        } catch (JsonProcessingException ex) {
            throw new UnsupportedOperationException("Cannot serialize config", ex);
        } finally {
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        }
    }
}
