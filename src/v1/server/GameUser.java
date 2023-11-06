package v1.server;

import v1.MSG;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameUser {
    GameServer server = GameServer.getInstance();
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String id;
    private int status = MSG.NOT_READY; // 0: 미준비, 1: 준비

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public void setDos(DataOutputStream dos) {
        this.dos = dos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status == MSG.NOT_READY || status == MSG.READY) {
            this.status = status;
        }
    }

    public GameUser(Socket socket) {
        try {
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
