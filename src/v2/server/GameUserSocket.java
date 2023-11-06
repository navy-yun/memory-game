package v2.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;

public class GameUserSocket {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String id;

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


    public GameUserSocket(Socket socket) {
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

    public void send(String msg) throws IOException {
        getDos().writeUTF(msg);
    }

    public String read() throws IOException {
        return getDis().readUTF();
    }


}
