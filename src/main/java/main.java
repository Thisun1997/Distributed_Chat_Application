import Services.ServerLogger;
import States.ServerState;
import org.apache.log4j.Logger;

public class main {

    public static void main(String[] args)  throws  Exception{
        Logger logger = ServerLogger.getLogger(args[0], main.class);
        logger.info("s" + args[0] + " started");
        if(args[0].equals("1")) {
            Config.setup1();
        }
        else if(args[0].equals("2")) {
            Config.setup2();
        }
        else if(args[0].equals("3")) {
            Config.setup3();
        }
        else{
            Config.setup4();
        }
        new ChatServer().init();
    }
}
