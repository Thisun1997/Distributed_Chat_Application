package Server;

import Client.ClientThread;
import consensus.Leader;
import consensus.election.FastBullyAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {


        String selfID = args[0];
        String mode = args[1];

        String configFile = "src/main/config/serverConfig.txt";
        File conf = new File(configFile); // read configuration
        Scanner myReader = null;
        try {
            myReader = new Scanner(conf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Server server = Server.getInstance();
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] params = data.split(" ");
            server.addServer(selfID, params[0], params[1], Integer.parseInt(params[2]), Integer.parseInt(params[3]));
        }

        System.out.println("LOG  : ------server started------");

        try {
            // throw exception if invalid server id provided
            if( Server.getInstance().getAddress() == null ) {
                throw new IllegalArgumentException();
            }

            /**
             Coordination socket
             **/
            // server socket for coordination
            ServerSocket serverCoordinationSocket = new ServerSocket();

            // bind SocketAddress with inetAddress and port
            SocketAddress endPointCoordination = new InetSocketAddress(
                    Server.getInstance().getAddress(),
                    Server.getInstance().getServerPort()
            );
            serverCoordinationSocket.bind( endPointCoordination );
            System.out.println( serverCoordinationSocket.getLocalSocketAddress() );
            System.out.println( "LOG  : TCP Server waiting for coordination on port " +
                    serverCoordinationSocket.getLocalPort() ); // port open for coordination

            /**
             Client socket
             **/
            // server socket for clients
            ServerSocket serverClientsSocket = new ServerSocket();

            // bind SocketAddress with inetAddress and port
            SocketAddress endPointClient = new InetSocketAddress(
                    Server.getInstance().getAddress(),
                    Server.getInstance().getClientPort()
            );
            serverClientsSocket.bind(endPointClient);
            System.out.println(serverClientsSocket.getLocalSocketAddress());
            System.out.println("LOG  : TCP Server waiting for clients on port "+
                    serverClientsSocket.getLocalPort()); // port open for clients

            /**
             Handle coordination
             **/
            ServerThread serverThread = new ServerThread( serverCoordinationSocket );
            // starting the thread
            Thread Server_t = new Thread(serverThread);
            if(Integer.parseInt(mode) == 2){
                Thread.sleep(10000);
            }
            Server_t.start();


            /**
             Maintain consensus using Bully Algorithm
             **/
            // T2
            Server.getInstance().setElectionAnswerTimeout(10L);
            // T3
            Server.getInstance().setElectionCoordinatorTimeout(10L);
            // T4
            Server.getInstance().setElectionNominationTimeout(30L);
            initiateCoordinator(Integer.parseInt(mode));


//            Runnable heartbeat = new BullyAlgorithm("Heartbeat");
//            new Thread(heartbeat).start();

            /**
             Heartbeat detection using gossiping
             **/
//            startGossip();
//            Runnable gossip = new GossipJob();
//            new Thread(gossip).start();
//            if (isGossip) {
//                System.out.println("INFO : Failure Detection is running GOSSIP mode");
//                startGossip();
//                startConsensus();
//            }


            /**
             Handle clients
             **/
            while (true) {
                Socket clientSocket = serverClientsSocket.accept();
                ClientThread clientThread = new ClientThread( clientSocket );
                Thread client_t = new Thread(clientThread);
                // starting the thread
                Server.getInstance().addClient(clientThread, client_t);
                client_t.start();
            }
        }
        catch( IllegalArgumentException e ) {
            System.out.println("ERROR : invalid server ID");
        }
        catch ( IndexOutOfBoundsException e) {
            System.out.println("ERROR : server arguments not provided");
            e.printStackTrace();
        }
        catch ( IOException e) {
            System.out.println("ERROR : occurred in main " + Arrays.toString(e.getStackTrace()));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void initiateCoordinator(Integer mode) {
        System.out.println("INFO : leader election started");
        if (Server.getInstance().getOtherServers().isEmpty()){
            //self is the leader
        }
        else{
            if(mode == 1){
                FastBullyAlgorithm IamUp_FBA = new FastBullyAlgorithm("IamUp");
                new Thread(IamUp_FBA).start();
            }
//            IamUp_FBA.sendIamUpMessage();
            else if(mode == 2){
                if (Integer.parseInt(Server.getInstance().getSelfServerInfo().getServerID()) == 1){
                    FastBullyAlgorithm.initialize();
                }
            }
        }
    }


}



