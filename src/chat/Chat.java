package chat;

import io.socket.SocketIO;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

public class Chat extends Thread {

    private SocketIO socket;
    private ChatCallback callback;

    public Chat(ChatCallbackAdapter callback) {
        this.callback = new ChatCallback(callback);
    }

    @Override
    public void run() {
        try {
            socket = new SocketIO("http://127.0.0.1:8087", callback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("message", message);
            socket.emit("user message", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void sendQueue(String message) {
        socket.emit("q_satu", message);
    }
    
    
        public void sendLewat(String pos,String loket) {
        try {
            JSONObject json = new JSONObject();
            // json.putOpt("message", message);
            json.putOpt("server", "99");
            json.putOpt("antrian", "1");
            json.putOpt("pos", pos);
            json.putOpt("loket", loket);
            socket.emit("lewat", json);
        } catch (JSONException ex) {
        }
    }

    public void sendPanggil(String pos,String loket) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("server", "99");
            json.putOpt("antrian", "1");
            json.putOpt("pos", pos);
            json.putOpt("loket", loket);
            socket.emit("panggil", json);
            
        } catch (JSONException ex) {
        }
    }
    
     public void sendPanggilClient(String id,String message) {
        try {
            JSONObject json = new JSONObject();
            // json.putOpt("message", message);
            json.putOpt("message", message);
            json.putOpt("id", id);
            socket.emit("panggilClient", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

         public void sendcheck(String id) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("phonenumber", id);
            socket.emit("checkin", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        }
    public void join(String nickname) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("nickname", nickname);
            socket.emit("nickname", callback, json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
