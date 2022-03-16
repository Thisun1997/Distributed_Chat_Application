import States.ServerState;

import java.util.Hashtable;

public class Config {

 public static void setup1(){
  ServerState serverState=ServerState.getInstance();
  serverState.setElectionAnswerTimeout(10L);
  serverState.setElectionCoordinatorTimeout(10L);
  serverState.setElectionNominationTimeout(30L);
  serverState.setConsensusVoteDuration(5L);
  serverState.setServerId("1");
  serverState.setServer("1","localhost","8070","4444");
  serverState.setServer("2","localhost","8000","7990");
  serverState.setServer("3","localhost","7000","4000");
  serverState.setServer("4","localhost","6000","3000");
  serverState.setAliveErrorFactor("4");
  serverState.setServerAddress("localhost");
  serverState.setCoordinationPort("8070");
  serverState.setClientPort("4444");

 }
 public static void setup2(){
  ServerState serverState=ServerState.getInstance();
  serverState.setElectionAnswerTimeout(10L);
  serverState.setElectionCoordinatorTimeout(10L);
  serverState.setElectionNominationTimeout(30L);
  serverState.setConsensusVoteDuration(5L);
  serverState.setServerId("2");
  serverState.setServer("1","localhost","8070","4444");
  serverState.setServer("2","localhost","8000","7990");
  serverState.setServer("3","localhost","7000","4000");
  serverState.setServer("4","localhost","6000","3000");
  serverState.setAliveErrorFactor("4");
  serverState.setServerAddress("localhost");
  serverState.setCoordinationPort("8000");
  serverState.setClientPort("7990");

 }
 public static void setup3(){
  ServerState serverState=ServerState.getInstance();
  serverState.setElectionAnswerTimeout(10L);
  serverState.setElectionCoordinatorTimeout(10L);
  serverState.setElectionNominationTimeout(30L);
  serverState.setConsensusVoteDuration(5L);
  serverState.setServerId("3");
  serverState.setServer("1","localhost","8070","4444");
  serverState.setServer("2","localhost","8000","7990");
  serverState.setServer("3","localhost","7000","4000");
  serverState.setServer("4","localhost","6000","3000");
  serverState.setAliveErrorFactor("4");
  serverState.setServerAddress("localhost");
  serverState.setCoordinationPort("7000");
  serverState.setClientPort("4000");

 }
 public static void setup4(){
  ServerState serverState=ServerState.getInstance();
  serverState.setElectionAnswerTimeout(10L);
  serverState.setElectionCoordinatorTimeout(10L);
  serverState.setElectionNominationTimeout(30L);
  serverState.setConsensusVoteDuration(5L);
  serverState.setServerId("4");
  serverState.setServer("1","localhost","8070","4444");
  serverState.setServer("2","localhost","8000","7990");
  serverState.setServer("3","localhost","7000","4000");
  serverState.setServer("4","localhost","6000","3000");
  serverState.setAliveErrorFactor("4");
  serverState.setServerAddress("localhost");
  serverState.setCoordinationPort("6000");
  serverState.setClientPort("3000");

 }
}
