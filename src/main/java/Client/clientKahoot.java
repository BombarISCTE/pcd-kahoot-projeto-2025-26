package Client;

import Game.Pergunta;
import Game.Team;

public class clientKahoot {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Uso: java clientKahoot <IP> <PORT> <GameCode> <TeamCode> <Username>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String gameCode = args[2];
        //String teamCode = args[3];
        Team team = new Team(Integer.parseInt(args[3]));
        String username = args[4];

        //Client client = new Client(ip, port, gameCode, team, username);
        //client.runClient();

        try{
            Pergunta[] perguntas = Pergunta.lerPerguntas("src/main/java/Game/FicheiroQuestoes.json");
            ClientGUI gui = new ClientGUI(perguntas);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
