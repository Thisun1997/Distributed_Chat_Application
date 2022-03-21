import Services.ServerLogger;
import org.apache.log4j.Logger;

public class main {
    private static Logger logger = ServerLogger.getLogger(System.getenv("localServerId").replace("s",""),main.class);

    public static void main(String[] args)  throws  Exception{
        logger.info(System.getenv("localServerId") + " started");
        Config.setup();
        new ChatServer().init();
    }
}
