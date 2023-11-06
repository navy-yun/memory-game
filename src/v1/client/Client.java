package v1.client;

import v1.MSG;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

import static java.lang.System.exit;

public class Client {
    static Socket socket;
    static DataOutputStream dos;
    static DataInputStream dis;
    static String id;
    private static final int BEGIN = 0;
    private static final int LOGIN = 1;
    private static final int GAMMING = 2;
    static int gameState = BEGIN; // 0: 시작, 1: 로그인 후 대기 중, 2: 게임중

    static int ready_state = MSG.NOT_READY;
    static Scanner sc = new Scanner(System.in);

    static Thread thread;
    static Thread mainThread;
    public static void main(String[] args) {
        try {
            mainThread = Thread.currentThread();
            socket = new Socket("localhost", 50001);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            gameStart();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void gameStart() throws IOException {
        while (true) {
            readMsg();
        }
    }

    public static void readMsg() {
        try {
            String read = null;
            read = dis.readUTF();
            int header = MSG.getHeader(read);
            String body = MSG.getBody(read);
            header = filterMessageTypeAccordingToState(header);

            branchMsg(header, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {

        }
    }

    private static void branchMsg(int header, String body) throws InterruptedException {
        switch (header) {
            case MSG.REQ_INPUT:
                printBody(body);
                sendMsg(MSG.READ_INPUT, readInput());
                break;
            case MSG.ID_CREATED:
                gameState = LOGIN;
                id = body;
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            waitForGameStart();   
                        } catch (IllegalStateException | InterruptedException e) {
                            System.out.println("게임 시작 & Read 종료!");
                        }
                    }
                });
                thread.start();
                break;
            case MSG.PRINT:
                printBody(body);
                break;
            case MSG.CHANGE_READY_STATE:
                printBody(body);
                break;
            case MSG.GAME_START:
                playGame();
                break;
            case MSG.SKIP:
                break;
        }
    }

    private static boolean readMsg(int gamming) {

        try {
            String read = null;
            read = dis.readUTF();
            int header = MSG.getHeader(read);
            String body = MSG.getBody(read);
            header = filterMessageTypeAccordingToState(header);

            branchGameCommand(header, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private static void branchGameCommand(int header, String body) {
        switch (header) {
            case MSG.GAME_PRINT:
                printBody(body);
                break;
            case MSG.GAME_ANSWER_SUBMIT:
                printBody(body);
                submitAnswer();
                break;
            case MSG.GAME_ANSWER_RESULT:
                break;

        }
    }

    private static void submitAnswer() {
        makeDelay(200);
        String answer = readInput();
        sendMsg(MSG.GAME_ANSWER_SUBMIT, answer);
    }

    private static void playGame() {
        gameState = GAMMING;
        printBody("[게임을 시작합니다.]");


        while (true) {
            readMsg(GAMMING);
            // 종료조건 필요
        }
    }

    private static void killThread() {
        thread.interrupt();

    }

    private static int filterMessageTypeAccordingToState(int header) {
        if (gameState == LOGIN) {
            switch (header) {
                case MSG.ID_CREATED:
                case MSG.PRINT:
                case MSG.REQ_INPUT:
                    return MSG.SKIP;
            }
        } else if (gameState == GAMMING) {
            switch (header) {
                case MSG.ID_CREATED:
                case MSG.PRINT:
                case MSG.REQ_INPUT:
                    return MSG.SKIP;
            }
            // ...
            // return header;
        }
         return header;
    }

    private static void waitForGameStart() throws InterruptedException {
        while (true) {
            makeDelay(300);
            printMenu();

            String read = readInput();

            if (read.equals("r")) {
                sendMsg(MSG.CHANGE_READY_STATE, String.valueOf(MSG.READY));
                ready_state = MSG.READY;
            } else if (read.equals("n")) {
                sendMsg(MSG.CHANGE_READY_STATE, String.valueOf(MSG.NOT_READY));
                ready_state = MSG.NOT_READY;
            } else if (read.toLowerCase().equals("q")) {
                exit(0);
            } else {
                makeDelay(300);
                if (gameState == GAMMING) {
                    return;
                }
            }
        }
    }

    private static void makeDelay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printMenu() {
        System.out.println("■■■■■■■■■■■■");
        if (ready_state == MSG.NOT_READY) {
            System.out.println("(r) 게임준비");
        } else {
            System.out.println("(n) 준비해제 ");
        }
        System.out.println("(q) 나가기");
    }

    private static void printBody(String body) {
        try {
            System.out.println(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMsg(int header, String body) {
        try {
            String msg = MSG.addHeader(header, body);
            dos.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readInput() {
        try {
            System.out.print(">> ");
            return sc.nextLine();
        } catch (IllegalStateException e) {
            sc = new Scanner(System.in);
            return sc.nextLine();
        } catch (NoSuchElementException e) {
            sc = new Scanner(System.in);
            return sc.nextLine();
        } catch (NullPointerException e) {
            sc = new Scanner(System.in);
            return sc.nextLine();
        }
    }
}
