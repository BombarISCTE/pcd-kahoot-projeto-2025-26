package Client;

import Game.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    //usado para entrar no jogo ?
    public Client(int gameID, Team team, int ClientId, String address, int port) {

    }

    //usado na main clienteKahoot
    public Client (int IP , int Port, Game game, Team team, String username){

    }

    public void runClient(){
        try{
            connectToServer();
            sendMessages();
        } catch (IOException _){
            //System.err.println("IO Exception thrown");
            throw new IllegalStateException("sys kaboom");
        } finally {
            try {
                socket.close();
            } catch (IOException _ ){
                System.err.println("IO Exception thrown");
            }
        }
    }

//    public static void main( String[] args){
//        new Client().runClient();
//    }

}
