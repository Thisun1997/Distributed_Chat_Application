package Services;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

public class ServerLogger {
    private static ServerLogger serverLogger;
    private static final Layout newLayout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
    private static final ConsoleAppender consoleAppender = new ConsoleAppender(newLayout);
    private static FileAppender fileAppender;


    private ServerLogger(String selfID){
        File logFile = new File("Logs/" + "s"+selfID + ".log");
        try {
            fileAppender = new FileAppender(newLayout, logFile.getAbsolutePath(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized ServerLogger getServerLogger(String selfID){
        if (serverLogger == null){
            serverLogger = new ServerLogger(selfID);
        }
        return serverLogger;
    }

    public static synchronized Logger getLogger(String selfID, Class c){
        Logger logger = Logger.getLogger(c);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(ServerLogger.getServerLogger(selfID).getFileAppender());
        logger.addAppender(ServerLogger.getServerLogger(selfID).getConsoleAppender());
        return logger;
    }

    private static FileAppender getFileAppender() {
        return fileAppender;
    }

    private static ConsoleAppender getConsoleAppender() {
        return consoleAppender;
    }
}
