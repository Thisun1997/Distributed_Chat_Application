package consensus;

import Server.Server;

public class Leader {
    private String leaderID;

    private static Leader leaderInstance;

    private Leader(){

    }

    public static synchronized Leader getInstance(){
        if (leaderInstance == null){
            leaderInstance = new Leader();
        }
        return leaderInstance;
    }

}
