package v2.server;

import v2.Cmd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {
    static ServerSocket serverSocket;
    static HashMap<String, GameUserSocket> userList;
    static HashMap<String, String> userIds;
    static String[] orders;
    static int readyCount = 0;

    static {
        try {
            serverSocket = new ServerSocket(50001);
            userList = new HashMap<>();
            userIds = new HashMap<>();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
    public static void main(String[] args) {
        start();
    }
    public static void start() {

        try {
            log("서버를 시작합니다...");
            while (true) {
                Socket userSocket = serverSocket.accept();
                Thread userThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // id 요구
                        try {
                            GameUserSocket gameUser = new GameUserSocket(userSocket);

                            gameUser.send(Cmd.REQ_ID);
                            String id = gameUser.read();

                            while (isDuplicated(id)) {
                                log("[" + id + "] 아이디가 중복되었습니다.");
                                log("새로운 ID를 다시 요구합니다.");
                                // 중복되면 다시
                                gameUser.send(Cmd.REQ_ID);
                                id = gameUser.read();
                            }

                            log("[" + id + "]님께서 접속하였습니다!");
                            addUser(id, gameUser);

                            gameUser.send(Cmd.REQ_READY);
                            String ready = gameUser.read();
                            if (ready.equals(Cmd.RES_READY)) {
                                log("[" + id + "] READY!");
                                readyCount++;
                            }

                            // 2. 클라이언트들의 준비 상태 체크
                            if (canPlay()) {
                                // 클라이언트들은 여기서 대기중
                                sendAll(Cmd.START_GAME);
                                // 마지막 스레드가 실행함
                                playGame();
                            };

                            log("스레드 종료.");

                        } catch (IOException | InterruptedException e) {
                            //removeUser(userIds.get(Thread.currentThread()));
                            throw new RuntimeException(e);
                        }
                    }
                });
                userThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addUser(String id, GameUserSocket gameUser) {
        gameUser.setId(id);
        userList.put(id, gameUser);
        userIds.put(Thread.currentThread().getName(), gameUser.getId());
        showInfo();
    }
    private static void removeUser(String id) {
        System.out.println(userIds.get(Thread.currentThread()));
        userList.remove(id);
        showInfo();
    }

    private static void showInfo() {
        log("접속 중인 사용자 : " + userList.size());
        log("준비 상태 사용자 : " + readyCount);
    }

    private static void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
//        System.out.println("[" + LocalTime.now() + "] " + msg);
    }

    private static void playGame() throws IOException, InterruptedException {
        // 게임모드
        System.out.println("게임실행");
        // theadeInfo();
        // 턴을 랜덤하게 정함
        // 순서 보여주기
        orders = new String[readyCount];
        sendAll(shuffleOrder(orders));
        sendAll("3초 후에 게임시작 하겠습니다...");
        countDown();

        int count = 0;
        String answer = "";
        String memory = "";
        GameUserSocket player;
        while (true) {
            player = nextTurn(count);
            count++;
            // 턴인 사람은 문제를 푸는 동안 다른 사람들에게 3초간 메시지 전달.
            answer = player.read();

            if (checkAnswer(answer, memory) == false) {
                sendAll(Cmd.GAME_OVER);
                sendAll("게임종료! [" + player.getId() + "]님이 패배하셨습니다.");
                break;
            }

            // 모든 플레이어에게 결과 보여주기
            memory = answer;
            showAnswer(answer);
            countDown();
        }

        System.out.println();
    }

    private static void showAnswer(String answer) throws IOException {
        sendAll("정답!  " + answer);
    }

    private static void countDownExceptPlayer(GameUserSocket player) throws InterruptedException, IOException {
        Thread.sleep(1000);
        sendAllExcept(player, "3...");
        Thread.sleep(1000);
        sendAllExcept(player, "2...");
        Thread.sleep(1000);
        sendAllExcept(player, "1...");
        sendAllExcept(player,"\n".repeat(50));
    }

    private static void countDown() throws InterruptedException, IOException {
        Thread.sleep(1000);
        sendAll("3...");
        Thread.sleep(1000);
        sendAll("2...");
        Thread.sleep(1000);
        sendAll("1...");
        sendAll("\n".repeat(50));
    }

    private static boolean checkAnswer(String answer, String memory) {
        if (memory.length() < 2) {
            return true;
        }
        if (answer.substring(0, answer.length()-1).equals(memory)) {
            return true;
        } else {
            return false;
        }
    }

    private static GameUserSocket nextTurn(int count) throws IOException {
        GameUserSocket player = userList.get(orders[count % readyCount]);
        player.send(Cmd.TURN);
        return player;
    }

    private static String shuffleOrder(String[] orders) {
        String order = "■■■■■ 게임순서 ■■■■■\n";
        int count = 1;
        HashMap<String, GameUserSocket> newUserList = new HashMap<>();
        List keys = new ArrayList(userList.keySet());
        Collections.shuffle(keys);
        for (Object id : keys) {
            // Access keys/values in a random order
            newUserList.put((String) id, userList.get(id));
            orders[count-1] = (String) id;
            order += "(" + count++ + ") " + (String) id + "\n";
        }

        log(order);
        userList = newUserList;
        return order;
    }

    private static void theadeInfo() {
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        Set<Thread> threads = threadMap.keySet();
        for (Thread thread : threads) {
            System.out.println(thread.getName());
        }
    }

    public static void sendAll(String msg) throws IOException {
        Set<String> keys = userList.keySet();
        Iterator<String> iterator = keys.iterator();
        while( iterator.hasNext() ) {
            String id = iterator.next();
            GameUserSocket gameUser = userList.get(id);
            gameUser.send(msg);
        }
    }

    private static void sendAllExcept(GameUserSocket player, String msg) throws IOException {
        Set<String> keys = userList.keySet();
        Iterator<String> iterator = keys.iterator();
        while( iterator.hasNext() ) {
            String id = iterator.next();
            GameUserSocket gameUser = userList.get(id);
            if (player != gameUser) {
                gameUser.send(msg);
            }
        }
    }

    private static boolean canPlay() {
        showInfo();
        if (userList.size() >= 2 && readyCount == userList.size()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isDuplicated(String id) {
        return userList.containsKey(id);
    }

}
