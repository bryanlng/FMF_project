package com.wl.fmfServer.mainoffice;

import com.wl.fmfServer.data.Communication;
import com.wl.fmfServer.data.Tools;

import java.io.*;
import java.util.logging.*;
import java.util.Properties;
import java.util.Hashtable;


//   MainOfficeServer
//   Main Class to initialize everything.

public class MainOfficeServer {

    public MainOfficeServer()
    {

        FileHandler fh=null;
        Properties prop = new Properties();

        //  Open config file  (properties file)
        prop = Tools.getProperties(Tools.MAINOFFICE_PROPERTIES_FILE);

        //  create log object
        Tools.logger =  Logger.getLogger("com.wl.fmfServer.mainoffice");

        try {
            fh = new FileHandler(prop.getProperty(Tools.MAINOFFICE_LOGFILE));
        } catch (Exception ex) {
            System.out.print("cannot open log file " + ex);
        }

        Tools.logger.addHandler(fh);
        // Request that every detail gets logged.
        Tools.logger.setLevel(Level.ALL);
        // Log a simple INFO message.
        Tools.logger.info("doing stuff");

        Tools.logger.fine("done");

        // Open port for listening
        int ipPort = Communication.DEFAULT_MAINOFFICE_PORT;
        try {
            ipPort = Integer.parseInt(prop.getProperty(Tools.MAINOFFICE_IPPORT1));
        }
        catch (Exception e){
            System.out.println("Cannot find ipport from properties file");
        }

        // Create 2 threads.  1 for secured port, 1 for non-secured

      //  Thread secComm = new MainOfficeCommServer(true,ipPort);
      //  Thread unsecComm = new MainOfficeCommServer(false,ipPort+1);
        Thread unsecComm = new MainOfficeCommServer(false,ipPort);
       // secComm.start();
        unsecComm.start();
   }

  // public static LinkedList<String> TargetHistoryList = new LinkedList<String>();

   // Target List
   public static Hashtable <String, MainOfficeHandler> targetHT = new Hashtable<String, MainOfficeHandler>();

   // Client List
   public static Hashtable <String, MainOfficeHandler> clientHT = new Hashtable<String, MainOfficeHandler>();

   // Target Info List
   public static Hashtable <String, MainOfficeTargetInfo> targetInfoListHT = new Hashtable<String, MainOfficeTargetInfo>();


   public static BufferedWriter debugBufferedWriter = null;


/*   This is how we can set up a secured communication.  TBD

   public static void main(String[] a){
       System.setProperty("javax.net.ssl.keyStore", "serverkeystore");
       System.setProperty("javax.net.ssl.keyStorePassword", "one234");

       System.out.println("TrustStore:"+System.getProperty("javax.net.ssl.keyStore"));
       System.out.println("TrustPass:"+System.getProperty("javax.net.ssl.keyStorePassword"));
       MainOfficeServer ms = new MainOfficeServer();
   }
   */
}