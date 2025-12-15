package Client;

import Game.Pergunta;
import Messages.*;
import Utils.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private String IP;
    private int PORT;
    private int gameId;
    private int teamId;
    private String username; //identificador individual
    private ClientListener listener;

    public Client (Socket socket, String IP , int port, int gameId, int teamId, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.IP = IP;
            this.PORT = port;
            this.gameId = gameId;
            this.teamId = teamId;
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

//    public void connectToServer() {
//        try {
//            bufferedWriter.write(this);
//            bufferedWriter.newLine();
//            bufferedWriter.flush();
//        } catch (IOException e) {
//            closeEverything(socket, bufferedReader, bufferedWriter);
//        }
//    }



    public void run(){
        try{
            connectToServer();
            enviarJoinGame();
            listenForMessage();
        } catch (Exception e){
            System.err.println("Erro no cliente");
        } finally {
           closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void connectToServer() {
        try {
            InetAddress endereco = InetAddress.getByName(IP);
            System.out.println("Endereço do servidor: " + endereco.getHostAddress());

            socket = new Socket(endereco, PORT);
            System.out.println("Ligação estabelecida ao servidor: " + IP + ":" + PORT);

            System.out.println("Socket:" + socket);
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectOut.flush();
            objectIn = new ObjectInputStream(socket.getInputStream());
            System.out.println("Ligado ao servidor");
        }catch (IOException e) {
            System.err.println("Falha em estabelecer ligação ao servidor: " + IP + " - " + e.getMessage());
        }
    }

    private void enviarJoinGame() {
        JoinGame joinGame = new JoinGame(username, teamId, gameId);
        try {
            objectOut.writeObject(joinGame);
            objectOut.flush();
            System.out.println("JoinGame enviado ao servidor");
        } catch (IOException e) {
            System.err.println("Erro ao enviar JoinGame");
        }
    }

    private void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pergunta pergunta; // todo passar uma pergunta

                while (socket.isConnected()) {
                    try {
                        pergunta = (Pergunta) objectIn.readObject();
                        System.out.println("Mensagem recebida do servidor: " + pergunta);
                    } catch (IOException | ClassNotFoundException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);

                    }
                }
            }
        }).start();

    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    private void tratarNovaQuestao(NewQuestion novaQuestao) {
        if(listener != null){
            listener.onNewQuestion(novaQuestao.getPergunta(), novaQuestao.getOpcoes(), novaQuestao.getNumeroPergunta(), novaQuestao.getTempoLimite());
        }
    }



    public void enviarResposta(int opcao) {
        try{
            Answer resposta = new Answer(username, teamId, opcao);
            objectOut.writeObject(resposta);
            objectOut.flush();
            System.out.println("Resposta enviada: " + opcao);
        }catch (IOException e) {
            System.err.println("Erro ao enviar resposta");
        }
    }



    public String getUsername() {
        return username;
    }
    public int getGameId() {
        return gameId;
    }

    @Override
    public String toString() {
        return "Client{" +
                "username='" + username + '\'' +
                ", gameId='" + gameId  +
                '}';
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String ip = Constants.SERVER_IP; //sc.nextLine();
        int port = Constants.SERVER_PORT; //Integer.parseInt(sc.nextLine());
        int gameId = 123; //sc.nextLine();
        int teamId = 1; //Integer.parseInt(sc.nextLine());
        System.out.print("Enter your username: ");
        String username = sc.nextLine();
        Client client = new Client(new Socket(), ip, port, gameId, teamId, username);
        ClientGUI gui = new ClientGUI(client);

    }

}
