package Server;

import Game.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Server {
    private ArrayList<Game> listOfGames;
    private int port=12345;
    private String IP= "localhost";
    private HashMap<String, Game> activeGames = new HashMap<>();
    private Semaphore semaphore = new Semaphore(5); //todo posso usar ou tenho de criar uma classe semaforo?

    public Server() {
        this.listOfGames = new ArrayList<>();
    }

    public void addGame(Game game) {
        this.listOfGames.add(game);
    }

    private String gameStatistics() {
        return "Not implemented yet"; //todo
    }

    private void nextQuestion() {
        //todo
    }


}
