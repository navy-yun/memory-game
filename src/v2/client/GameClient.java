package v2.client;

import v2.Cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;
    private static String id;
    private static boolean isReady;
    private static Scanner sc = new Scanner(System.in);

    static {
        try {
            socket = new Socket("localhost", 50001);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public static void main(String[] args) {
        createId();
        getReady();
        standBy();
        playGame();
    }

    private static void playGame() {
        String read;
        while (true) {
            read = read();

            switch (read) {
                case Cmd.TURN:
                    // 타임아웃
                    System.out.print("입력>> ");
                    send(sc.nextLine());
                    break;
                case Cmd.GAME_OVER:
                    System.out.println(read());
                    return;
                default:
                    System.out.println(read);
                    break;
            }
        }
    }

    private static void createId() {
        String read = read();
        System.out.println("사용하실 ID를 입력하세요");
        do {
            System.out.print(">> ");
            send(sc.nextLine());
            read = read();
        } while (!read.equals(Cmd.REQ_READY));
    }

    private static void getReady() {

        boolean flag = true;

        System.out.println("(1) READY");
        System.out.println("(2) EXIT");

        while (flag) {
            System.out.print(">> ");
            String select = sc.nextLine();

            switch (select) {
                case "1":
                    flag = false;
                    send(Cmd.RES_READY);
                    break;
                case "2":
                    exitGame();
                    break;
                default:
                    break;
            }
        }
    }

    private static void exitGame() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void standBy() {
        System.out.println("standBy");
        String read = read();
        while (!read.equals(Cmd.START_GAME)) {
            System.out.println("대기중...");
            read = read();
        }

    }

    public static void send(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
