package consensus.election;


import Core.Room;
import Messages.*;
import Protocols.Client;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import consensus.election.timeout.*;
import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

import Services.Quartz;

public class FastBullyAlgorithm implements Runnable{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), FastBullyAlgorithm.class);
    String option;
    JobExecutionContext jobExecutionContext = null;
    static CoordinationMessage message;
    static String initiatingServer;
    static String leader;
    static int leaderCheck;
    protected Scheduler scheduler;


    public FastBullyAlgorithm(String option,CoordinationMessage... message){
        this.option = option;
        this.scheduler =  Quartz.getInstance().getScheduler();
        if(message.length>0) {
            this.message = message[0];
        }
    }

    public FastBullyAlgorithm(String option, JobExecutionContext jobExecutionContext){
        this.option = option;
        this.scheduler =  Quartz.getInstance().getScheduler();
        this.jobExecutionContext = jobExecutionContext;
    }


    public void startElection(){

        LeaderState.getInstance().setLeaderID(null);
        ServerState.getInstance().initTempCandidateServers();
        ServerState.getInstance().setAnswerMessageReceived(false);
        ServerState.getInstance().setOngoingElection(true);
        ServerState.getInstance().setLeaderUpdateComplete(false);
        LeaderState.getInstance().reset();

        initiatingServer = ServerState.getInstance().getServerId();
        ArrayList<String> candidateServers =ServerState.getInstance().getCandidateServers();

        Long electionTimeOut = ServerState.getInstance().getElectionAnswerTimeout();


        Client.broadcast(new ElectionMessage(initiatingServer),candidateServers);
        logger.info("Election message sent by the server "+ServerState.getInstance().getServerId());
        startWaitingForAnswerMessage(electionTimeOut);

    }

    public void startWaitingForAnswerMessage(Long timeout) {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(AnswerMessageTimeout.class).withIdentity
                        ("answer_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, answerMsgTimeoutJob);
    }

    public void replyAnswerForElectionMessage(){
        LeaderState.getInstance().setLeaderID(null);
        ServerState.getInstance().setOngoingElection(true);
        LeaderState.getInstance().reset();
        ElectionMessage electionMessage=(ElectionMessage) message;
        String initiatingServerId = electionMessage.getId();
        logger.info("Election message from "+ initiatingServerId+" received.");
        String selfServerId = ServerState.getInstance().getServerId();
        try {
            Client.send(initiatingServerId, new AnswerMessage(selfServerId),true);
            logger.info("Answer message sent to "+initiatingServerId);
        }
        catch (Exception e){
            logger.warn("Unable to send the answer message to "+initiatingServerId+" caused by : "+e);
        }
        startWaitingForNominationOrCoordinationMessage(ServerState.getInstance().getElectionNominationTimeout());
    }

    private void startWaitingForNominationOrCoordinationMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(NominationMessageTimeout.class).withIdentity
                        ("coordinator_or_nomination_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    public void sendNominationMessage(String strOption){
        if(Objects.equals(strOption, "sendNominationAnswerTimeout")){
            String highestPriorityCandidateId = ServerState.getInstance().getHighestPriorityCandidate();
            try {
                Client.send(highestPriorityCandidateId,new NominationMessage(),true);
                logger.info("Sending nomination to server " + highestPriorityCandidateId+" after answer message timeout");
            } catch (Exception e) {
                logger.warn("Unable to send the nomination message to server "+highestPriorityCandidateId+" caused by : "+e);
                ServerState.getInstance().removeTempCandidateServer(highestPriorityCandidateId);
            }
            startWaitingForCoordinatorMessage(ServerState.getInstance().getElectionCoordinatorTimeout());
            ServerState.getInstance().setAnswerMessageReceived(false);
        }
        else if (Objects.equals(strOption, "sendNominationCoordinatorTimeout")){
            String highestPriorityCandidateId = ServerState.getInstance().getHighestPriorityCandidate();
            try {
                Client.send(highestPriorityCandidateId,new NominationMessage(),true);
                logger.info("Sending nomination to : " + highestPriorityCandidateId+" after coordinator or nominator message timeout");
            } catch (Exception e) {
                logger.warn("Unable to send the nomination message to server "+highestPriorityCandidateId+" caused by : "+e);
                ServerState.getInstance().removeTempCandidateServer(highestPriorityCandidateId);
            }
            resetWaitingForCoordinatorMessageTimer(this.jobExecutionContext, this.jobExecutionContext.getTrigger().getKey(),
                    ServerState.getInstance().getElectionCoordinatorTimeout());
            this.jobExecutionContext = null;
        }

    }

    public void startWaitingForCoordinatorMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(CoordinatorMessageTimeout.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    private synchronized void sendCoordinatorMessage(String strOption) {
        String selfServerId  = ServerState.getInstance().getServerId();
        ArrayList<String> lowPriorityServerList = ServerState.getInstance().getLowPriorityServers();
        if (strOption.equals("coordinator")){
            ViewMessage viewMessage=(ViewMessage) message;
            String leaderServerId = viewMessage.getId();
            ServerState.getInstance().setViewMessageReceived(true);
            ServerState.getInstance().setTempCandidateServer(leaderServerId);

            logger.info("View message received with leader as "+leaderServerId);
            leaderCheck = 0;
            if(LeaderState.getInstance().getLeaderID() != null){
                leaderCheck = Integer.parseInt(LeaderState.getInstance().getLeaderID());
            }
            if (Integer.parseInt(selfServerId) >= Integer.parseInt(leaderServerId) && Integer.parseInt(selfServerId) >=leaderCheck){
                Client.broadcast(new CoordinatorMessage(selfServerId),lowPriorityServerList);
                logger.info("Coordinator message sent ["+option+"] with leader as "+selfServerId);
                acceptNewLeader(selfServerId);
            }
            else if (Integer.parseInt(selfServerId) < Integer.parseInt(leaderServerId)){
                acceptNewLeader(leaderServerId);
            }
            stopWaitingForViewMessage();
        }
        else if (strOption.equals("coordinatorAnswerTimeout") || Objects.equals(strOption, "coordinatorViewTimeout") || Objects.equals(strOption, "coordinatorFromNomination")){
            Client.broadcast(new CoordinatorMessage(selfServerId),lowPriorityServerList);
            logger.info("Coordinator message sent ["+strOption +"] with leader as "+selfServerId);
            acceptNewLeader(selfServerId);
            if (Objects.equals(strOption, "coordinatorAnswerTimeout") || Objects.equals(strOption, "coordinatorFromNomination")){
                stopElection();
            }
            else{
                ServerState.getInstance().setViewMessageReceived(false);
            }
        }
    }

    public synchronized void acceptNewLeader(String serverId) {
        LeaderState.getInstance().setLeaderID(serverId);
        ServerState.getInstance().setOngoingElection(false);
        ServerState.getInstance().setViewMessageReceived(false);
        ServerState.getInstance().setAnswerMessageReceived(false);
        logger.info("Accepting new leader server "+serverId);


        ArrayList<Room> roomList = new ArrayList<>();
        try {
            for(Room room: ServerState.getInstance().getRooms().values()){
                Room r=room.clone();
                r.setMemberChannels(null);
                roomList.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(Integer.parseInt(ServerState.getInstance().getServerId()) == Integer.parseInt(serverId)) {
            LeaderState.getInstance().updateLeader(ServerState.getInstance().getServerId(), ServerState.getInstance().getClientIdList(), roomList);
            ServerState.getInstance().setLeaderUpdateComplete(true);
        }
        else{
            if(Integer.parseInt(ServerState.getInstance().getServerId()) < Integer.parseInt(serverId)){
                LeaderState.getInstance().reset();
            }
            Client.send(serverId,new LeaderUpdateMessage(ServerState.getInstance().getServerId(),ServerState.getInstance().getClientIdList(),roomList),true);
            logger.info("Send information to new leader "+serverId);
        }
      }

    private void restartElection() {
        logger.info("Restarting the election");
        stopElection();
        startElection();
    }

    public void stopElection() {
        ServerState.getInstance().initTempCandidateServers();
        ServerState.getInstance().setOngoingElection(false);
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

    private synchronized void startWaitingTimer(String groupId, Long timeout, JobDetail jobDetail) {
        try {

            logger.debug(String.format("Starting the waiting job [%s] : %s",
                    scheduler.getSchedulerName(), jobDetail.getKey()));

            if (scheduler.checkExists(jobDetail.getKey())) {

                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {
                SimpleTrigger simpleTrigger =
                        (SimpleTrigger) TriggerBuilder.newTrigger()
                                .withIdentity("election_trigger", groupId)
                                .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                                .build();

                scheduler.scheduleJob(jobDetail, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {

            try {

                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

                //System.err.println(Arrays.toString(scheduler.getTriggerKeys(GroupMatcher.anyGroup()).toArray()));
                // [DEFAULT.MT_e8f718prrj3ol, group1.GOSSIPJOBTRIGGER, group1.CONSENSUSJOBTRIGGER, group_fast_bully.ELECTION_TRIGGER]

            } catch (SchedulerException e) {
                logger.warn("Scheduler exception : "+e);
            }

        } catch (SchedulerException e) {
            logger.warn("Scheduler exception : "+e);
        }
    }

    public void stopWaitingTimer(JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.interrupt(jobKey);
                //scheduler.deleteJob(jobKey);
                logger.debug(String.format("Job [%s] get interrupted from [%s]",
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
        ServerState.getInstance().setOngoingElection(true);
        LeaderState.getInstance().reset();
        String selfServerId = ServerState.getInstance().getServerId();
        Hashtable<String, String[]> otherServers = ServerState.getInstance().getServers();
        ArrayList<String> otherServersList = new ArrayList<>();
        otherServersList.addAll(otherServers.keySet());
        otherServersList.remove(selfServerId);
        Client.broadcast(new IAmUpMessage(selfServerId),otherServersList);
        logger.info("IamUp messages sent");
        try {
            startWaitingForViewMessage(ServerState.getInstance().getElectionAnswerTimeout());
        } catch (SchedulerException e) {
            logger.warn("Error while waiting for the view message at fast bully election: " +
                    e.getLocalizedMessage());
        }

    }

    private synchronized void sendViewMessage() {
        IAmUpMessage iAmUpMessage=(IAmUpMessage) message;
        String senderServerId = iAmUpMessage.getId();

        ServerState.getInstance().setTempCandidateServer(senderServerId);
        if(LeaderState.getInstance().getLeaderID() == null){
            try {
                Client.send(senderServerId,new ViewMessage(senderServerId),true);
                logger.info("View message sent to "+senderServerId+" with leader as "+senderServerId);
            } catch (Exception e) {
                logger.warn("Unable to send the view message to server "+senderServerId+ " caused by : "+e);
            }
        }
        else{
            try {
                if (Objects.equals(ServerState.getInstance().getServerId(), LeaderState.getInstance().getLeaderID())){
                    leader = ServerState.getInstance().getServerId();
                }
                else {
                    leader = LeaderState.getInstance().getLeaderID();
                }
                Client.send(senderServerId,new ViewMessage(leader),true);
                logger.info("View message sent to "+senderServerId+" with leader as "+leader);
            } catch (Exception e) {
                logger.warn("Unable to send the view message to server "+senderServerId+ " caused by : "+e);
            }
        }
    }

    public void resetWaitingForCoordinatorMessageTimer(JobExecutionContext context, TriggerKey triggerKey, Long timeout) {
        try {
            JobDetail jobDetail = context.getJobDetail();
            if (scheduler.checkExists(jobDetail.getKey())) {

                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {

                Trigger simpleTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("election_trigger", "group_fast_bully")
                        .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                        .build();
                context.getScheduler().rescheduleJob(triggerKey, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {
            logger.trace(oe.getLocalizedMessage());

            try {

                JobDetail jobDetail = context.getJobDetail();
                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));

                scheduler.triggerJob(jobDetail.getKey());

            } catch (SchedulerException e) {
                logger.warn("Scheduler exception : "+e);
            }

        } catch (SchedulerException e) {
            logger.warn("Scheduler exception : "+e);
        }
    }

    public void updateLeader(){
        stopElection();
        ServerState.getInstance().setViewMessageReceived(true);
        ServerState.getInstance().setLeaderUpdateComplete(false);
        CoordinatorMessage coordinatorMessage= (CoordinatorMessage) message;
        String leaderServerID = coordinatorMessage.getId();
        acceptNewLeader(leaderServerID);

    }

    @Override
    public void run() {
        switch (option){
            case "start_election":
                startElection();
                break;
            case "election":
//                if(!ServerState.getInstance().getOngoingElection()) {
                    replyAnswerForElectionMessage();
                    break;
//                }
            case "sendNominationAnswerTimeout":
            case "sendNominationCoordinatorTimeout":
                sendNominationMessage(option);
                break;
            case "coordinator":
            case "coordinatorAnswerTimeout":
            case "coordinatorViewTimeout":
            case "coordinatorFromNomination":
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
            case "updateLeader":
                updateLeader();
                break;
        }

    }

    public static void initialize(){
        if(!ServerState.getInstance().getOngoingElection()) {
            FastBullyAlgorithm startFBA = new FastBullyAlgorithm("start_election");
            new Thread(startFBA).start();
        }
    }
}
