/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kiosk_server.v3.backup;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import redis.clients.jedis.Jedis;
/**
 *
 * @author silaban
 */




/**
 *
 * @author agung
 */
public class Kiosk_ServerV3 implements Runnable {
    
    
    static String[] nomina={"","satu","dua","tiga","empat","lima","enam","tujuh","delapan","sembilan","sepuluh","sebelas"};
    
   static String[] soundN={"s1","1","2","3","4","5","6","7","8","9","10","11"};
   static ArrayList<String> soundsP = new ArrayList<String>();
 
    
    DatagramSocket clientSocket;

    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];

    InetAddress IPAddress;

    DatagramPacket sendPacket;

    DatagramPacket receivePacket;

    String sentence;
    
    
    

    private static Jedis jedis = null;
    private static int vLayan = 100;
    private static int vBayar = 200;
    private static int total_antrian = 0;
    private static int sisa_antrian = 0;
    private static String nomor_antrian = "0";
    private static int status_antrian = 0;
    private static int nomor_layanan = 0;

    private final Socket m_socket;
    private final int m_num;

    Kiosk_ServerV3(Socket socket, int num) {
        m_socket = socket;
        m_num = num;

        Thread handler = new Thread(this, "handler-" + m_num);
        handler.start();
    }

    public static void main(String argv[]) {
        int port = 6789;
        System.out.println("Accepting connections on port: " + port);
        int nextNum = 1;

        try {

            ServerSocket welcomeSocket = new ServerSocket(port);
            jedis = new Jedis("127.0.0.1");

            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                Kiosk_ServerV3 ks = new Kiosk_ServerV3(connectionSocket, nextNum++);
            }
        } catch (IOException ex) {
            Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String execAntrian(String loket, String type, String pos, String noloket, String ipadd) {
        String stat = "wait";
     
        if (jedis.get("panngilno").equals("1")) {
            //jedis.set("panngilno", "1");
            //System.out.println("blok suara");
        } else {

            switch (type) {
                case "DEFAULT": //Default
                    // jedis.set(loket + "_status", "0");
                    try {
                        nomor_antrian = jedis.get(loket + "_no");
                    } catch (redis.clients.jedis.exceptions.JedisDataException ex2) {
                        jedis.set(loket + "_no", "0");
                        nomor_antrian = jedis.get(loket + "_no");
                    }
                    stat = nomor_antrian + "-" + jedis.get(loket + "_no");
                    ;
                    break;
                case "PANGGIL": //Panggil
                   
                    //System.out.println(loket);
                    //System.out.println(noloket);
                   
                    try {
                       jedis.set(loket + "_status", "1");
                       jedis.set("panngilno", "1");
                       nomor_antrian = jedis.get(loket + "_no");
                       jedis.publish("realtime", loket + "-call-" + nomor_antrian);
                        if (Integer.valueOf(nomor_antrian) > 0) {
                            stat = nomor_antrian;        
                            soundsP.add("sounds/bel.wav");
                            soundsP.add("sounds/nomor_antrian.wav");
                            switch (pos) {
                                case "1":
                                    soundsP.add("sounds/A.WAV");
                                    break;
                                case "2":
                                    soundsP.add("sounds/B.WAV");
                                    break;
                                case "3":
                                    soundsP.add("sounds/C.WAV");
                                    break;
                                default:
                                    break;
                            }
                      
                       //System.out.println(bilangx(Double.parseDouble(nomor_antrian)));
                       bilangx(Double.parseDouble(nomor_antrian));
                       soundsP.add("sounds/ke_loket.wav");
                       soundsP.add("sounds/"+noloket+".wav");
                       play2(soundsP);
                       soundsP.clear();
                            //System.out.println("sounds/" + loket + "/" + nomor_antrian + ".wav");
                            //System.out.println("panggil : " + loket + "/" + nomor_antrian);
                            //stat = nomor_antrian;
                        }
                    } catch (java.lang.NumberFormatException ex) {
                        jedis.set("panngilno", "0");
                        stat = "0";
                    }
                    break;
                case "LEWAT": //Lewat no Beerikut
                    String dnomor = jedis.get(loket + "_no");
                    jedis.set("panngilno", "1");
                    if (cekNoAntrian(pos)) {

                        jedis.set(loket + "_status", "1");

                        if (pos.equals("1")) // layanan 
                        {
                            try {
                                nomor_antrian = jedis.lpop("q_npwp");
                                //jedis.decr("countnpwp");
                                jedis.incr("sisanpwp");
                            } catch (java.lang.NullPointerException ex) {
                                nomor_antrian = dnomor;
                                // System.out.println(nomor_antrian);
                            }

                        } else if (pos.equals("2")) {
                            try {
                                nomor_antrian = jedis.lpop("q_lain");
                                //jedis.decr("countlain");
                                jedis.incr("sisalain");
                            } catch (java.lang.NullPointerException ex) {
                                nomor_antrian = dnomor;
                            }
                        } else {
                            try {
                                nomor_antrian = jedis.lpop("q_sppt");
                                jedis.incr("sisasppt");
                                //jedis.decr("countsppt");
                            } catch (java.lang.NullPointerException ex) {
                                nomor_antrian = dnomor;
                            }
                        }
                        try {
                            jedis.set(loket + "_no", nomor_antrian);
                        } catch (redis.clients.jedis.exceptions.JedisDataException ex2) {
                            nomor_antrian = dnomor;
                            jedis.set(loket + "_no", nomor_antrian);
                        }
                        jedis.set(loket + "_sts_no", nomor_antrian);
                        stat = nomor_antrian;

                        jedis.publish("realtime", loket + "-call-" + nomor_antrian);
                        try {
                            if (Integer.valueOf(nomor_antrian) > 0) {
                                try (DatagramSocket ds = new DatagramSocket()) {
                                String str = "A-000";   
                                switch (pos) {
                                case "1":
                                     str =  "A-" + leftPad(nomor_antrian, 3, ' ');
                                    break;
                                case "2":
                                  str = "B-" + leftPad(nomor_antrian, 3, ' ');
                                    break;
                                case "3":
                                    str =  "C-" + leftPad(nomor_antrian, 3, ' ');
                                    break;
                                default:
                                    break;
                            }
                                    
                                    InetAddress ia = InetAddress.getByName(ipadd);
                                    DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ia, 8888);
                                    ds.send(dp);
                                } catch (SocketException ex) {
                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
                                    jedis.set("panngilno", "0");
                                } catch (UnknownHostException ex) {
                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
                                    jedis.set("panngilno", "0");
                                } catch (IOException ex) {
                                    Logger.getLogger(Kiosk_ServerV3.class.getName()).log(Level.SEVERE, null, ex);
                                    jedis.set("panngilno", "0");
                                } finally { //@TODO : baru tadi malam agug
                                    panggil("sounds/" + loket + "/" + nomor_antrian + ".wav");
                                }
                                //stat = nomor_antrian;
                            }
                        } catch (java.lang.NumberFormatException ex) {
                            jedis.set("panngilno", "0");
                        }
                    } else {
                        jedis.set("panngilno", "0");
                        stat = "kosong";
                    }
                    //stat = nomor_antrian;

                    break;
                case "LAYAN": //LAYAN
                    jedis.set(loket + "_status", "2");
                    nomor_antrian = jedis.get(loket + "_no");
                    jedis.set(loket + "_in", nomor_antrian);
                    jedis.publish("realtime", loket + "-service-" + nomor_antrian);
                    stat = jedis.get(loket + "_in");
                    break;
                case "ISTIRAHAT": //ISTIRAHAT
                    jedis.set(loket + "_status", "3");
                    nomor_antrian = jedis.get(loket + "_no");
                    jedis.publish("realtime", loket + "-rest-" + nomor_antrian);
                    break;
                case "TERSEDIA": //TERSEDIA
                    jedis.set(loket + "_status", "1");
                    nomor_antrian = jedis.get(loket + "_no");
                    jedis.publish("realtime", loket + "-on-" + nomor_antrian);
                    stat = nomor_antrian;
                    break;
                case "CEKANTRIAN": //CEKANTRIAN
                    String vLabel2 = "";
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
                        stat = "ada";
                    } else {
                        stat = "tidakada";
                    }
                    break;
            }
            jedis.set("panngilno", "0");
            //System.out.println("unblok - suara");
        }

        // System.out.println("sounds/" + loket + "/" + nomor_antrian + ".wav - status:" + stat);
        return stat;
    }

    private static boolean cekNoAntrian(String pos) {
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

    public static String leftPad(String originalString, int length,
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

    public static void panggil(String files) {
        byte[] buffer = new byte[28672];
        File file = new File(files);
        try {
            AudioInputStream is = AudioSystem.getAudioInputStream(file);
            AudioFormat format = is.getFormat();
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                FloatControl volume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(5.0f);
                line.start();
                while (is.available() > 0) {
                    int len = is.read(buffer);
                    line.write(buffer, 0, len);
                }
                line.drain();
            }

        } catch (Exception ex) {
            jedis.set("panngilno", "0");
        }
    }

    
    public static String bilangx(double angka)
    {

        if(angka<12)
        {
          soundsP.add("sounds/"+soundN[(int)angka]+".wav");
          return nomina[(int)angka];
        }
        
        if(angka>=12 && angka <=19)
        {
             soundsP.add("sounds/"+soundN[(int)angka%10]+".wav");
             soundsP.add("sounds/BELAS.WAV");
            return nomina[(int)angka%10] +" belas ";
        }
        
        if(angka>=20 && angka <=99)
        {
               soundsP.add("sounds/"+soundN[(int)angka/10]+".wav");
                soundsP.add("sounds/PULUH.WAV");
             soundsP.add("sounds/"+soundN[(int)angka%10]+".wav");
            return nomina[(int)angka/10] +" puluh "+nomina[(int)angka%10];
        }
        
        if(angka>=100 && angka <=199)
        {
            soundsP.add("sounds/100.wav");
           // soundsP.add("sounds/"+soundN[(int)angka%100]+".wav");
            return "seratus "+ bilangx(angka%100);
        }
        
        if(angka>=200 && angka <=999)
        {
            soundsP.add("sounds/"+soundN[(int)angka/100]+".wav");
            soundsP.add("sounds/RATUS.WAV");
           // soundsP.add("sounds/"+soundN[(int)angka%100]+".wav");
            return nomina[(int)angka/100]+" ratus "+bilangx(angka%100);
        }
        
//        if(angka>=1000 && angka <=1999)
//        {
//            return "seribu "+ bilangx(angka%1000);
//        }
//        
//        if(angka >= 2000 && angka <=999999)
//        {
//            return bilangx((int)angka/1000)+" ribu "+ bilangx(angka%1000);
//        }
//        
//        if(angka >= 1000000 && angka <=999999999)
//        {
//            return bilangx((int)angka/1000000)+" juta "+ bilangx(angka%1000000);
//        }
        
        return "";
    }
    
    
    public static void play2(ArrayList<String> files){
    byte[] buffer = new byte[4096];
    for (String filePath : files) {
        File file = new File(filePath);
        try {
            AudioInputStream is = AudioSystem.getAudioInputStream(file);
            AudioFormat format = is.getFormat();
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                while (is.available() > 0) {
                    int len = is.read(buffer);
                    line.write(buffer, 0, len);
                }
                line.drain();
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
        }
    }
 }
      
    @Override
    public void run() {
        try {
            try {

                //System.out.println( m_num + " Connected." );
                BufferedReader inFromClient
                        = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(m_socket.getOutputStream());
                // OutputStreamWriter out = new OutputStreamWriter( m_socket.getOutputStream() );
                //out.write( "Welcome connection #" + m_num + "\n\r" );
                //out.flush();
                //while ( true )
                // {
                String clientSentence = inFromClient.readLine();
                String[] split = clientSentence.split("-");
                //System.out.println(split[0]+"-"+split[1]+"-"+split[2]+"-"+split[3]+"-"+split[4]);
                String capitalizedSentence = execAntrian(split[0], split[1], split[2], split[3], split[4]) + " \n";
                //System.out.println("aktif : "+capitalizedSentence);
                outToClient.writeBytes(capitalizedSentence);
                outToClient.flush();

                // }
                //  clientSentence = inFromClient.readLine();
                // String[] split = clientSentence.split("-");
                //System.out.println(jedis.lrange("qsppt", 0, 20));
                //capitalizedSentence = execAntrian(split[0], split[1], split[2]) + " \n";
                //outToClient.writeBytes(capitalizedSentence);
//                 while ( true )
//                {
//                        
//                    if (inFromClient == null )
//                    {
//                        System.out.println( m_num + " Closed." );
//                        return;
//                    }
//                    else
//                    {
//                        System.out.println( m_num + " Read: " + inFromClient );
//                        if (inFromClient.equals( "exit" ) )
//                        {
//                            System.out.println( m_num + " Closing Connection." );
//                            return;
//                        }
//                        //else if ( line.equals( "crash" ) )
//                        //{
//                        //    System.out.println( m_num + " Simulating a crash of the Server..." );
//                        //    Runtime.getRuntime().halt(0);
//                        //}
//                        else
//                        {
//                            clientSentence = inFromClient.readLine();      
//                            String[] split = clientSentence.split("-");
//                            
//                            System.out.println( m_num + " Write: echo " + clientSentence );
//                            //out.write( "echo " + clientSentence + "\n\r" );
//                           // out.flush();
//                            
//                            capitalizedSentence = execAntrian(split[0], split[1], split[2]) + " \n";
//                            outToClient.writeBytes(capitalizedSentence);
//                            //out.write( "echo " + clientSentence + "\n\r" );
//                            //outToClient.flush();
//                        }
//                    }
//                }
//                
//                BufferedReader in = new BufferedReader( new InputStreamReader( m_socket.getInputStream() ) );
//                OutputStreamWriter out = new OutputStreamWriter( m_socket.getOutputStream() );
//                out.write( "Welcome connection #" + m_num + "\n\r" );
//                out.flush();
//                while ( true )
//                {
//                    String line = in.readLine();
//                    if ( line == null )
//                    {
//                        System.out.println( m_num + " Closed." );
//                        return;
//                    }
//                    else
//                    {
//                        System.out.println( m_num + " Read: " + line );
//                        if ( line.equals( "exit" ) )
//                        {
//                            System.out.println( m_num + " Closing Connection." );
//                            return;
//                        }
//                        //else if ( line.equals( "crash" ) )
//                        //{
//                        //    System.out.println( m_num + " Simulating a crash of the Server..." );
//                        //    Runtime.getRuntime().halt(0);
//                        //}
//                        else
//                        {
//                            System.out.println( m_num + " Write: echo " + line );
//                            out.write( "echo " + line + "\n\r" );
//                            out.flush();
//                        }
//                    }
//                }
            } finally {
                m_socket.close();
                //jedis.set("panngilno", "0");
                // System.out.println( m_num + " tutop p;anggil"  );
            }
        } catch (IOException e) {
            System.out.println(m_num + " Error: " + e.toString());
        }
    }

}

