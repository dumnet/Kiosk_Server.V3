/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kiosk_server.v3;

/**
 *
 * @author silaban
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Thread thread = new Thread(new PlaySound("100", "1", "1"));
        thread.start();

        synchronized (thread) {
            try {
                //System.out.println("Waiting for b to complete...");
                while (thread.isAlive()) { //condition guarantees no thread lock occurs
                    thread.wait();
                }
            } catch (InterruptedException e) {
            }

            System.out.println("Oke");
        }

    }

}
