/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.elahe.httpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author elahe jalalpoor
 */
public class Client extends Thread {

    Socket client;
    JTextArea txtLog;
    private String hostName;
    private ArrayList<String> blocked;
    private long index ;

    Client(long index, Socket client, JTextArea txtLog, ArrayList<String> rules) {
        this.client = client;
        this.txtLog = txtLog;
        this.blocked = rules;
        this.index = index;
    }

    @Override
    public void run() {
        try {
            

            InputStream inputClient = client.getInputStream();
            OutputStream outputClient = client.getOutputStream();

            String buf = "";
            String[] strArr;
            hostName = null;

            
            int c = 0;
            int cc = 0;
            while (c >= 0) {
                try {
                    c = inputClient.read();
                    if (c >= 0) {
                        buf = buf + (char) c;
                    }

                    if (c == '\r' || c == '\n') {
                        cc++;
                    } else {
                        cc = 0;
                    }
                    if (cc == 4) {
                        break;
                    }
                } catch (IOException ex) {
                    break;
                }
            }
            
            txtLog.append(buf);
            

            String arr[] = buf.split("\r\n");
            for (String arr1 : arr) {
                strArr = arr1.split(" ");
                if (strArr[0].toLowerCase().contains("host")) {
                    hostName = strArr[1];
                    
                    break;
                }
            }
            
            if ( hostName != null && hostName.length() > 0 )
            {
                
                boolean canDo = true;
                for (String rulesNames : blocked) {
                    if ( hostName.equals(rulesNames)) {
                        canDo = false;
                        
                    }
                }

                //User can get the site from this specific host
                if (canDo) {
                   
                    Socket soc = new Socket(hostName, 4444);
                    OutputStream output = soc.getOutputStream();
                    InputStream input = soc.getInputStream();
                    if (soc.isConnected()) {
                        
                    } else {
                        
                        return;
                    }

                    
                    output.write(buf.getBytes());
                    output.flush();

                    
                    int q ;
                    while ((q = input.read()) != -1) {
                        outputClient.write(q);
                    }
                }
                else
                {
                    String error = "<html><head><title>this site is blocked!</title></head><body><h2>Access Denied!</h2></body></html>";
                    outputClient.write(error.getBytes());
                }
                client.close();
            }
        } catch (Exception e) {
            try {
                client.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
