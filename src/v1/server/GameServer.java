package v1.server;

import v1.MSG;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {
    static private GameServer server = new GameServer();
    public static GameServer getInstance() {
        return server;
    }
    private GameServer() {
        try {
            serverSocket = new ServerSocket(50001);
            gameUsers = new HashMap<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static ServerSocket serverSocket;
    static HashMap<String, GameUser> gameUsers;
    boolean isRun;

    GameUser listenConnection() {
        try {
            Socket userSocket = serverSocket.accept();
            GameUser gameUser = new GameUser(userSocket);

            return gameUser;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    GameUser addUserTolistAndSayHello(GameUser gameUser) {
        String id = requestUserId(gameUser);
        if (id.equals("EXIT")) {
            return null;
        }
        gameUsers.put(id, gameUser);
        sendHello(gameUser.getId());
        return gameUser;
    }

    private void sendHello(String newUserId) {
        String gameInfo = getGameInfo();
        Set<String> keys = gameUsers.keySet();
        Iterator<String> iterator = keys.iterator();
        while( iterator.hasNext() ) {
            String id = iterator.next();
            GameUser gameUser = gameUsers.get(id);
            sendMsg(gameUser, MSG.PRINT, "[" + newUserId +"] 님이 접속하셨습니다.");
            sendMsg(gameUser, MSG.PRINT, gameInfo);
        }
    }
    private void sendAll(int header, String msg) {
        Set<String> keys = gameUsers.keySet();
        Iterator<String> iterator = keys.iterator();
        while( iterator.hasNext() ) {
            String id = iterator.next();
            GameUser gameUser = gameUsers.get(id);
            sendMsg(gameUser, header, msg);
        }
    }
    private void sendMsg(GameUser gameUser, int header, String body) {
        try {
            String msg = MSG.addHeader(header, body);
            gameUser.getDos().writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getGameInfo() {
        int countUsers = gameUsers.size();
        int countReady = countReady();

        String info = "접속중인 유저: " + countUsers + "명\n" +
                      "준비중인 유저: " + countReady + "명";

        return info;
    }

    private int countReady() {
        int count = 0;

        Set<String> keys = gameUsers.keySet();
        Iterator<String> iterator = keys.iterator();
        while( iterator.hasNext() ) {
            String id = iterator.next();
            GameUser gameUser = gameUsers.get(id);
            if (gameUser.getStatus() == MSG.READY) {
                ++count;
            }
        }
        return count;
    }

    private String requestUserId(GameUser gameUser) {
        try {
            //gameUser.getDos().writeUTF("사용하실 아이디를 입력하세요.\n");
            sendMsg(gameUser, MSG.REQ_INPUT, "사용하실 아이디를 입력하세요.");

            String id = null;
            do {
                id = readMsg(gameUser);
            } while (id == null);

            while (!checkValidId(id)) {
                sendMsg(gameUser, MSG.REQ_INPUT, "사용할 수 없는 아이디입니다. 다시 입력해주세요.");
                id = readMsg(gameUser);
            }

            gameUser.setId(id);
            sendMsg(gameUser, MSG.PRINT, "[" + id + "]님 환영합니다!");
            sendMsg(gameUser, MSG.ID_CREATED, id);
            return id;

        } catch (Exception e) {
            return "EXIT";
//            throw new RuntimeException(e);
        }
    }

    public String readMsg(GameUser gameUser) {
        String read = null;
        try {
            read = gameUser.getDis().readUTF();
        } catch (IOException e) {
            removeUserFromlist(gameUser);
            gameUser.close();
            return "EXIT";
//            throw new RuntimeException(e);
        }

        int header = MSG.getHeader(read);
        String body = MSG.getBody(read);

        switch (header) {
            case MSG.PRINT:
                break;
            case MSG.REQ_INPUT:
                break;
            case MSG.READ_INPUT:
                return body;
            case MSG.CHANGE_READY_STATE:
                // 상태 변경
                gameUser.setStatus(Integer.parseInt(body));
                sendMsg(gameUser, MSG.CHANGE_READY_STATE, getGameInfo());
                // 게임 가능 여부 판단
                if (canStartGame()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startGame();
                        }
                    }).start();
                }
                break;
            default:
                break;
        }

        return null;
    }

    private void removeUserFromlist(GameUser gameUser) {
        gameUsers.remove(gameUser.getId());
    }

    private void startGame() {
        try {
            System.out.println("게임시작");
            sendAll(MSG.GAME_START, "");
            Thread.sleep(300);

            playGame();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void playGame() {
        System.out.println(Thread.currentThread());

        MemoryGame game = initGame();
        sendAll(MSG.GAME_PRINT, "게임을 시작합니다.");

        while (true) {

            GameUser currentPlayer = game.nextPlayer();
            // 플레이어의 입력받기
            sendMsg(currentPlayer, MSG.GAME_ANSWER_SUBMIT, "3초 안에 정답을 입력하세요.");
            // 플레이어의 정답받기
            String answer = readFromGame(currentPlayer);
            // 정답 체크
            boolean isCorrect = game.checkMemory(answer);

            if (isCorrect == false) {
                sendMsg(currentPlayer, MSG.GAME_LOSE, "");
                sendAll(MSG.GAME_PRINT, "게임이 종료되었습니다.");
                sendAll(MSG.GAME_OVER, "");
            }
        }
    }

    private String readFromGame(GameUser currentPlayer) {
        String read = null;
        try {
            read = currentPlayer.getDis().readUTF();
        } catch (IOException e) {
            removeUserFromlist(currentPlayer);
            currentPlayer.close();
        }

        int header = MSG.getHeader(read);
        String body = MSG.getBody(read);

        switch (header) {
            case MSG.GAME_ANSWER_RESULT:
                System.out.println("정답 : " + body);
                return body;
            default:
                break;
        }
        return null;
    }

    private MemoryGame initGame() {
        MemoryGame game = new MemoryGame();
        game.setMemory("");
        game.setPlayers(randomizeTurns());

        return game;
    }

    private ArrayList<GameUser> randomizeTurns() {
        ArrayList<GameUser> players = new ArrayList<GameUser>(gameUsers.values());
        Collections.shuffle(players);
        return players;
    }

    private boolean canStartGame() {
        int countUsers = gameUsers.size();
        int countReady = countReady();

        if (countUsers >= 2 && countReady == countUsers) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValidId(String id) {
        if (id == null) {
            return false;
        }
        if (gameUsers.get(id) == null) {
            return true;
        } else {
            return false;
        }
    }

}
