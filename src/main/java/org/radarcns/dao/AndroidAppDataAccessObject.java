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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.radarcns.domain.restapi.Application;
import org.radarcns.domain.restapi.Source;
import org.radarcns.mongo.data.android.AndroidAppStatus;
import org.radarcns.mongo.data.android.AndroidRecordCounter;
import org.radarcns.mongo.data.android.AndroidServerStatus;
import org.radarcns.mongo.util.MongoAndroidApp;

/**
 * Data Access Object for Android App Status values.
 */
public class AndroidAppDataAccessObject {

    private static final AndroidAppDataAccessObject instance = new AndroidAppDataAccessObject();

    public static AndroidAppDataAccessObject getInstance() {
        return instance;
    }

    private final List<MongoAndroidApp> dataAccessObjects;

    private AndroidAppDataAccessObject() {
        dataAccessObjects = new LinkedList<>();

        dataAccessObjects.add(new AndroidAppStatus());
        dataAccessObjects.add(new AndroidRecordCounter());
        dataAccessObjects.add(new AndroidServerStatus());
    }

    /**
     * Computes the Android App Status realign on different collection.
     *
     * @param subject identifier
     * @param source identifier
     * @param client is the MongoDb client
     * @return {@code Application} representing the status of the related Android App
     */
    public Application getStatus(String subject, String source, MongoClient client) {
        Application app = null;

        for (MongoAndroidApp dataAccessObject : dataAccessObjects) {
            app = dataAccessObject.valueBySubjectSource(subject, source, app, client);
        }

        return app;
    }

    /**
     * Finds all subjects.
     *
     * @return all distinct subjectIDs for the given collection, otherwise an empty Collection
     */
    public Collection<String> findAllUser(MongoClient client) {
        Set<String> subjects = new HashSet<>();

        for (MongoAndroidApp dataAccessObject : dataAccessObjects) {
            subjects.addAll(dataAccessObject.findAllUser(client));
        }

        return subjects;
    }

    /**
     * Finds all sources for the given subject.
     *
     * @param subject is the subjectID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     */
    public Collection<Source> findAllSourcesBySubject(String subject, MongoClient client) {
        Set<Source> subjects = new HashSet<>();

        for (MongoAndroidApp dataAccessObject : dataAccessObjects) {
            subjects.addAll(dataAccessObject.findAllSourcesByUser(subject, client));
        }

        return subjects;
    }

    /**
     * Finds the source type for the given sourceID.
     *
     * @param source SourceID
     * @param client MongoDB client
     * @return a study {@code SourceType}
     */
    public String findSourceType(String source, MongoClient client) {
        String type = null;
        Iterator<MongoAndroidApp> iterator = dataAccessObjects.iterator();

        while (iterator.hasNext() && type == null) {
            type = iterator.next().findSourceType(source, client);
        }

        return type;
    }

    /**
     * Returns all mongoDb collections used by this DAO.
     * @return list of String
     */
    public List<String> getCollections() {
        List<String> list = new LinkedList<>();

        for (MongoAndroidApp dataAccessObject : dataAccessObjects) {
            list.addAll(dataAccessObject.getCollectionNames());
        }

        return list;
    }
}