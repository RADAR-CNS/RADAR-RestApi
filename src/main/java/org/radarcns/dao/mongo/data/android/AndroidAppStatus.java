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

package org.radarcns.dao.mongo.data.android;

import org.bson.Document;
import org.radarcns.dao.mongo.util.MongoAndroidApp;
import org.radarcns.restapi.app.Application;

public class AndroidAppStatus extends MongoAndroidApp {

    public static final String UPTIME_COLLECTION = "application_uptime";

    //TODO take field names from RADAR MongoDb Connector
    @Override
    protected Application getApplication(Document doc, Application app) {
        app.setUptime(doc.getDouble("applicationUptime"));

        return app;
    }

    @Override
    protected String getCollectionName() {
        return UPTIME_COLLECTION;
    }
}
