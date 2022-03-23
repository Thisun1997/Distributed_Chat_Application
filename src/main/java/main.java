import Services.ServerLogger;
import org.apache.log4j.Logger;

public class main {
//    private static Logger logger = ServerLogger.getLogger(System.getenv("localServerId").replace("s",""),main.class);
    private static Logger logger = null;

    public static void main(String[] args)  throws  Exception{
//        logger.info(System.getenv("localServerId") + " started");
        logger = ServerLogger.getLogger(args[0],main.class);
        logger.info(args[0] + " started");
        Config.setup(args[0], args[1]);
        new ChatServer().init();
    }
}
