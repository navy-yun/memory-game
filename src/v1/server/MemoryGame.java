package v1.server;

import java.util.ArrayList;
import java.util.Scanner;

public class MemoryGame {
    static Scanner sc = new Scanner(System.in);
    private ArrayList<GameUser> players;
    private int count = 0;
    private int turn = 0;
    private String memory = "";

    public ArrayList<GameUser> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<GameUser> players) {
        this.players = players;
    }

    public GameUser nextPlayer() {
        return this.players.get(nextTurn());
    }

    private int nextTurn() {
        return ++turn % players.size();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

//    public static void main(String[] args) {
//        String memory = "";
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("제한시간 3초"); // <-- 쓰레드로
//                for (int i = 5; i > 0; i--) {
//                    try {
////                        System.out.println(i);
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        });
//        while (true) {
//            System.out.println();
//            System.out.print(">> ");
//            String input = sc.nextLine();
//
//            if (!checkMemory(input)) {
//                System.out.println("실패!");
//                return;
//            }
//            memory = input;
//
//            System.out.println("3초 후 사라집니다. 숫자를 기억하세요!");
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//
//            for (int i = 0; i < 100; i++) {
//                System.out.println();
//            }
//        }
//    }

    public boolean checkMemory(String input) {
        if (input.substring(0, input.length()-1).equals(this.memory)) {
            return true;
        } else {
            return false;
        }
    }

}
