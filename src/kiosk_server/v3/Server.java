/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kiosk_server.v3;

import chat.CServer;
import chat.ChatCallbackAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

/**
 *
 * @author silaban
 */
public final class Server implements ChatCallbackAdapter {

    private CServer cserver;
    private int vInt = 0;

    private static Jedis jedis = null;
    private static int vLayan = 100;
    private static int vBayar = 200;
    private static int total_antrian = 0;
    private static int sisa_antrian = 0;
    private static String nomor_antrian = "0";
    private static int status_antrian = 0;
    private static int nomor_layanan = 0;

    @Override
    public void callback(JSONArray data) throws JSONException {
    }

    @Override
    public void on(String event, JSONObject obj) {
        try {
            switch (event) {
                case "mulai_antrian":
                    mulai(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    //System.out.println("noloket : " + obj.getString("noloket") + " " + obj.getString("loket") + " - " + obj.getString("pos"));
                    break;
                case "stop_antrian":
                    stop(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    //System.out.println("noloket : " + obj.getString("noloket") + " " + obj.getString("loket") + " - " + obj.getString("pos"));
                    break;
                case "lewat_antrian":
                    lewat(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    //  System.out.println("on lewat : "+obj.getString("pos")+" "+obj.getString("loket"));
                    break;
                case "panggil_antrian":
                    panggil(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    //System.out.println("on panggil_antrian : " + obj.getString("pos") + " " + obj.getString("loket"));
                    break;
                case "layan_antrian":
                    layan(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    // System.out.println("on layan_antrian : " + obj.getString("pos") + " " + obj.getString("noloket") + " " + obj.getString("pos") + " " + obj.getString("noantrian"));
                    break;
                case "siap_antrian":
                    siap(obj.getString("pos"), obj.getString("noloket"), obj.getString("loket"));
                    // System.out.println("on layan_antrian : " + obj.getString("pos") + " " + obj.getString("noloket") + " " + obj.getString("pos") + " " + obj.getString("noantrian"));
                    break;
                case "message_client":
                    System.out.println("dari : " + obj.getString("panggil"));
                    break;
                default:
                    break;
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onMessage(JSONObject json) {
    }

    @Override
    public void onConnect() {
        System.out.println("connected!");
        cserver.join("server");
        cserver.sendcheck("99");
        System.out.println("You joined as SERVER");
    }

    @Override
    public void onDisconnect() {
        System.out.println("Connection lost");
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Server();

            }
        });
    }

    @Override
    public void onConnectFailure() {
        System.out.println("error");
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Server();
            }
        });
    }

    // -----------------------------------------------------------------------------------
    private boolean cekNoAntrian(String pos) {
        String vLabel2 = "";
        boolean vAntrian = false;
        switch (pos) {
            case "1":
                vLabel2 = "q_npwp";
                break;
            case "2":
                vLabel2 = "q_lain";
                break;
            case "3":
                vLabel2 = "q_sppt";
                break;
            case "4":
                vLabel2 = "q_sppt";
                break;
            case "5":
                vLabel2 = "q_sppt";
                break;
            case "6":
                vLabel2 = "q_sppt";
                break;
            default:
                break;
        }
        if (jedis.llen(vLabel2) > 0) {
            vAntrian = true;
        }

        return vAntrian;
    }

    private String countAntrian(String pos) {
        String vLabel2 = "";
        String vAntrian = "0";
        switch (pos) {
            case "1":
                vLabel2 = "q_npwp";
                break;
            case "2":
                vLabel2 = "q_lain";
                break;
            case "3":
                vLabel2 = "q_sppt";
                break;
            case "4":
                vLabel2 = "q_sppt";
                break;
            case "5":
                vLabel2 = "q_sppt";
                break;
            case "6":
                vLabel2 = "q_sppt";
                break;
            default:
                break;
        }
        if (jedis.llen(vLabel2) > 0) {
            vAntrian = String.valueOf(jedis.llen(vLabel2));
        }

        return vAntrian;
    }

    private void TambahSisa(String pos) {
        switch (pos) {
            case "1":
                jedis.incr("sisanpwp");
                break;
            case "2":
                jedis.incr("sisalain");
                break;
            default:
                jedis.incr("sisasppt");
                break;
        }
    }

    private String leftPad(String originalString, int length,
            char padCharacter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(padCharacter);
        }
        String padding = sb.toString();
        String paddedString = padding.substring(originalString.length())
                + originalString;
        return paddedString;
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Server();
            }
        });
    }

    public Server() {
        startChat();
    }

    public void startChat() {
        //jedis = new Jedis("192.168.99.1");
        //cserver = new CServer(this, "http://192.168.99.1:8087");
        
         jedis = new Jedis("127.0.0.1");
        cserver = new CServer(this, "http://127.0.0.1:8087");
        Thread thread = new Thread(cserver);
        thread.start();
    }

    /**
     * @noloket 1 = npwp, 2 = lain, 3 - 4 - 5 - 6= sppt
     * @loket loket01,loket02,loket03,loket04,loket05,loket06
     *
     */
    private void mulai(String pos, String noloket, String loket) {
        //edis.set(loket + "_status", "1");
        //String pos = jedis.get(loket + "_status");
        String totalpos = "0";
        String totallayan = "0";
        //String statusmulai = "0";
        String status = jedis.get(loket + "_status");
        String dnomor = jedis.get(loket + "_no");
        String count = countAntrian(pos);
        switch (pos) {
            case "1":
                totalpos = jedis.get("totalnpwp");
                if (jedis.llen("q_layannpwp") > 0) {
                    totallayan = String.valueOf(jedis.llen("q_layannpwp"));
                } else {
                    if (status.equals("0")) {
                        status = "4";
                    }
                }
                break;
            case "2":
                totalpos = jedis.get("totallain");
                if (jedis.llen("q_layanlain") > 0) {
                    totallayan = String.valueOf(jedis.llen("q_layanlain"));
                } else {
                    if (status.equals("0")) {
                        status = "4";
                    }
                }
                break;

            default:
                totalpos = jedis.get("totalsppt");
                if (jedis.llen("q_layansppt_" + loket) > 0) {
                    totallayan = String.valueOf(jedis.llen("q_layansppt_" + loket));
                } else {
                    if (status.equals("0")) {
                        status = "4";
                    }
                }
                break;
        }
        cserver.sendMulai(pos, noloket, status, dnomor, count, totalpos, totallayan);
        //System.out.println("sendMulai - " + noloket);
        //jedis.publish("realtime", loket + "-on-" + dnomor);
    }

    private void stop(String pos, String noloket, String loket) {
        //jedis.set(loket + "_status", "0");
         String dnomor = jedis.get(loket + "_no");
         jedis.publish("realtime", loket + "-rest-" + dnomor);
        cserver.sendStop(pos, noloket, loket);
        //System.out.println("stop - " + loket);
        //jedis.publish("realtime", loket + "-on-" + dnomor);
    }

    private void panggil(String pos, String noloket, String loketname) {
        /**
         * loketxx_status 1 = panggil/lewat 2 = layan 3 = istirahat
         *
         * loketxx_no = noantrian terakhir
         *
         */
        String loketstatus = "1";
        jedis.set(loketname + "_status", "1");
        String dnomor = jedis.get(loketname + "_no");
        String totalpos;
        jedis.publish("realtime", loketname + "-call-" + dnomor);
        // nomor_antrian = jedis.get(loket + "_no");
        try {
            Thread thread = new Thread(new PlaySound(dnomor, pos, noloket));
            thread.start();

            synchronized (thread) {
                try {
                    while (thread.isAlive()) { //condition guarantees no thread lock occurs
                        thread.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        } finally {
            switch (pos) {
                case "1":
                    totalpos = jedis.get("totalnpwp");
                    cserver.sendBroadcastPanggil("0", "1", countAntrian(pos), dnomor, noloket, loketstatus, "2", totalpos);
                    break;
                case "2":
                    totalpos = jedis.get("totallain");
                    cserver.sendBroadcastPanggil("0", "2", countAntrian(pos), dnomor, noloket, loketstatus, "2", totalpos);
                    break;

                default:
                    totalpos = jedis.get("totalsppt");
                    cserver.sendBroadcastPanggil("0", "3", countAntrian(pos), dnomor, noloket, loketstatus, "2", totalpos);
                    break;
            }
        }
    }

    private void lewat(String pos, String noloket, String loketname) {
        String jml = "0";
        String dnomor = jedis.get(loketname + "_no");
        nomor_antrian = jedis.get(loketname + "_no");
        String loketstatus = jedis.get(loketname + "_status");
        String totalpos = "0";
        // System.out.println("mulai :"+nomor_antrian);
        if (cekNoAntrian(pos)) {
            //System.out.println("error nomor_antrian"+nomor_antrian);
            jedis.set(loketname + "_status", "1");
            loketstatus = "1";
            switch (pos) {
                // layanan
                case "1":
                    try {
                        nomor_antrian = jedis.lpop("q_npwp");
                    } catch (java.lang.NullPointerException ex) {
                        nomor_antrian = dnomor;
                        // System.out.println("error 1"+nomor_antrian);
                    }
                    break;
                case "2":
                    try {
                        nomor_antrian = jedis.lpop("q_lain");
                    } catch (java.lang.NullPointerException ex) {
                        nomor_antrian = dnomor;
                    }
                    break;
                default:
                    try {
                        nomor_antrian = jedis.lpop("q_sppt");
                    } catch (java.lang.NullPointerException ex) {
                        nomor_antrian = dnomor;
                    }
                    break;
            }
            jedis.set(loketname + "_no", nomor_antrian);       
            jedis.publish("realtime", loketname + "-call-" + nomor_antrian);
            try {
                //System.out.println("belum error nomor_antrian"+nomor_antrian);
               
                //System.out.println("error nomor_antrian"+nomor_antrian);
                Thread thread = new Thread(new PlaySound(nomor_antrian, pos, noloket));
                thread.start();

                synchronized (thread) {
                    try {
                        while (thread.isAlive()) { //condition guarantees no thread lock occurs
                            thread.wait();
                        }
                    } catch (InterruptedException e) {
                        //  System.out.println("error 3"+nomor_antrian);
                    }
                 

                }
            } catch (redis.clients.jedis.exceptions.JedisDataException ex2) {
                nomor_antrian = dnomor;
                jedis.set(loketname + "_no", nomor_antrian);
                //System.out.println("error 2" + nomor_antrian);
            }finally{
               switch (pos) {
                        case "1":
                            totalpos = jedis.get("totalnpwp");
                            cserver.sendBroadcastPanggil("0", "1", countAntrian(pos), nomor_antrian, noloket, loketstatus, "1", totalpos);
                            break;
                        case "2":
                            totalpos = jedis.get("totallain");
                            cserver.sendBroadcastPanggil("0", "2", countAntrian(pos), nomor_antrian, noloket, loketstatus, "1", totalpos);
                            break;

                        default:
                            totalpos = jedis.get("totalsppt");
                            cserver.sendBroadcastPanggil("0", "3", countAntrian(pos), nomor_antrian, noloket, loketstatus, "1", totalpos);

                            break;
                    }
            }
            //jedis.set(loketname + "_sts_no", nomor_antrian);
            //System.out.println("error akhir" + nomor_antrian);
            // stat = nomor_antrian;

            //jedis.publish("realtime", loket + "-call-" + nomor_antrian);
//                        try {
//                            if (Integer.valueOf(nomor_antrian) > 0) {
//                                try (DatagramSocket ds = new DatagramSocket()) {
//                                String str = "A-000";   
//                                switch (pos) {
//                                case "1":
//                                     str =  "A-" + leftPad(nomor_antrian, 3, ' ');
//                                    break;
//                                case "2":
//                                  str = "B-" + leftPad(nomor_antrian, 3, ' ');
//                                    break;
//                                case "3":
//                                    str =  "C-" + leftPad(nomor_antrian, 3, ' ');
//                                    break;
//                                default:
//                                    break;
//                            }
//                                    
//                                    InetAddress ia = InetAddress.getByName(ipadd);
//                                    DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ia, 8888);
//                                    ds.send(dp);
//                                } catch (SocketException ex) {
//                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
//                                    jedis.set("panngilno", "0");
//                                } catch (UnknownHostException ex) {
//                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
//                                    jedis.set("panngilno", "0");
//                                } catch (IOException ex) {
//                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
//                                    jedis.set("panngilno", "0");
//                                } finally { //@TODO : baru tadi malam agug
//                                    panggil("sounds/" + loket + "/" + nomor_antrian + ".wav");
//                                }
//                                //stat = nomor_antrian;
//                            }
//                        } catch (java.lang.NumberFormatException ex) {
//                            jedis.set("panngilno", "0");
//                        }
        } else {
            //System.out.println("nomor_antrian" + dnomor);
            switch (pos) {
                case "1":
                    totalpos = jedis.get("totalnpwp");
                    cserver.sendBroadcastPanggil("0", "1", countAntrian(pos), nomor_antrian, noloket, loketstatus, "0", totalpos);
                    break;
                case "2":
                    totalpos = jedis.get("totallain");
                    cserver.sendBroadcastPanggil("0", "2", countAntrian(pos), nomor_antrian, noloket, loketstatus, "0", totalpos);
                    break;

                default:
                    totalpos = jedis.get("totalsppt");
                    cserver.sendBroadcastPanggil("0", "3", countAntrian(pos), nomor_antrian, noloket, loketstatus, "0", totalpos);

                    break;
            }
            //jedis.set("panngilno", "0");
            //stat = "kosong";
        }
    }

    private void layan(String pos, String noloket, String loketname) {
        String noantrian = jedis.get(loketname + "_no");
        String nolayan = "0";
        String countLayan = "0";
        switch (pos) {
            case "1":
                if (jedis.llen("q_layannpwp") > 0) {
                    if (!jedis.lindex("q_layannpwp", 0).equals(noantrian)) {
                        jedis.lpush("q_layannpwp", noantrian);
                    }
                } else {
                    jedis.lpush("q_layannpwp", noantrian);
                }
                countLayan = String.valueOf(jedis.llen("q_layannpwp"));
                break;
            case "2":
                if (jedis.llen("q_layanlain") > 0) {
                    if (!jedis.lindex("q_layanlain", 0).equals(noantrian)) {
                        jedis.lpush("q_layanlain", noantrian);
                    }
                } else {
                    jedis.lpush("q_layanlain", noantrian);
                }
                countLayan = String.valueOf(jedis.llen("q_layanlain"));
                break;

            default:
                if (jedis.llen("q_layansppt_" + loketname) > 0) {
                    if (!jedis.lindex("q_layansppt_" + loketname, 0).equals(noantrian)) {
                        jedis.lpush("q_layansppt_" + loketname, noantrian);
                    }
                } else {
                    jedis.lpush("q_layansppt_" + loketname, noantrian);
                }
                countLayan = String.valueOf(jedis.llen("q_layansppt_" + loketname));
                break;
        }
        //jedis.rpush(loketname+"_layan", noantrian);
        jedis.set(loketname + "_status", "2");
        //jedis.set("panngilno", "1");
        //String dnomor = jedis.get(loketname + "_no");
        // nomor_antrian = jedis.get(loket + "_no");
        jedis.publish("realtime", loketname + "-service-" + noantrian);
        cserver.sendOkeLayan(noantrian, noloket, countLayan);
    }

    private void siap(String pos, String noloket, String loketname) {
        // status 4 - siap layan
        jedis.set(loketname + "_status", "3");
        cserver.sendSiapLayan(noloket, "3");
    }

}
