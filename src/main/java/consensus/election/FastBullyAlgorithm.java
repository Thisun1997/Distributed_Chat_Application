package consensus.election;

import MessagePassing.MessagePassing;
import Model.Constant;
import Server.Server;
import Server.ServerInfo;
import Server.ServerMessage;
import Services.Quartz;
import consensus.Leader;
import consensus.election.timeout.*;
import org.json.simple.JSONObject;
import org.quartz.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FastBullyAlgorithm implements Runnable{

    String option;
    JobExecutionContext jobExecutionContext = null;
    static JSONObject jsonMessage = null;
    static ServerInfo initiatingServer;
    static ServerInfo iAmUpSender;
    static ServerInfo leader;
    protected Scheduler scheduler;

    public FastBullyAlgorithm(String option){
        this.option = option;
        this.scheduler =  Quartz.getInstance().getScheduler();
    }

    public FastBullyAlgorithm(String option, JobExecutionContext jobExecutionContext){
        this.option = option;
        this.scheduler =  Quartz.getInstance().getScheduler();
        this.jobExecutionContext = jobExecutionContext;
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

    public void sendNominationMessage(String stroption){
        ServerInfo highestPriorityCandidate = Server.getInstance().getHighestPriorityCandidate();
        if(Objects.equals(stroption, "sendNominationAnswerTimeout")){
            try {
                MessagePassing.sendServer(ServerMessage.nominationMessage(), highestPriorityCandidate);
                System.out.println("Sending nomination to : " + highestPriorityCandidate.getServerID());
                startWaitingForCoordinatorMessage(Server.getInstance().getElectionCoordinatorTimeout());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Server.getInstance().setAnswerMessageReceived(false);
        }
        else if (Objects.equals(stroption, "sendNominationCoordinatorTimeout")){
            try {
                MessagePassing.sendServer(ServerMessage.nominationMessage(), highestPriorityCandidate);
                System.out.println("Sending nomination to : " + highestPriorityCandidate.getServerID());
                resetWaitingForCoordinatorMessageTimer(this.jobExecutionContext, this.jobExecutionContext.getTrigger().getKey(),
                        Server.getInstance().getElectionCoordinatorTimeout());
                this.jobExecutionContext = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void startWaitingForCoordinatorMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(CoordinatorMessageTimeout.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    private synchronized void sendCoordinatorMessage(String stroption) {
        String leaderServerID = jsonMessage.get("serverID").toString();
        String leaderAddress = jsonMessage.get("address").toString();
        Integer leaderServerPort = Integer.parseInt( jsonMessage.get("serverPort").toString());
        Integer leaderClientPort = Integer.parseInt( jsonMessage.get("clientPort").toString());
        leader = new ServerInfo(leaderServerID, leaderAddress, leaderServerPort, leaderClientPort);

        ServerInfo SelfServerInfo = Server.getInstance().getSelfServerInfo();
        ConcurrentHashMap<String, ServerInfo> lowPriorityServers = Server.getInstance().getLowPriorityServers();
        ArrayList<ServerInfo> lowPriorityServerList = new ArrayList<>();
        for (String serverID : lowPriorityServers.keySet()) {
            lowPriorityServerList.add(lowPriorityServers.get(serverID));
        }
        if (Objects.equals(stroption, "coordinator")){
            Server.getInstance().setViewMessageReceived(true);
            Server.getInstance().addTempCandidateServer(leader);
            String selfServerID = SelfServerInfo.getServerID();
            leaderServerID = leader.getServerID();
            Integer leadercheck = 0;
            if(Leader.getInstance().getLeaderID() != null){
                leadercheck = Integer.parseInt(Leader.getInstance().getLeaderID());
            }
            if (Integer.parseInt(selfServerID) >= Integer.parseInt(leaderServerID) && Integer.parseInt(selfServerID) >=leadercheck){
                try {
                    MessagePassing.sendServerBroadcast(ServerMessage.setCoordinatorMessage(SelfServerInfo.getServerID(), SelfServerInfo.getAddress(), SelfServerInfo.getServerPort(), SelfServerInfo.getClientPort()),lowPriorityServerList);
                    System.out.println("coordinator message sent"+option);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                acceptNewCoordinator(SelfServerInfo.getServerID());
            }
            else if (Integer.parseInt(selfServerID) < Integer.parseInt(leaderServerID)){
                acceptNewCoordinator(leaderServerID);
            }
            stopWaitingForViewMessage();
        }
        else if (Objects.equals(stroption, "coordinatorAnswerTimeout") || Objects.equals(stroption, "coordinatorViewTimeout")){
            try {
                MessagePassing.sendServerBroadcast(ServerMessage.setCoordinatorMessage(SelfServerInfo.getServerID(), SelfServerInfo.getAddress(), SelfServerInfo.getServerPort(), SelfServerInfo.getClientPort()),lowPriorityServerList);
                System.out.println("coordinator message sent"+stroption);
            } catch (IOException e) {
                e.printStackTrace();
            }
            acceptNewCoordinator(SelfServerInfo.getServerID());
            if (Objects.equals(stroption, "coordinatorAnswerTimeout")){
                stopElection();
            }
            else{
                Server.getInstance().setViewMessageReceived(false);
            }
        }
    }

    public synchronized void acceptNewCoordinator(String serverID) {
//        if(Leader.getInstance().getLeaderID() != null){
//            if(Integer.parseInt(Leader.getInstance().getLeaderID()) < Integer.parseInt(serverID)){
                Leader.getInstance().setLeaderID(serverID);
                Server.getInstance().setOngoingElection(false);
                Server.getInstance().setViewMessageReceived(false);
                Server.getInstance().setAnswerMessageReceived(false);
                System.out.println("accepting new coordinator "+serverID);
//            }
//        }
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

    private synchronized void sendViewMessage() {
        String senderServerID = jsonMessage.get("serverID").toString();
        String senderAddress = jsonMessage.get("address").toString();
        Integer senderServerPort = Integer.parseInt( jsonMessage.get("serverPort").toString());
        Integer senderClientPort = Integer.parseInt( jsonMessage.get("clientPort").toString());
        iAmUpSender = new ServerInfo(senderServerID, senderAddress, senderServerPort, senderClientPort);

        Server.getInstance().addTempCandidateServer(iAmUpSender);
        if(Leader.getInstance().getLeaderID() == null){
            try {
                MessagePassing.sendServer(ServerMessage.viewMessage(senderServerID, senderAddress, senderServerPort, senderClientPort), iAmUpSender);
                System.out.println("View message sent to "+senderServerID+" with leader as "+senderServerID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                if (Objects.equals(Server.getInstance().getSelfServerInfo().getServerID(), Leader.getInstance().getLeaderID())){
                    leader = Server.getInstance().getSelfServerInfo();
                }
                else {
                    leader = Server.getInstance().getOtherServers().get(Leader.getInstance().getLeaderID());
                }
                MessagePassing.sendServer(ServerMessage.viewMessage(leader.getServerID(), leader.getAddress(), leader.getServerPort(), leader.getClientPort()), iAmUpSender);
                System.out.println("View message sent to "+senderServerID+" with leader as "+leader.getServerID());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetWaitingForCoordinatorMessageTimer(JobExecutionContext context, TriggerKey triggerKey, Long timeout) {
        try {
            JobDetail jobDetail = context.getJobDetail();
            if (scheduler.checkExists(jobDetail.getKey())) {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {

                Trigger simpleTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("election_trigger", "group_fast_bully")
                        .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                        .build();
                context.getScheduler().rescheduleJob(triggerKey, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {
            System.out.println(oe.getLocalizedMessage());

            try {

                JobDetail jobDetail = context.getJobDetail();
                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));

                scheduler.triggerJob(jobDetail.getKey());

            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        switch (option){
            case "start_election":
                startElection();
                break;
            case "election":
                replyAnswerForElectionMessage();
                break;
            case "sendNominationAnswerTimeout":
            case "sendNominationCoordinatorTimeout":
                sendNominationMessage(option);
                break;
            case "coordinator":
            case "coordinatorAnswerTimeout":
            case "coordinatorViewTimeout":
                sendCoordinatorMessage(option);
                break;
            case "IamUp":
                sendIamUpMessage();
                break;
            case "sendView":
                sendViewMessage();
                break;
            case "restart_election":
                restartElection();
                break;
        }

    }

    public static void receiveMessage(JSONObject jsonObject) {
        String msgOption = jsonObject.get("option").toString();
        jsonMessage = jsonObject;
        switch(msgOption){
            case "election":
                String initiatingServerID = jsonObject.get("serverID").toString();
                System.out.println("election message from "+ initiatingServerID+" received.");
                FastBullyAlgorithm electionFBA = new FastBullyAlgorithm("election");
                new Thread(electionFBA).start();
                break;
            case "answer":
                String answerServerID = jsonObject.get("serverID").toString();
                Server.getInstance().setAnswerMessageReceived(true);
                ServerInfo answerServerInfo = Server.getInstance().getCandidateServers().get(answerServerID);
                Server.getInstance().addTempCandidateServer(answerServerInfo);
                System.out.println("answer message from "+ answerServerID+" received.");
                break;
            case "nomination":
                FastBullyAlgorithm nominationFBA = new FastBullyAlgorithm("coordinatorFromTimeout");
                new Thread(nominationFBA).start();
                break;
            case "IamUp":
                FastBullyAlgorithm sendViewFBA = new FastBullyAlgorithm("sendView");
//                new Thread(sendViewFBA).start();
                sendViewFBA.sendViewMessage();
                break;
            case "view":
                FastBullyAlgorithm coordinatorFBA = new FastBullyAlgorithm("coordinator");
//                new Thread(coordinatorFBA).start();
                coordinatorFBA.sendCoordinatorMessage(coordinatorFBA.option);
                break;
            case "coordinator":
                FastBullyAlgorithm stopFBA = new FastBullyAlgorithm("stop");
                stopFBA.stopElection();
                Server.getInstance().setViewMessageReceived(true);
                String leaderServerID = jsonObject.get("serverID").toString();
                Integer leadercheck = 0;
                if(Leader.getInstance().getLeaderID() != null){
                    leadercheck = Integer.parseInt(Leader.getInstance().getLeaderID());
                }
                if(Integer.parseInt(leaderServerID) > leadercheck){
                    String leaderAddress = jsonObject.get("address").toString();
                    Integer leaderServerPort = Integer.parseInt( jsonObject.get("serverPort").toString());
                    Integer leaderClientPort = Integer.parseInt( jsonObject.get("clientPort").toString());
                    leader = new ServerInfo(leaderServerID, leaderAddress, leaderServerPort, leaderClientPort);
                    synchronized (Leader.getInstance()){
                        stopFBA.acceptNewCoordinator(leaderServerID);
                    }
                }
                break;
        }
    }

    public static void initialize(){
        FastBullyAlgorithm startFBA = new FastBullyAlgorithm("start_election");
        new Thread(startFBA).start();
    }
}
