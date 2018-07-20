package org.radarcns.mongo.data.monitor.questionnaire;

import static org.radarcns.mongo.util.MongoHelper.ASCENDING;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;

import java.time.Instant;

import org.bson.Document;
import org.radarcns.domain.restapi.monitor.QuestionnaireCompletionStatus;
import org.radarcns.mongo.data.monitor.application.MongoApplicationStatusWrapper;
import org.radarcns.mongo.util.MongoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireCompletionLogWrapper {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MongoApplicationStatusWrapper.class);

    private static final String QUESTIONNAIRE_COMPLETION_LOG_COLLECTION =
            "questionnaire-completion-log";


    /**
     * Returns an {@code ApplicationStatus} initialised with the extracted value.
     *
     * @param subject is the subjectID
     * @param source  is the sourceID
     * @param client  is the mongoDb client instance
     * @return the last seen status update for the given subject and sourceType, otherwise null
     */
    public QuestionnaireCompletionStatus valueByProjectSubjectSource(String project, String subject,
            String source, MongoClient client) {

        MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(
                MongoHelper.getCollection(client, QUESTIONNAIRE_COMPLETION_LOG_COLLECTION), project,
                subject, source, VALUE + ".time", ASCENDING, 1);

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        QuestionnaireCompletionStatus data = new QuestionnaireCompletionStatus();
        data.setTimeRecorded(Instant.ofEpochSecond(doc.getLong("time")));
        data.setQuestionnaireName(doc.getString("name"));
        data.setCompletionPercentage(doc.getDouble("completionPercentage"));

        return data;
    }
}
