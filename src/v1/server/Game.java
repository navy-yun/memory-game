package v1.server;

import java.util.ArrayList;

public class Game {
    static GameServer server = GameServer.getInstance();
    public static void main(String[] args) {
        startServer();
        while (true) {

        }
    }

    static void startServer() {

        while (true) {
            // 1. listen connection
            GameUser gameUser = server.listenConnection();
            // 2. delegate new task to new thread.
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(server.addUserTolistAndSayHello(gameUser) == null) {
                        return;
                    }
                    while (true) {
                        try {
                            if (server.readMsg(gameUser).equals("EXIT")) {
                                return;
                            }
                        } catch (NullPointerException e) {
                            e.getMessage();
                        }
                    }
                }
            });
            thread.start();
        }
    }
}
