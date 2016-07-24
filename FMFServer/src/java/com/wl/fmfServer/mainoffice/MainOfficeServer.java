package com.wl.fmfServer.mainoffice;

import com.wl.fmfServer.data.Communication;
import com.wl.fmfServer.data.Tools;


import java.io.BufferedWriter;
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

		String mysqlHost = prop.getProperty(Tools.MAINOFFICE_MYSQLHOST);            
		String mysqlUserID = prop.getProperty(Tools.MAINOFFICE_MYSQLUSERID);
		String mysqlPassword = prop.getProperty(Tools.MAINOFFICE_MYSQLPASSWORD);

		MainOfficeDB.getInstance().init(mysqlHost, mysqlUserID, mysqlPassword);

		// Create 2 threads.  1 for secured port, 1 for non-secured
		//  Thread secComm = new MainOfficeCommServer(true,ipPort);
		//  Thread unsecComm = new MainOfficeCommServer(false,ipPort+1);
		Thread unsecComm = new MainOfficeCommServer(false,ipPort);		//ended up only implementing unsecured one, as secured one would require creating an SSL certificate
		// secComm.start();
		unsecComm.start();

		// Create a thread to clean up database. Perform every day, everytime, clean up as much as 2 days of record.
		Thread cleanupTask = new CleanupDBTask(1,2);
		cleanupTask.start();
		
	}

	// public static LinkedList<String> TargetHistoryList = new LinkedList<String>();

	// Target List. From the FindMyPhone. Target = cellphones. Strings are cellphone #s in string form
	public static Hashtable <String, MainOfficeHandler> targetHT = new Hashtable<String, MainOfficeHandler>();

	// Client List. From FindMyFamily. 
	public static Hashtable <String, MainOfficeHandler> clientHT = new Hashtable<String, MainOfficeHandler>();

	// Target Info List
	public static Hashtable <String, MainOfficeTargetInfo> targetInfoListHT = new Hashtable<String, MainOfficeTargetInfo>();

	public static BufferedWriter debugBufferedWriter = null;
	

	// Background Job to cleanupDB everyday
	private class CleanupDBTask extends Thread
	{
		private int dayDelay=0;   // Delete every dayDelay
		private int daysOfRecordToDelete=0;   // no. of days of records to Delete
		private boolean started = false;
		
	    public CleanupDBTask (int aDelay, int recordToDelete) 
	    {
	    	dayDelay = aDelay;
	    	daysOfRecordToDelete = recordToDelete;
		}

		public void run() {
			System.out.println("cleanupDBTask initiated:" + dayDelay + ", "
					+ daysOfRecordToDelete);
			if (dayDelay <= 0)
				return;

			while (true) {

				try {
					if (started) {
						Thread.sleep(dayDelay * 24 * 60 * 60 * 1000);						
					}
					started = true;
					// Now cleanup DB
					MainOfficeDB.getInstance().cleanupRecords(daysOfRecordToDelete);
					
				} catch (Exception e) {
				}

			}

		}

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