package consensus.election;

import MessagePassing.MessagePassing;
import Model.Constant;
import Server.Server;
import Server.ServerInfo;
import Server.ServerMessage;
import consensus.Leader;
import org.quartz.impl.StdSchedulerFactory;
import consensus.election.timeout.*;
import org.json.simple.JSONObject;
import org.quartz.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FastBullyAlgorithm implements Runnable{

    String option;
    static ServerInfo initiatingServer;
    static ServerInfo iAmUpSender;
    protected Scheduler scheduler;

    public FastBullyAlgorithm(String option){
        this.option = option;
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public void startElection(){
        Server server = Server.getInstance();

        server.initTempCandidateServers();
        server.setAnswerMessageReceived(false);
        server.setOngoingElection(true);
        initiatingServer = server.getSelfServerInfo();
        ConcurrentHashMap<String, ServerInfo> candidateServers = server.getCandidateServers();

        Long electionTimeOut = server.getElectionAnswerTimeout();

        ArrayList<ServerInfo> CandidateServerList = new ArrayList<>();
        for (String serverID : candidateServers.keySet()) {
                CandidateServerList.add(candidateServers.get(serverID));
            }

        try {
            MessagePassing.sendServerBroadcast(ServerMessage.getElection(initiatingServer.getServerID()),CandidateServerList);
            System.out.println("election message sent.");
            startWaitingForAnswerMessage(electionTimeOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startWaitingForAnswerMessage(Long timeout) {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(AnswerMessageTimeout.class).withIdentity
                        ("answer_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, answerMsgTimeoutJob);
    }

    public void replyAnswerForElectionMessage(){
        ServerInfo selfServerInfo = Server.getInstance().getSelfServerInfo();
        try {
            MessagePassing.sendServer(ServerMessage.getAnswer(selfServerInfo.getServerID()),initiatingServer);
            System.out.println("answer message sent.");
            startWaitingForNominationOrCoordinationMessage(Server.getInstance().getElectionAnswerTimeout());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startWaitingForNominationOrCoordinationMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(NominationMessageTimeout.class).withIdentity
                        ("coordinator_or_nomination_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    public void sendNominationMessage(){
        ServerInfo highestPriorityCandidate = Server.getInstance().getHighestPriorityCandidate();
        try {
            MessagePassing.sendServer(ServerMessage.nominationMessage(), highestPriorityCandidate);
            System.out.println("Sending nomination to : " + highestPriorityCandidate.getServerID());
            startWaitingForCoordinatorMessage(Server.getInstance().getElectionCoordinatorTimeout());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Server.getInstance().setAnswerMessageReceived(false);
    }

    public void startWaitingForCoordinatorMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(CoordinatorMessageTimeout.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    private void sendCoordinatorMessage() {
        ServerInfo coordinatorServerInfo = Server.getInstance().getSelfServerInfo();
        ConcurrentHashMap<String, ServerInfo> lowPriorityServers = Server.getInstance().getLowPriorityServers();
        ArrayList<ServerInfo> lowPriorityServerList = new ArrayList<>();
        for (String serverID : lowPriorityServers.keySet()) {
            lowPriorityServerList.add(lowPriorityServers.get(serverID));
        }

        try {
            MessagePassing.sendServerBroadcast(ServerMessage.setCoordinatorMessage(coordinatorServerInfo.getServerID(), coordinatorServerInfo.getAddress(), coordinatorServerInfo.getServerPort(), coordinatorServerInfo.getClientPort()),lowPriorityServerList);
            System.out.println("coordinator message sent");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo());
        stopElection();
    }

    private void restartElection() {
        stopElection();
        startElection();
    }

    public void stopElection() {

        Server.getInstance().initTempCandidateServers();
        Server.getInstance().setOngoingElection(false);

        stopWaitingForAnswerMessage();
        stopWaitingForCoordinatorMessage();
        stopWaitingForNominationMessage();
        stopWaitingForViewMessage();
    }

    public void stopWaitingForAnswerMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForNominationMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("coordinator_or_nomination_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForCoordinatorMessage() {
        JobKey coordinatorMsgTimeoutJobKey = new JobKey("coordinator_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(coordinatorMsgTimeoutJobKey);
    }

    public void stopWaitingForViewMessage() {
        JobKey viewMsgTimeoutJobKey = new JobKey("view_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(viewMsgTimeoutJobKey);
    }

    private void startWaitingTimer(String groupId, Long timeout, JobDetail jobDetail) {
        try {

            System.out.println(String.format("Starting the waiting job [%s] : %s",
                    scheduler.getSchedulerName(), jobDetail.getKey()));

            if (scheduler.checkExists(jobDetail.getKey())) {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {
                SimpleTrigger simpleTrigger =
                        (SimpleTrigger) TriggerBuilder.newTrigger()
                                .withIdentity(Constant.ELECTION_TRIGGER, groupId)
                                .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                                .build();

                scheduler.scheduleJob(jobDetail, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {

            try {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

                //System.err.println(Arrays.toString(scheduler.getTriggerKeys(GroupMatcher.anyGroup()).toArray()));
                // [DEFAULT.MT_e8f718prrj3ol, group1.GOSSIPJOBTRIGGER, group1.CONSENSUSJOBTRIGGER, group_fast_bully.ELECTION_TRIGGER]

            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void stopWaitingTimer(JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.interrupt(jobKey);
                //scheduler.deleteJob(jobKey);
                System.out.println(String.format("Job [%s] get interrupted from [%s]",
                        jobKey, scheduler.getSchedulerName()));
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is Boot time only election timer job. In main
     */
    public void startWaitingForViewMessage(Long electionAnswerTimeout) throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(ViewMessageTimeout.class).withIdentity
                        ("view_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", electionAnswerTimeout, coordinatorMsgTimeoutJob);
    }

    public void sendIamUpMessage() {
        Server server = Server.getInstance();
        ServerInfo selfServerInfo = server.getSelfServerInfo();
        ConcurrentHashMap<String, ServerInfo> otherServers = server.getOtherServers();
        ArrayList<ServerInfo> otherServersList = new ArrayList<>();
        for (String serverID : otherServers.keySet()) {
            otherServersList.add(otherServers.get(serverID));
        }
        try {
            MessagePassing.sendServerBroadcast(ServerMessage.iAmUpMessage(selfServerInfo.getServerID(), selfServerInfo.getAddress(),
                    selfServerInfo.getServerPort(), selfServerInfo.getClientPort()),otherServersList);
            System.out.println("IamUp message sent");
            try {
                startWaitingForViewMessage(Server.getInstance().getElectionAnswerTimeout());
            } catch (SchedulerException e) {
                System.out.println("Error while waiting for the view message at fast bully election: " +
                        e.getLocalizedMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public void sendViewMessage(ServerInfo sender, ServerInfo coordinator) {
//        if (null == coordinator) {
//            // in the beginning coordinator could be null
//            coordinator = sender;
//        }
//        String viewMessage = jsonMessageBuilder.viewMessage(coordinator.getServerId(), coordinator.getAddress(),
//                coordinator.getPort(), coordinator.getManagementPort());
//        peerClient.commPeerOneWay(sender, viewMessage);
//    }


    @Override
    public void run() {
        switch (option){
            case "start_election":
                startElection();
                break;
            case "election":
                replyAnswerForElectionMessage();
                break;
            case "nomination":
                sendNominationMessage();
                break;
            case "coordinator":
                sendCoordinatorMessage();
                break;
            case "IamUp":
                sendIamUpMessage();
                break;
            case "view":
                sendViewMessage();
                break;
            case "restart_election":
                restartElection();
                break;
        }

    }

    private void sendViewMessage() {
    }

    public static void receiveMessage(JSONObject jsonObject) {
        String msgOption = jsonObject.get("option").toString();
        switch(msgOption){
            case "election":
                String initiatingServerID = jsonObject.get("serverID").toString();
                System.out.println("election message from "+ initiatingServerID+" received.");
                FastBullyAlgorithm electionFBA = new FastBullyAlgorithm("election");
                new Thread(electionFBA).start();
            case "answer":
                String answerServerID = jsonObject.get("serverID").toString();
                Server.getInstance().setAnswerMessageReceived(true);
                ServerInfo answerServerInfo = Server.getInstance().getCandidateServers().get(answerServerID);
                Server.getInstance().addTempCandidateServer(answerServerInfo);
                System.out.println("answer message from "+ answerServerID+" received.");
            case "nomination":
                FastBullyAlgorithm nominationFBA = new FastBullyAlgorithm("nomination");
                new Thread(nominationFBA).start();
            case "IamUp":
                synchronized (Leader.getInstance()){
                    String senderServerID = jsonObject.get("serverID").toString();
                    String senderAddress = jsonObject.get("address").toString();
                    int senderServerPort = (int) jsonObject.get("serverPort");
                    int senderClientPort = (int) jsonObject.get("clientPort");
                    iAmUpSender = new ServerInfo(senderServerID, senderAddress, senderServerPort, senderClientPort);
                    FastBullyAlgorithm iAmUpFBA = new FastBullyAlgorithm("nomination");
                    iAmUpFBA.sendViewMessage();
                }

        }
    }

    public static void initialize(){
        FastBullyAlgorithm startFBA = new FastBullyAlgorithm("start_election");
        new Thread(startFBA).start();
    }
}
