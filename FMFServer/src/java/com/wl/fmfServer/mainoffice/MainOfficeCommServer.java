package com.wl.fmfServer.mainoffice;

import com.wl.fmfServer.data.CommServerEngine;
import java.net.*;
import javax.net.ssl.SSLSocket;

public class MainOfficeCommServer extends CommServerEngine{


    public MainOfficeCommServer (boolean s, int p) {
        super(s,p);
    }

    public void securedCommHandler(SSLSocket sslsocket){
        MainOfficeHandler mHandler = new MainOfficeHandler(sslsocket);
        Thread mThread = new Thread(mHandler);
        mThread.start();
    }

    public void unsecuredCommHandler(Socket s){
        MainOfficeHandler mHandler = new MainOfficeHandler((Socket)s);
        Thread mThread = new Thread(mHandler);
        mThread.start();
    }
}