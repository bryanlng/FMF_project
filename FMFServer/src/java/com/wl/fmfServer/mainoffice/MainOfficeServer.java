package com.wl.fmfServer.mainoffice;

import com.wl.fmfServer.data.Communication;
import com.wl.fmfServer.data.Tools;

import java.io.*;
import java.util.logging.*;
import java.util.Calendar;
import java.util.Properties;
import java.util.Hashtable;
import java.sql.*;

//   MainOfficeServer
//   Main Class to initialize everything.

public class MainOfficeServer {
	
	//code taken from http://www.tutorialspoint.com/jdbc/jdbc-update-records.htm
	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	public static final String DB_URL = "jdbc:mysql://localhost/fmf_server";
	
	//  Database credentials
	public static final String USERNAME = "root";
	public static final String PASSWORD = "leung1601";
	
	//Connection and Statement objects that need to be accessed in MainOfficeHandler
	public static Connection connection = null;
	public static PreparedStatement statement = null;
    public MainOfficeServer()
    {

        FileHandler fh=null;
        Properties prop = new Properties();
//        Tools.startServerCalendar();
        Tools.startServerTimer();
        //  Open config file  (properties file)
        prop = Tools.getProperties(Tools.MAINOFFICE_PROPERTIES_FILE);

        if (prop == null)
        {
        	return;
        }
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
        /***********************************SQL Initialization*****************************************************/
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            //Use PreparedStatement instead of Statement to prevent SQL injections
            System.out.println("Connecting to a selected database...");
            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully...");
            Tools.setIsSQLUP("UP");

        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
         }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
         }
        // Create 2 threads.  1 for secured port, 1 for non-secured
        //  Thread secComm = new MainOfficeCommServer(true,ipPort);
        //  Thread unsecComm = new MainOfficeCommServer(false,ipPort+1);
        Thread unsecComm = new MainOfficeCommServer(false,ipPort);		//ended up only implementing unsecured one, as secured one would require creating an SSL certificate
        // secComm.start();
        unsecComm.start();
   }

  // public static LinkedList<String> TargetHistoryList = new LinkedList<String>();

   // Target List. From the FindMyPhone. Target = cellphones. Strings are cellphone #s in string form
   public static Hashtable <String, MainOfficeHandler> targetHT = new Hashtable<String, MainOfficeHandler>();

   // Client List. From FindMyFamily. 
   public static Hashtable <String, MainOfficeHandler> clientHT = new Hashtable<String, MainOfficeHandler>();

   // Target Info List
   public static Hashtable <String, MainOfficeTargetInfo> targetInfoListHT = new Hashtable<String, MainOfficeTargetInfo>();


   public static BufferedWriter debugBufferedWriter = null;
   
   public Connection getConnection(){
	   return connection;
   }
   
   public Statement getStatement(){
	   return statement;
   }


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