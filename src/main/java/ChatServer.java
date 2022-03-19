import Core.Room;
import Heartbeat.Consensus;
import Heartbeat.Gossip;
import Protocols.ClientServer;
import Protocols.CoordinationServer;
import Protocols.Server;
import Services.Quartz;
import States.ServerState;
import consensus.election.FastBullyAlgorithm;
import org.quartz.*;


public class ChatServer {
    ChatServer() {
    }

    public void init() throws Exception {
        try {
            ServerState serverState = ServerState.getInstance();
            String mainHallID="MainHall-" + ServerState.getInstance().getServerId();
            ServerState.getInstance().setRoom(mainHallID,new Room(mainHallID,""));
            String coordinationPort = serverState.getCoordinationPort();
            Server coordinationServer = new CoordinationServer(coordinationPort);
            coordinationServer.start();
            String clientPort = serverState.getClientPort();
            Server clientServer = new ClientServer(clientPort);
            clientServer.start();

            initCoordinator();
            initGossip();
            initConsensus();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void initGossip() {
        try {
            JobDetail gossipJob = JobBuilder.newJob(Gossip.class)
                    .withIdentity("gossip", "group1").build();

            Trigger consensusTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("gossip_trigger", "group1")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(3).repeatForever())
                    .build();

            Scheduler scheduler = Quartz.getInstance().getScheduler();

            scheduler.start();
            scheduler.scheduleJob(gossipJob, consensusTrigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void initConsensus() {
        try {
            JobDetail consensusJob = JobBuilder.newJob(Consensus.class)
                    .withIdentity("consensus", "group1").build();


            Trigger consensusTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("consensus_trigger", "group1")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(10).repeatForever())
                    .build();

            Scheduler scheduler = Quartz.getInstance().getScheduler();

            scheduler.start();
            scheduler.scheduleJob(consensusJob, consensusTrigger);
        } catch (Exception e) {

        }
    }

    private static void initCoordinator() {
        FastBullyAlgorithm IamUp_FBA = new FastBullyAlgorithm("IamUp");
        new Thread(IamUp_FBA).start();
    }
}
