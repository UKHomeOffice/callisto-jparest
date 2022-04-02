package uk.gov.homeoffice.digital.sas.jparest.testutils.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LoggingUtils {

    private LoggingUtils() {
        // no instantiation
    }

    public static void startMemoryAppender(Logger logger, LoggerMemoryAppender loggerMemoryAppender, Level loggerLevel) {
        loggerMemoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(loggerLevel);
        logger.addAppender(loggerMemoryAppender);
        loggerMemoryAppender.start();
    }



}
