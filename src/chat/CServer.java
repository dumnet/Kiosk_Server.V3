/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

/**
 *
 * @author silaban
 */
import io.socket.SocketIO;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

public class CServer implements Runnable {

    private SocketIO socket;
    private String ipadd = "http://127.0.0.1:8087";
    private final ChatCallback callback;


    public CServer(ChatCallbackAdapter callback,String ipadd) {        
        this.callback = new ChatCallback(callback);   
         this.ipadd = ipadd;
    }

    @Override
    public void run() {
        try {
            socket = new SocketIO(ipadd, callback);
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

    public void sendPanggil(String message) {
        try {
            JSONObject json = new JSONObject();
            // json.putOpt("message", message);
            json.putOpt("panggil", message);
            json.putOpt("loket", "1");
            socket.emit("panggil", json);
            //socket.emit("q_satu", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
     public void sendMulai(String pos, String noloket, String status, String noantrian, String count, String total, String layan) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", noloket);
            json.putOpt("pos", pos);
            json.putOpt("status", status);
            json.putOpt("noantrian", noantrian);
            json.putOpt("count", count);
            json.putOpt("total", total);
            json.putOpt("layan", layan);
            socket.emit("mulai_callback", json);
        } catch (JSONException ex) {
           // ex.printStackTrace();
        }
    }
     
      
     public void sendStop(String pos, String noloket, String loket) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", noloket);
            json.putOpt("pos", pos);
            json.putOpt("status", true);
            json.putOpt("loket", loket);
            socket.emit("stop_callback", json);
        } catch (JSONException ex) {
           // ex.printStackTrace();
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
        }
    }
     
        public void sendBroadcastPanggil(String message,String pos, String count,String noantrian,String noloket,String status,String jml,String total) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", noloket);
            json.putOpt("antrian", message);            
            json.putOpt("noantrian", noantrian);
            json.putOpt("pos", pos);
            json.putOpt("count", count);
            json.putOpt("status", status);
            json.putOpt("jml", jml);
            json.putOpt("total", total);
            socket.emit("broadcast_end_panggil", json);
        } catch (JSONException ex) {
        }
    }
        
    public void sendOkeLayan(String noantrian,String noloket,String countlayan) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", noloket);        
            json.putOpt("noantrian", noantrian);
            json.putOpt("countlayan", countlayan);
            socket.emit("okelayan", json);
        } catch (JSONException ex) {
        }
    }
    
       public void sendSiapLayan(String noloket,String status) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", noloket);   
            json.putOpt("status", status);   
            socket.emit("siaplayan", json);
        } catch (JSONException ex) {
        }
    }

         public void sendcheck(String id) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("id", id);
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
