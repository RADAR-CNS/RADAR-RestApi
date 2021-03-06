/*
 *  Copyright 2017 King's College London and The Hyve
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

package org.radarcns.webapp.resource;

/**
 * Static class listing End Point parameter.
 */
public interface Parameter {

    String END = "endTime";
    String TIME_WINDOW = "timeWindow";
    String SOURCE_DATA_NAME = "sourceDataName";
    String SOURCE_ID = "sourceId";
    String START = "startTime";
    String STAT = "stat";
    String PROJECT_NAME = "projectName";
    String SUBJECT_ID = "subjectId";
    String PRODUCER = "producer";
    String MODEL = "model";
    String CATALOGUE_VERSION = "catalogueVersion";
}
