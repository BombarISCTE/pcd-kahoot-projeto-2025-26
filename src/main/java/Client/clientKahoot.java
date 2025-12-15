package Client;

import Game.Pergunta;

public class clientKahoot {

    public static void main(String[] args) {
//        if (args.length < 5) {
//            System.out.println("Uso: java clientKahoot <IP> <PORT> <GameCode> <TeamCode> <Username>");
//            return;
//        }
//
//        String ip = args[0];
//        int port = Integer.parseInt(args[1]);
//        String gameCode = args[2];
//        //String teamCode = args[3];
//        Team team = new Team(Integer.parseInt(args[3]));
//        String username = args[4];
//
//        //Client client = new Client(ip, port, gameCode, team, username);
//        //client.runClient();

        try{
            Pergunta[] perguntas = Pergunta.lerPerguntas("src/main/resources/Perguntas/FicheiroQuestoes.json");
            Client client = new Client( "localhost", 12345, 123, 1, "Player1");
            ClientGUI gui = new ClientGUI(client, perguntas);//todo passar as perguntas sem respostas pelo server
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}