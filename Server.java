/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.runtime.JSType;

/**
 *
 * @author Worood
 */

public class Server {
    static  ArrayList<ObjectOutputStream> users = new ArrayList<ObjectOutputStream>();  
    static ArrayList<String> names = new ArrayList<String>();
    int localId;
    static int total = 0;
    int port = 12345;
    static ServerSocket serSocket = null;
    Socket soc ;
    ObjectOutputStream oos ;
    ObjectInputStream ois;
    BufferedReader br;
    BufferedWriter bw;
    PrintWriter pr;
    boolean b;
    String cname;
    public Server() {
        
        
        try {
            b = false;
            if(serSocket == null){
            serSocket = new ServerSocket(port);
            System.out.println("Server Started on port: "+port +"\n" + "waiting for Clients");
               }
            
            soc = serSocket.accept();
            
            ////////////
            br = new BufferedReader(new FileReader(new File("clients.txt")));
            bw = new BufferedWriter(new FileWriter(new File("clients.txt"),true));
            pr = new PrintWriter(bw);
            
            
            System.out.println("Accepted Client on port: "+soc.getPort());
          
            ois = new ObjectInputStream(soc.getInputStream());
            oos = new ObjectOutputStream(soc.getOutputStream());
            users.add(oos);
            new Thread(){
                @Override
                public void run() {
               
                    
                    try {
                        //   try {
                        String x = ois.readUTF();
                        System.out.println(x);
                       
                        System.out.println("before checking");
                        for(String s: names){
                            if(s.equalsIgnoreCase(x)){
                                try {
                                    oos.writeUTF("You are already signed in");
                                    oos.flush();
                                    soc.close();
                                    this.suspend();
                                } catch (IOException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        String p = checkClient(x);
                        System.out.println(" " + p);
                        
                        if(p.equalsIgnoreCase("notfound")){
                            try {
                                // System.out.println("you need to register and enter a password");
                                oos.writeUTF("Not Found");
                                oos.flush();
                                oos.writeUTF("\nYou are not registered.\n Enter your name and password: ");
                                oos.flush();
                                addClient();
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }
                        else {
                            try {
                                oos.writeUTF("hello, " + getNameI(x) + "\nConfirm it's u by entering ur password");
                                oos.flush();
                                cname =x; 
                                enterPass(p);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        total++;
                        localId = total;
                        names.add(x);
                        
                        
                        while(true){
                            try{
                                oos.writeUTF("To send a unicast message type 'uni' then the message then the ID"
                                        + "of the person you want to send to\n"
                                        + "If you want to leave type 'exit'");
                                oos.flush();
                                oos.writeUTF("To send a broadcast message type 'bro' then the message  you want to send to");
                                oos.flush();
                                String type = ois.readUTF();
                                
                                if(type.equalsIgnoreCase("uni")){
                                    String msg = ois.readUTF();
                                    String other = ois.readUTF();
                                    for(int i = 0; i<names.size(); i++){
                                        
                                        if(other.equals(names.get(i))){
                                            
                                            ObjectOutputStream oth = users.get(i);
                                            if(oth==null) continue;
                                            oth.writeUTF("Msg from (" + names.get(localId-1) +") " +msg);
                                            oth.flush();
                                        }
                                        
                                    }
                                } else if(type.equalsIgnoreCase("bro")){                                    
                                    String  msg = ois.readUTF();
                                    for(ObjectOutputStream oos1 : users){
                                        if(oos1==null) continue;
                                        if(oos == oos1) continue;
                                        oos1.writeUTF("Msg from (" + getNameI(names.get(localId-1)) +") " +msg);
                                        oos1.flush();
                                    }
                                }else if(type.equalsIgnoreCase("exit") )
                                {
                                    oos.writeUTF("Goodbye, " + cname); oos.flush();
                                    for(ObjectOutputStream oos1 : users){
                                        if(oos==null) continue;
                                        if(oos == oos1) continue;
                                        oos1.writeUTF( getNameI(names.get(localId-1)) +" Is leaving..." );
                                        oos1.flush();
                                        
                                    }
                                    users.set(localId-1, null);
                                    soc.close();
                                    break;
                                }
                                
                                else {
                                    oos.writeUTF("Uknown Symbol :/");
                                    oos.flush();
                                }
                                
                            } catch (IOException ex) {
                                
                             //   System.out.println("158 Exception is " + ex.toString());
                                //     Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }   
                }
            }.start();
         
        } catch (IOException ex) {
            System.out.println("179 Exception is " + ex.toString());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public String getNameI(String id){
        String res= "";
        
        for( int i = 0; i<id.length(); i++){
            if(Character.isDigit(id.charAt(i)))
                break;
            res+=id.charAt(i);
        }
        return res;
    }
    public String checkClient(String id){
    boolean b = false;
    String ans = "Notfound";
    String [ ] words;
    String i;
        try {
            
            String line = br.readLine();
          //  System.out.println("first read " + line);
            while(line!=null){
                words = line.split("\t");
                if(words[0].equals(id))
                    ans = words[1];      
                  line = br.readLine();
            }  
        } catch (IOException ex) {
            System.out.println(";__;");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("finished checking");
    return ans;
}
    
    
    public void addClient(){
        try {
            String name = ois.readUTF();
            String pass = ois.readUTF();
            String pass2 = ois.readUTF();
            while(!pass.equalsIgnoreCase(pass2)){
                oos.writeUTF("Password must match");
                oos.flush();
                 pass = ois.readUTF();
                 pass2 = ois.readUTF();
            }
            Random r = new Random();
            int num = r.nextInt(999) + 1;
            oos.writeUTF("Your ID is "); oos.flush();
            oos.writeUTF( name + num); oos.flush();
            pr.println();
            bw.write(name + num + "\t" +pass);
            bw.write('\n');
            bw.flush();
            names.add(name+num);
            b = true;
            cname = name;
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Client registered...");
       
    }
    
    public void enterPass(String p) throws IOException{
        int tries = 0;
       
        String ps ;
       
        while(tries<3){
             oos.writeUTF("Enter password"); oos.flush();
             ps =ois.readUTF();
             if(ps.equals(p)){
            oos.writeUTF("Success");  oos.flush();
            
            b = true;
            return ;
        }
        tries++;
         oos.writeUTF("Wrong password, try again."
                 + " \n You have " + (3 - tries)+" more chances to try");
         oos.flush();
        
        }
        oos.writeUTF("You can no longer try. We think it's not you. BYE");
        oos.writeUTF("You are already signed in");
                                oos.flush();
                                soc.close();
                                  
    }
    
    
    public static void main(String[] args) {
        while(true){
        Server server = new Server();
        }
    }
}
