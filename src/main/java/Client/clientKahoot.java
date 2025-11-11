package Client;

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
        Team team = new Team(args[3]);
        String username = args[4];

        Client client = new Client(ip, port, gameCode, teamCode, username);
        client.start();
    }
}
