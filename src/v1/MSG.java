package v1;

public class MSG {
    public static final int PRINT = 0;
    public static final int REQ_INPUT = 1;
    public static final int READ_INPUT = 2;
    public static final int ID_CREATED = 3;
    public static final int CHANGE_READY_STATE = 4;
    public static final int GAME_START = 5;
    public static final int SKIP = 6;
    public static final int READY = 10;
    public static final int NOT_READY = 11;

    public static final int GAME_PRINT = 30;
    public static final int GAME_YOUR_TURN = 31;
    public static final int GAME_ANSWER_SUBMIT = 32;
    public static final int GAME_ANSWER_RESULT = 33;
    public static final int GAME_LOSE = 34;
    public static final int GAME_OVER = 35;


    static public int getHeader(String msg) {
       return Integer.parseInt(msg.substring(0, 2));
    }

    public static String addHeader(int header, String body) {
        if (header < 10) {
            return "0" + header + body;
        }
        return header + body;
    }

    public static String getBody(String read) {
        if (read.length() > 2) {
            return read.substring(2);
        } else {
            return null;
        }
    }
}
