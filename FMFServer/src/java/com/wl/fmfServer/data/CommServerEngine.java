package com.wl.fmfServer.data;


import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

//  CommServerEngine
//  There are 2 kinds of server Engine.  Secured and Unsecured
//
public abstract class CommServerEngine extends Thread
{
    private boolean secured;
    private int portNumber;

    public CommServerEngine (boolean s, int p) {
        secured=s;
        portNumber=p;
    }

    public void run() {
        int i = 1;

        if (secured) {	//Secured server
            try {
            	//Create a secured server socket bound to the port Number
                SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                SSLServerSocket sslserversocket =
                (SSLServerSocket) sslserversocketfactory.createServerSocket(portNumber);
                System.out.println("secured Waiting for call on port number :"+portNumber);

                for (;;) {	
                	//;; is an infinite loop. However,this is taken care of by the sslserversocket.accept() method,
                    SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
                    //System.out.println("secured accepted for call:"+i);
                    securedCommHandler(sslsocket);
                    System.out.println("called securedCommHandler:"+i);
                    i++;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        else { //Unsecured server

            try { //Create a server socket bound to the port Number

                // Create a ServerSocket to listen to
                ServerSocket s = new ServerSocket(portNumber);
                System.out.println("Unsecured Waiting for call on port number :"+portNumber);

                // Loop forever at this thread to listen to incoming message
                for (;;) { //;; is an infinite loop. However,this is taken care of by the ServerSocket.accept() method,
                    Socket incoming = s.accept( );
                    // System.out.println("Unsecured accepted !!!!! " + i);

                    // handle an incoming message
                    unsecuredCommHandler(incoming);

                    // System.out.println("called unsecuredCommHandler:"+i);
                    i++;
                }
            }
            catch (Exception e){
                System.out.println(e);                
            }
        }
    }


    //  Children has to implement the following methods:

    public abstract void securedCommHandler(SSLSocket s);

    public abstract void unsecuredCommHandler(Socket s);



}