/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kiosk_server.v3;

import chat.ChatCallback;
import chat.ChatCallbackAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author silaban
 */
public class PlaySound implements Runnable {

    private final String[] nomina = {"", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas"};

    private final String[] soundN = {"s1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
    private final ArrayList<String> soundsP = new ArrayList<String>();
       
    //private Process process;
    private String nomor_antrian="";
    private String pos="";
    private String noloket="";
    
    public PlaySound(String nomor_antrian, String pos, String noloket) {
        this.nomor_antrian = nomor_antrian;
        this.pos = pos;
        this.noloket = noloket;
    }

    @Override
    public void run() {
        execAntrian();
    }

    private  synchronized void execAntrian() {
        try {
          // jedis.set(loket + "_status", "1");
            // jedis.set("panngilno", "1");
            // nomor_antrian = jedis.get(loket + "_no");
            //jedis.publish("realtime", loket + "-call-" + nomor_antrian);
            if (Integer.valueOf(nomor_antrian) > 0) {
                //stat = nomor_antrian;        
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

                bilangx(Double.parseDouble(nomor_antrian));
                soundsP.add("sounds/ke_loket.wav");
                soundsP.add("sounds/" + noloket + ".wav");
                play2(soundsP);
                soundsP.clear();
            }
        } catch (java.lang.NumberFormatException ex) {
            //jedis.set("panngilno", "0");
            //stat = "0";
        }
         notify();
    }

    private synchronized void play2(ArrayList<String> files) {
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
         notify();
    }

    private String bilangx(double angka) {

        if (angka < 12) {
            soundsP.add("sounds/" + soundN[(int) angka] + ".wav");
            return nomina[(int) angka];
        }

        if (angka >= 12 && angka <= 19) {
            soundsP.add("sounds/" + soundN[(int) angka % 10] + ".wav");
            soundsP.add("sounds/BELAS.WAV");
            return nomina[(int) angka % 10] + " belas ";
        }

        if (angka >= 20 && angka <= 99) {
            soundsP.add("sounds/" + soundN[(int) angka / 10] + ".wav");
            soundsP.add("sounds/PULUH.WAV");
            soundsP.add("sounds/" + soundN[(int) angka % 10] + ".wav");
            return nomina[(int) angka / 10] + " puluh " + nomina[(int) angka % 10];
        }

        if (angka >= 100 && angka <= 199) {
            soundsP.add("sounds/100.wav");
            // soundsP.add("sounds/"+soundN[(int)angka%100]+".wav");
            return "seratus " + bilangx(angka % 100);
        }

        if (angka >= 200 && angka <= 999) {
            soundsP.add("sounds/" + soundN[(int) angka / 100] + ".wav");
            soundsP.add("sounds/RATUS.WAV");
            // soundsP.add("sounds/"+soundN[(int)angka%100]+".wav");
            return nomina[(int) angka / 100] + " ratus " + bilangx(angka % 100);
        }

        return "";
    }
}
