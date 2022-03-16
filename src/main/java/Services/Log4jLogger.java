package Services;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class Log4jLogger {

    private static final Logger logger = LogManager.getLogger(Log4jLogger.class);

    public static void logTrace(String msg){
        logger.trace(msg);
    }
    public static void logInfo(String msg){
        logger.info(msg);
    }
    public static void logError(String msg){
        logger.error(msg);
    }
}
