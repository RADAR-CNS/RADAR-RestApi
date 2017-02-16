package org.radarcns.integrationtest.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.integrationtest.util.Parser.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 15/02/2017.
 */
public class CSVValidator {

    private static final Logger logger = LoggerFactory.getLogger(CSVValidator.class);

    public static void validate(MockDataConfig config)
        throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Parser parser = new Parser(config);

        int line = 1;
        String mex = null;

        Map<Variable, Object> map = parser.next();
        Map<Variable, Object> last = map;
        while (map != null){
            line++;

            if( !last.get(Variable.USER).toString().equals(map.get(Variable.USER).toString()) ) {
                mex = "It is possible to test only one user at time.";
            } else if ( !last.get(Variable.SOURCE).toString().equals(map.get(Variable.SOURCE).toString()) ) {
                mex = "It is possible to test only one source at time.";
            } else if ( !( ((Long)last.get(Variable.TIMESTAMP)).longValue() <= ((Long)map.get(Variable.TIMESTAMP)).longValue() ) ) {
                mex = Variable.TIMESTAMP.toString() + " must increase raw by raw.";
            } else if ( map.get(Variable.VALUE) == null ) {
                mex = Variable.VALUE.toString() + "value to test must be specified.";
            }

            if ( mex != null) {
                mex += " " + config.getDataFile() + " is invalid. Error at line " + line;
                logger.error(mex);
                throw new IllegalArgumentException(mex);
            }

            last = map;
            map = parser.next();
        }

        parser.close();
    }

}
