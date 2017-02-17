package org.radarcns;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.integrationtest.collector.ExpectedValue;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class AvroTest {

    private static final Logger logger = LoggerFactory.getLogger(AvroTest.class);

    private final boolean TEST = false;

    @Test
    public void testMessage() throws Exception {
        if( TEST ) {
            Message mex = new Message("Test");
            byte[] bytes = AvroConverter.avroToAvroByte(mex);
            Message test = AvroConverter.avroByteToAvro(bytes, Message.getClassSchema());
            assertEquals(mex.getMessage(), test.getMessage());
        }
    }

    @Test
    public void testDataset() throws Exception {
        if( TEST ) {
            for (int i=0; i<10000; i++) {
                Dataset dataset = getRandomDataset();
                byte[] bytes = AvroConverter.avroToAvroByte(dataset);
                Dataset test = AvroConverter.avroByteToAvro(bytes, Dataset.getClassSchema());

                assertEquals(true, ExpectedValue.compareDatasets(dataset, test,
                    0, false));

                Dataset test2 = getRandomDataset();
                assertEquals(true, ExpectedValue.compareDatasets(test2, test2,
                    0, false));

//                logger.info(test.toString());
//                logger.info(test2.toString());
                assertEquals(false, ExpectedValue.compareDatasets(test2, test,
                    0, false));
//                logger.info("{} ---------------------", i);
            }
        }
    }

    private Dataset getRandomDataset(){
        Random random = new Random();

        Date start = new Date();

        LinkedList<Item> list = new LinkedList<>();

        Date startEvent = new Date();
        Date endEvent = new Date();

        startEvent.setTime(start.getTime());
        endEvent.setTime(startEvent.getTime() + 10000);
        EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(startEvent),
            RadarConverter.getISO8601(endEvent));
        Quartiles quartiles = new Quartiles(new Double(random.nextDouble()),
            new Double(random.nextDouble()), new Double(random.nextDouble()));
        Item item = new Item(new Acceleration(quartiles,quartiles,quartiles),etf);
        list.addLast(item);

        startEvent.setTime(endEvent.getTime());
        endEvent.setTime(startEvent.getTime() + 10000);
        etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start), RadarConverter.getISO8601(endEvent));
        quartiles = new Quartiles(new Double(random.nextDouble()),
            new Double(random.nextDouble()), new Double(random.nextDouble()));
        item = new Item(new Acceleration(quartiles,quartiles,quartiles),etf);
        list.addLast(item);

        etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start),RadarConverter.getISO8601(endEvent));
        Header header = new Header(DescriptiveStatistic.quartiles, Unit.hz, etf);

        Dataset hrd = new Dataset(header,list);

        return hrd;
    }
}
