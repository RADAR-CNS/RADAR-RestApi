package org.radarcns.unit.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.config.api.ApiConfig;
import org.radarcns.config.Properties;

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
public class TestApiConfig {

    @Test
    public void loadApiConfigOk() throws IOException {
        assertEquals("device-catalog.yml", Properties.getApiConfig().getDeviceCatalog());
    }

    @Test
    public void loadApiConfigApiOk() throws IOException {
        ApiConfig config = new YamlConfigLoader().load(new File(
                TestApiConfig.class.getClassLoader()
                .getResource("radar_test_1_ok.yml").getFile()), ApiConfig.class);

        assertEquals("/api", config.getApiBasePath());

        Assert.assertArrayEquals(new String[]{"http", "https", "test", "test"},
                config.getApplicationProtocols());
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class)
    public void readCatalogKoUnit() throws IOException {
        new YamlConfigLoader().load(new File(
                TestApiConfig.class.getClassLoader()
                .getResource("radar_dev_catalog_ko.yml").getFile()), ApiConfig.class);
    }

    @Test
    public void testUrlPath() throws IOException {
        assertEquals("http://localhost:8080/radar/api/",
                Properties.getApiConfig().getApiUrl());
    }
}
