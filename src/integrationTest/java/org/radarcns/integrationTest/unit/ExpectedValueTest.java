package org.radarcns.integrationTest.unit;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.bson.Document;
import org.junit.Test;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.config.Properties;
import org.radarcns.integrationTest.aggregator.ExpectedValue;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExpectedValueTest Test.
 */
public class ExpectedValueTest {

    private static final Logger logger = LoggerFactory.getLogger(ExpectedValueTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final int SAMPLES = 10;

    @Test
    public void matchDatasetOnDocuments() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, EMPATICA, HR, COUNT, SAMPLES);
        Dataset dataset = RandomInput.getDatasetRandom(USER, SOURCE, EMPATICA, HR, COUNT, SAMPLES);

//        ObjectMapper mapper = new ObjectMapper();
//        Object json = mapper.readValue(dataset.toString(), Object.class);
//        String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
//        logger.info(indented);

        int count = 0;
        for (Document doc : docs) {
//            logger.info(doc.toJson());
            count += doc.getDouble("count").intValue();
        }
        assertEquals(SAMPLES, count);

        count = 0;
        for (Item item : dataset.getDataset()) {
            count += (Double) ((HeartRate) item.get("value")).getValue();
        }
        assertEquals(SAMPLES, count);

        EffectiveTimeFrame window1 = new EffectiveTimeFrame(
            RadarConverter.getISO8601(docs.get(0).getDate("start")),
            RadarConverter.getISO8601(docs.get(docs.size() - 1).getDate("end")));

        EffectiveTimeFrame window2 = dataset.getHeader().getEffectiveTimeFrame();

        assertEquals(true, ExpectedValue.compareEffectiveTimeFrame(window1, window2));
    }

}
