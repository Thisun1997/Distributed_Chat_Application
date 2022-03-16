package States;


import Core.Member;
import Core.Room;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class ServerState {
    private static ServerState instance;
    private int votes;
    private Long electionAnswerTimeout;
    private Long electionNominationTimeout;
    private Long electionCoordinatorTimeout;
    private Long consensusVoteDuration;
    private String serverId;
    private String serverAddress;
    private String clientPort;
    private String coordinationPort;
    private String aliveErrorFactor;
    private AtomicBoolean ongoingElection;
    private AtomicBoolean ongoingConsensus;
    private AtomicBoolean leaderUpdateComplete;
    private AtomicBoolean viewMessageReceived;
    private AtomicBoolean answerMessageReceived;
    private BiMap<String, Channel> idMap;
    private Hashtable<String, Integer> suspects;
    private Hashtable<String, Integer> heartbeats;
    private Hashtable<String, String[]> servers;
    private Hashtable<String, Member> members;
    private Hashtable<String, Room> rooms;
    private Hashtable<String,Integer> clientIdApproved;
    private Hashtable<String ,String> approvedJoinRoomServer;
    private Hashtable<String ,Integer> isJoinRoomApproved;
    private Hashtable<String,Integer> isRoomCreationApproved;
    private CopyOnWriteArrayList<String> tempCandidateServers;
    private CopyOnWriteArrayList<Channel> alive;
    private ArrayList<String> lowPriorityServers = new ArrayList<>();
    private ArrayList<String> candidateServers;
    private ArrayList<String> upServers;
    private ArrayList<String> roundRobinServers;
    private Hashtable<String,ArrayList<String>> tempRoomList;


    private ServerState() {
        this.idMap = Maps.synchronizedBiMap(HashBiMap.create());
        this.tempCandidateServers = new CopyOnWriteArrayList<String>();
        this.alive = new CopyOnWriteArrayList<Channel>();
        this.suspects = new Hashtable<String, Integer>();
        this.heartbeats = new Hashtable<String, Integer>();
        this.servers = new Hashtable<String, String[]>();
        this.members = new Hashtable<String, Member>();
        this.ongoingElection = new AtomicBoolean(false);
        this.ongoingConsensus = new AtomicBoolean(false);
        this.leaderUpdateComplete = new AtomicBoolean(false);
        this.viewMessageReceived = new AtomicBoolean(false);
        this.answerMessageReceived = new AtomicBoolean(false);
        this.candidateServers = new ArrayList<String>();
        this.rooms = new Hashtable<String, Room>();
        this.upServers= new ArrayList<String>();
        this.roundRobinServers= new ArrayList<String>();
        this.approvedJoinRoomServer=new Hashtable<String,String>();
        this.isJoinRoomApproved=new Hashtable<String ,Integer>();
        this.isRoomCreationApproved=new Hashtable<String ,Integer>();
        this.clientIdApproved=new Hashtable<String,Integer>();
        this.tempRoomList=new Hashtable<String,ArrayList<String>>();


    }

    public static ServerState getInstance() {
        if (instance == null) {
            synchronized (ServerState.class) {
                if (instance == null) {
                    instance = new ServerState();
                }

            }
        }
        return instance;
    }

    public void setAlive(Channel channel,boolean isAlive) {
        if(isAlive){
           alive.add(channel);
        }
        else{
            alive.remove(channel);
        }
    }

    public boolean getAlive(Channel channel) {
        if(alive.contains(channel)){
            return true;
        }
        else {
            return  false;
        }
    }

    public ArrayList<String> getRoundRobinServers(){
        if(this.roundRobinServers.size()==0){
            ArrayList<String> rrs=new ArrayList<String>();
            rrs.addAll(this.upServers);
            this.roundRobinServers=rrs;
        }
        return this.roundRobinServers;

    }

    public void setRoundRobinServers(String roundRobinServer) {
        this.roundRobinServers.remove(roundRobinServer);
    }

    public void initVotes() {
        this.votes = 0;
    }

    public void setVotes(int vote) {
        this.votes = vote;
    }

    public int getVotes() {
        return votes;
    }

    public String getAliveErrorFactor() {
        return aliveErrorFactor;
    }

    public void setAliveErrorFactor(String aliveErrorFactor) {
        this.aliveErrorFactor = aliveErrorFactor;
    }

    public Hashtable<String, Integer> getSuspects() {
        return suspects;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void downServer(String serverId) {
        this.upServers.remove(serverId);
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public void setClientPort(String port) {
        this.clientPort = port;
    }

    public String getClientPort() {
        return this.clientPort;
    }

    public void setCoordinationPort(String coordinationPort) {
        this.coordinationPort = coordinationPort;
    }

    public String getCoordinationPort() {
        return this.coordinationPort;
    }

    public int getSuspect(String id) {
        if (this.suspects.containsKey(id)) {
            return this.suspects.get(id);
        }
        return 0;
    }

    public void setSuspect(String id, int suspect) {
        this.suspects.put(id, suspect);
    }

    public void removeSuspect(String id) {
        this.suspects.remove(id);
    }

    public void removeHeartbeat(String id) {
        this.heartbeats.remove(id);
    }

    public int getHeartbeat(String id) {
        if(this.heartbeats.containsKey(id)) {
            return this.heartbeats.get(id);
        }
        else {
            return 0;
        }
    }

    public Hashtable<String, Integer> getHeartbeats() {
        return this.heartbeats;
    }

    public void setHeartbeat(String id, int heartbeat) {
        if(heartbeats.containsKey(id)){
            heartbeats.replace(id, heartbeat);
        }
        else{
            heartbeats.put(id,heartbeat);
        }

    }


    public BiMap<String, Channel> getIdMap() {
        return idMap;
    }

    public Hashtable<String, String[]> getServers() {
        return this.servers;
    }

    //local server details must set before set other server info
    public void setServer(String id, String host, String coordinationPort,String clientPort) {
        String[] server = new String[3];
        server[0] = host;
        server[1] = coordinationPort;
        server[2] = clientPort;
        if (getServerId().equals(id)) {
            startServer(id);
        } else {
            if (Integer.parseInt(getServerId()) < Integer.parseInt(id)) {
                candidateServers.add(id);
            } else {
                lowPriorityServers.add(id);
            }
        }
        this.servers.put(id, server);
    }

    public void setMember(String id, Member member) {
        this.members.put(id, member);
    }

    public Member getMember(String id) {
        return members.get(id);
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void setRoom(String roomId,Room room) {
        this.rooms.put(roomId,room);
    }

    public boolean getOngoingElection() {
        return ongoingElection.get();
    }

    public void setOngoingElection(Boolean ongoingElection) {
        this.ongoingElection.set(ongoingElection);
    }

    public boolean getOngoingConsensus() {
        return ongoingConsensus.get();
    }

    public void setOngoingConsensus(Boolean ongoingConsensus) {
        this.ongoingConsensus.set(ongoingConsensus);
    }

    public void initTempCandidateServers() {
        tempCandidateServers = new CopyOnWriteArrayList<String>();
    }

    public String getHighestPriorityCandidate() {
        return Collections.max(this.tempCandidateServers.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList())).toString();

    }

    public void setTempCandidateServer(String tempCandidateServer) {
        this.tempCandidateServers.add(tempCandidateServer);
    }

    public boolean getLeaderUpdateComplete() {
        return leaderUpdateComplete.get();
    }

    public void setLeaderUpdateComplete(boolean updateComplete) {
        this.leaderUpdateComplete.set(updateComplete);
    }

    public boolean getViewMessageReceived() {
        return viewMessageReceived.get();
    }

    public void setViewMessageReceived(boolean viewMessageReceived) {
        this.viewMessageReceived.set(viewMessageReceived);
    }

    public boolean getAnswerMessageReceived() {
        return answerMessageReceived.get();
    }

    public void setAnswerMessageReceived(boolean answerMessageReceived) {
        this.answerMessageReceived.set(answerMessageReceived);
    }

    public ArrayList<String> getCandidateServers() {
        return this.candidateServers;
    }

    public Long getElectionAnswerTimeout() {
        return electionAnswerTimeout;
    }

    public void setElectionAnswerTimeout(Long electionAnswerTimeout) {
        this.electionAnswerTimeout = electionAnswerTimeout;
    }

    public Long getElectionNominationTimeout() {
        return electionNominationTimeout;
    }

    public void removeTempCandidateServer(String serverId) {
        String localId = getServerId();
        if (null != serverId) {
            if (null != localId) {
                tempCandidateServers.remove(serverId);
            }
        }
    }

    public Long getElectionCoordinatorTimeout() {
        return electionCoordinatorTimeout;
    }

    public Hashtable<String, Room> getRooms() {
        return rooms;
    }

    public synchronized ArrayList<String> getClientIdList() {
        ArrayList<String> clientIdList = new ArrayList<String>();
        for (Room room : rooms.values()) {
            clientIdList.addAll(room.getMembers());
        }
        return clientIdList;
    }

    public ArrayList<String> getLowPriorityServers() {
        return lowPriorityServers;
    }

    private synchronized void startServer(String id) {
        Room mainHall = new Room("MainHall-" + id, "");
        this.rooms.put("MainHall-" + id, mainHall);
    }

    public ArrayList<String> getUpServers() {
        return upServers;
    }

    public void setUpServer(String upServer) {
        if(!this.upServers.contains(upServer)) {
            this.upServers.add(upServer);
        }
    }

    public void setElectionCoordinatorTimeout(Long electionCoordinatorTimeout) {
        this.electionCoordinatorTimeout = electionCoordinatorTimeout;
    }

    public void setElectionNominationTimeout(Long electionNominationTimeout) {
        this.electionNominationTimeout = electionNominationTimeout;
    }

    public Long getConsensusVoteDuration() {
        return consensusVoteDuration;
    }

    public void setConsensusVoteDuration(Long consensusVoteDuration) {
        this.consensusVoteDuration = consensusVoteDuration;
    }

    public synchronized void  setTempRoomList(String id,ArrayList<String> tempRoomList) {
        this.tempRoomList.put(id,tempRoomList);
    }

    public synchronized ArrayList<String> getTempRoomList(String id) {
        return tempRoomList.get(id);
    }

    public void setClientIdApproved(String id,int status) {
        this.clientIdApproved.put(id,status);
    }

    public int getClientIdApproved(String id) {
        return this.clientIdApproved.get(id);
    }

    public void removeClientIdApproved(String id){
        this.clientIdApproved.remove(id);
    }

    public void setApprovedJoinRoomServer(String id,String serverId) {
        this.approvedJoinRoomServer.put(id,serverId);
    }

    public String  getApprovedJoinRoomServer(String id) {
        return approvedJoinRoomServer.get(id);
    }
    public void  removeApprovedJoinRoomServer(String id) {
        approvedJoinRoomServer.remove(id);
    }

    public int getIsJoinRoomApproved(String id) {
        return isJoinRoomApproved.get(id);
    }

    public void setIsJoinRoomApproved(String id,int status) {
        this.isJoinRoomApproved.put(id,status);
    }
    public void removeIsJoinRoomApproved(String id) {
        if(approvedJoinRoomServer.containsKey(id)){
            approvedJoinRoomServer.remove(id);
        }

    }
    public void setIsRoomCreationApproved(String channelId,int status){
        isRoomCreationApproved.put(channelId,status);
    }

    public int getIsRoomCreationApproved(String channelId) {
        return isRoomCreationApproved.get(channelId);
    }
    public void removeIsRoomCreationApproved(String channelId) {
        if(isRoomCreationApproved.containsKey(channelId)) {
            isRoomCreationApproved.remove(channelId);
        }
    }
}

