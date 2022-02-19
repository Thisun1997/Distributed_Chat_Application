package Server;

import Client.ClientThread;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private String serverID;
    private String address;
    private Integer serverPort;
    private Integer clientPort;
    private ServerInfo selfServerInfo;
    private final ConcurrentHashMap<Long, ClientThread> clientThreadList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Room> roomList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ServerInfo> otherServers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ServerInfo> candidateServers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ServerInfo> lowPriorityServers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ServerInfo> tempCandidateServers = new ConcurrentHashMap<>();

    private AtomicBoolean ongoingElection = new AtomicBoolean(false);;
    private AtomicBoolean answerMessageReceived = new AtomicBoolean(false);;
    private AtomicBoolean viewMessageReceived = new AtomicBoolean(false);;
    private Long electionAnswerTimeout;
    private Long electionCoordinatorTimeout;

    private Long electionNominationTimeout;

    private static Server serverInstance;

    private Server() {
    }

    public static synchronized Server getInstance(){
        if (serverInstance == null){
            serverInstance = new Server();
        }
        return serverInstance;
    }

    public String getAddress() {
        return address;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public ServerInfo getSelfServerInfo() {
        return selfServerInfo;
    }

    public void setSelfServerInfo(ServerInfo serverInfo) {
        this.selfServerInfo = serverInfo;
    }

    public ConcurrentHashMap<String, ServerInfo> getCandidateServers() {
        return candidateServers;
    }

    public ConcurrentHashMap<String, ServerInfo> getLowPriorityServers() {
        return lowPriorityServers;
    }

    public ConcurrentHashMap<String, ServerInfo> getOtherServers() {
        return otherServers;
    }

    private void startServer(ServerInfo serverInfo){
        this.serverID = serverInfo.getServerID();
        this.address = serverInfo.getAddress();
        this.serverPort = serverInfo.getServerPort();
        this.clientPort = serverInfo.getClientPort();

        Room mainHall = new Room(getMainHallID(serverID),serverID,"");
        roomList.put(getMainHallID(serverID), mainHall);
    }

    public void addServer(String selfID, String serverID, String address, Integer serverPort, Integer clientPort){
        ServerInfo serverInfo = new ServerInfo(serverID, address, serverPort, clientPort);
        if(Objects.equals(selfID, serverID)){
            startServer(serverInfo);
            setSelfServerInfo(serverInfo);
        }
        else{
            if(Integer.parseInt(selfID) < Integer.parseInt(serverID)){
                candidateServers.put(serverID,serverInfo);
            }
            else{
                lowPriorityServers.put(serverID,serverInfo);
            }
            otherServers.put(serverID, serverInfo);
        }
    }

    public String getMainHallID(String serverID) {
        return "MainHall-"+serverID;
    }

    public void addClient(ClientThread clientThread, Thread thread){
        clientThreadList.put(thread.getId(),clientThread);
    }

    public synchronized void initTempCandidateServers() {
        tempCandidateServers = new ConcurrentHashMap<>();
    }

    public synchronized ServerInfo getHighestPriorityCandidate() {
        Integer max = 0;
        for (String key: tempCandidateServers.keySet()){
            if(Integer.parseInt(key) > max){
                max = Integer.parseInt(key);
            }
        }
        return tempCandidateServers.get(Integer.toString(max));
    }

    public synchronized void addTempCandidateServer(ServerInfo serverInfo) {
        ServerInfo selfServerInfo = getSelfServerInfo();
        if (null != serverInfo) {
            if (null != selfServerInfo) {
                if (Integer.parseInt(selfServerInfo.getServerID()) < Integer.parseInt(serverInfo.getServerID())) {
                    tempCandidateServers.put(serverInfo.getServerID(), serverInfo);
                }
            }
        }
    }

    public boolean getOngoingElection() {
        return ongoingElection.get();
    }

    public void setOngoingElection(boolean ongoingElection) {
        this.ongoingElection.set(ongoingElection);
    }

    public boolean getAnswerMessageReceived() {
        return answerMessageReceived.get();
    }

    public void setAnswerMessageReceived(boolean answerMessageReceived) {
        this.answerMessageReceived.set(answerMessageReceived);
    }

    public boolean getViewMessageReceived() {
        return viewMessageReceived.get();
    }

    public void setViewMessageReceived(boolean viewMessageReceived) {
        this.viewMessageReceived.set(viewMessageReceived);
    }

    public Long getElectionAnswerTimeout() {
        return electionAnswerTimeout;
    }

    public void setElectionAnswerTimeout(Long electionAnswerTimeout) {
        this.electionAnswerTimeout = electionAnswerTimeout;
    }

    public Long getElectionCoordinatorTimeout() {
        return electionCoordinatorTimeout;
    }

    public void setElectionCoordinatorTimeout(Long electionCoordinatorTimeout) {
        this.electionCoordinatorTimeout = electionCoordinatorTimeout;
    }

    public Long getElectionNominationTimeout() {
        return electionNominationTimeout;
    }

    public void setElectionNominationTimeout(Long electionNominationTimeout) {
        this.electionNominationTimeout = electionNominationTimeout;
    }

}

