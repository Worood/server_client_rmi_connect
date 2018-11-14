/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Worood
 */
public class Client {
    public static void main(String[] args) {
        try {
            Scanner scan = new Scanner(System.in);
            int port = 12345;
            Socket soc = new Socket("localhost", port);
            System.out.println("Client Connected to server on port: "+soc.getLocalPort());
            ObjectOutputStream oos =new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());
           
            System.out.println("Enter your ID, please");
            new Thread(){
                @Override
                public void run() {
                while(true){
                    try {
                        
                        String x = scan.nextLine();
                        if(x.equalsIgnoreCase("Exit")){
                        }
                        oos.writeUTF(x); oos.flush(); 
                       
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                }
            }.start();
           
            while (true) {                
                
                String y = ois.readUTF();
                System.out.println(y);
               
            
            }
            
        } catch (IOException ex) {
           // Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Shutting down...");
            System.exit(0);
        }
    }
}
