package memorygame;

import java.util.Scanner;

public class Memory {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        String memory = "";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("제한시간 3초"); // <-- 쓰레드로
                for (int i = 5; i > 0; i--) {
                    try {
//                        System.out.println(i);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        while (true) {
//            thread.start();
            System.out.println();
            System.out.print(">> ");
            String input = sc.nextLine();

            if (!checkMemory(memory, input)) {
                System.out.println("실패!");
                return;
            }
            memory = input;

            System.out.println("3초 후 사라집니다. 숫자를 기억하세요!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            for (int i = 0; i < 100; i++) {
                System.out.println();
            }
        }
    }

    private static boolean checkMemory(String memory, String input) {
        if (input.substring(0, input.length()-1).equals(memory)) {
            return true;
        } else {
            return false;
        }
    }
}
