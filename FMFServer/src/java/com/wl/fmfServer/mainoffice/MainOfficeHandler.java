package com.wl.fmfServer.mainoffice;

import com.wl.fmfServer.data.TcpDataCommunication;
import com.wl.fmfServer.data.Tools;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.net.ssl.SSLSocket;

/*
This will be used by both target connection and client connection
[FMPRSP:0 0]\nPH:\nDE:\nBA:80\nMD:ON\nGP:ON\nNW:ON\nTK:ON\nLOC:1 <gps 2015/01/19 15:53:19 33.0742 -96.7237 76 0 0 9>\nLOC:2 <network 2015/01/19 15:55:15 33.0687 -96.7122 1261 0 0>\nWF:ENC
*/

public class MainOfficeHandler  extends TcpDataCommunication implements Runnable
{
	

	// Starts - Copy these to FMF and FMP
    //Client Commands
    public static String FMFOFFICE_CLIENTLISTALL = "ClientListAll";
    public static String FMFOFFICE_CLIENTCOMMAND = "ClientCommand";
    public static String FMFOFFICE_CLIENTGETLATEST = "ClientGetLatest";    
    public static String FMFOFFICE_CLIENTGETALLHISTORY = "ClientGetAllHistory";
    public static String FMFOFFICE_CLIENTGETSERVERSTATUS = "ClientGetServerStatus";    
    public static String FMFOFFICE_CLIENTGETLOG = "ClientGetLog";        
    public static String FMFOFFICE_CLIENTCOMMAND_END = "ClientCmdEnd";
    //Client Responses
    public static String FMFOFFICE_CLIENTRESPONSE_LISTALL = "ClientListAllResponse";
    public static String FMFOFFICE_CLIENTRESPONSE_GETLATEST = "ClientGetLatestResponse";  
    public static String FMFOFFICE_CLIENTRESPONSE_GETALLHIST = "ClientGetAllHistResponse"; 
    public static String FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS = "ClientGetServerStatusResponse";
    public static String FMFOFFICE_CLIENTRESPONSE_GETLOG = "ClientGetLogResponse";            
    public static String FMFOFFICE_CLIENTRESPONSE_END = "ClientResponseEnd";    
    public static String FMFOFFICE_CLIENTRESPONSE_OK = "ClientRspOk";
    public static String FMFOFFICE_CLIENTRESPONSE_ERR = "ClientRspErr";
    public static String FMFOFFICE_CLIENTRESPONSE_NOTARGET = "ClientRspNoTarget";
    public static String FMFOFFICE_CLIENTRESPONSE_SEPERATOR = "ClientRspSeperator";    

    //[TargetLogin: :9876543210]

    //[TargetKAlive: :9876543210] or [TargetTrack: :9876543210]
    //[FMPRSP:0 0]\nPH:\nDE:\nBA:80\nMD:ON\nGP:ON\nNW:ON\nTK:ON\nLOC:1 <gps 2015/01/19 15:53:19 33.0742 -96.7237 76 0 0 9>\nLOC:2 <network 2015/01/19 15:55:15 33.0687 -96.7122 1261 0 0>\nWF:ENC
    //[TargetResponseEnd]

    //[TargetResponseBegin:1234567890:9876543210]
    //[FMPRSP:0 0]\nPH:\nDE:\nBA:80\nMD:ON\nGP:ON\nNW:ON\nTK:ON\nLOC:1 <gps 2015/01/19 15:53:19 33.0742 -96.7237 76 0 0 9>\nLOC:2 <network 2015/01/19 15:55:15 33.0687 -96.7122 1261 0 0>\nWF:ENC
    //[TargetResponseEnd]

    public static String FMFOFFICE_TARGETLOGIN = "TargetLogin";
    public static String FMFOFFICE_TARGETKEEPALIVE = "TargetKAlive";
    public static String FMFOFFICE_TARGETLOG = "TargetLog";    
    public static String FMFOFFICE_TARGETRESPONSE_BEGIN = "TargetResponseBegin";
    public static String FMFOFFICE_TARGETRESPONSE_END = "TargetResponseEnd";
    public static String FMFOFFICE_TARGETRESPONSE_LOGIN     = "TargetRspLogin";
    public static String FMFOFFICE_TARGETRESPONSE_KEEPALIVE = "TargetRspKAlive";        
    public static String FMFOFFICE_TARGETTRACK = "TargetTrack";
    
	// Ends - Copy these to FMF and FMP

    private final int TARGET_CONNECTION=0;
    private final int CLIENT_CONNECTION=1;
    private final int DEBUG_CONNECTION=2;

    private BufferedWriter remoteBufferedWriter=null;

    private short connectionType=-1;

    private String userID;
    
    private String targetLastKeepAlive=null;
    private String targetLoginTime=null;    

    public MainOfficeHandler() {}

    public MainOfficeHandler(SSLSocket soc){
        super((Socket) soc);
    }

    public MainOfficeHandler(Socket soc){
        super( soc);
    }
    public void run() {
    	try {
    
        	//debugWrite("MainOfficeHandler");
            Tools.logger.fine("MainOfficeHandler");

            boolean done = false;
            while (!done) {
                String readString = getInReader().readLine();
                debugWrite("MainOfficeHandler: readString is :"+readString);
                if (readString == null || readString.length() == 0)
                {
                	debugWrite("readString == null or length is 0. done");
                    done = true;
                    continue;
                }

                
                readString=readString.substring(1, readString.length()-1);
                String delims = "[:]";
                String[] tokens = readString.split(delims);
                String command=null;
                String clientPhone = "X";
                String targetPhone = "Y";                  
                if (tokens.length == 3)
                {
                    command = tokens[0];
                    clientPhone = tokens[1];
                    targetPhone = tokens[2]; 
                }    
                else if (tokens.length == 2)
                {
                    command = tokens[0];
                    clientPhone = tokens[1]; 
                }     
                else if (tokens.length == 1)
                {
                    command = tokens[0]; 
                }                   
                else 
                {
                    debugWrite("Wrong format");                	
                    break;
                }
                targetPhone = Tools.trimPhoneNumber(targetPhone);
                
                debugWrite("MainOfficeHandler::command:"+command+", clientPhone:"+clientPhone+", targetPhone:"+targetPhone);
                
                if (command.equals(FMFOFFICE_CLIENTLISTALL)){

                	getOutBufferedWriter().write(FMFOFFICE_CLIENTRESPONSE_LISTALL+"\n");

                	Enumeration<String> enumKey = MainOfficeServer.targetHT.keys();
                	while(enumKey.hasMoreElements()) {
                		String key = enumKey.nextElement();

                		if(key==null || key.length() == 0)
                		{
                			continue;
                		}
                		else
                		{
                    		MainOfficeHandler val = MainOfficeServer.targetHT.get(key);
                    		if (val == null)
                    		{
                    			continue;
                    		}
                    			
                			// Send back to Client
                			//FMFOFFICE_CLIENTRESPONSE_LISTALL
                			getOutBufferedWriter().write("["+key+":"+getTargetLoginTime() + ":"+getTargetLastKeepAlive()+"]\n");
                		}
                	}
                	getOutBufferedWriter().write(FMFOFFICE_CLIENTRESPONSE_END+"\n");  
                	getOutBufferedWriter().flush();
                }
                else if (command.equals(FMFOFFICE_CLIENTCOMMAND)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
//                	MainOfficeHandler targetHandler = MainOfficeServer.targetHT.get(targetPhone);
                	MainOfficeHandler targetHandler=null;
                	Enumeration<String> enumKey = MainOfficeServer.targetHT.keys();
                	if(enumKey.hasMoreElements()) {
                		String key = enumKey.nextElement();
                		targetHandler = MainOfficeServer.targetHT.get(key);                		
                	}
                	
                	if (targetHandler != null) {
                    	debugWrite("FMFOFFICE_CLIENTCOMMAND. Target exist:");
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_OK+":"+clientPhone+":"+targetPhone+"]");
                        getOutBufferedWriter().newLine();
                        getOutBufferedWriter().flush();     
                        
                        // Send to target
                        remoteBufferedWriter=targetHandler.getOutBufferedWriter();
                        remoteBufferedWriter.write(readString+"\n");
                        
                        String retString = extraStringFromBF(
                        		getInReader(),addCommandBracket(FMFOFFICE_CLIENTCOMMAND_END),remoteBufferedWriter );

                        remoteBufferedWriter.flush();   
                	}
                	else
                	{
                    	debugWrite("FMFOFFICE_CLIENTCOMMAND. Target NOT exist:");
                    	
                    	// readalllines
                        String retString = extraStringFromBF(
                        		getInReader(),addCommandBracket(FMFOFFICE_CLIENTCOMMAND_END),null );
                        
                        // REsponse with error
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");
                        getOutBufferedWriter().newLine();
                        getOutBufferedWriter().flush();                  		                	    		
                	}           	                	                
                }
                else if (command.equals(FMFOFFICE_CLIENTGETLATEST)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLATEST Found MainOfficeTargetInfo:");
                    	String retString = targetInfo.getLatestLocation(); 
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_GETLATEST+":"+clientPhone+":"+targetPhone+"]\n");
                    	getOutBufferedWriter().write(retString+"\n");
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_END+"]");                    	                    	               	
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                                               	                
                }                
                else if (command.equals(FMFOFFICE_CLIENTGETALLHISTORY)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLATEST Found lindedlist:"+targetInfo.getNumOfLocations());
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_GETLATEST+":"+clientPhone+":"+targetPhone+"]\n");
                    	String locations = targetInfo.getAllLocations("["+FMFOFFICE_CLIENTRESPONSE_SEPERATOR+"]");
            	        getOutBufferedWriter().write(locations);
            	        getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_END+"]");
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                                               	                
                }
                else if (command.equals(FMFOFFICE_CLIENTGETSERVERSTATUS)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// return Server status
                	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS+":"+clientPhone+":"+targetPhone+"]\n");

                	Enumeration<String> enumKey = MainOfficeServer.targetInfoListHT.keys();
                	while(enumKey.hasMoreElements()) {
                		String key = enumKey.nextElement();

                		if(key==null || key.length() == 0)
                		{
                			continue;
                		}
                		else
                		{
                			MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(key);
                    		if (targetInfo == null)
                    		{
                    			continue;
                    		}
                			getOutBufferedWriter().write(targetInfo.getTargetInfo(key));
                		}
                	}            
                	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_END+"]");
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                              	                
                }                
                else if (command.equals(FMFOFFICE_CLIENTGETLOG)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLOG Found targetInfo:"+targetInfo.getDebugLogtime());
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_GETLOG+":"+clientPhone+":"+targetPhone+"]\n");
            	        getOutBufferedWriter().write(targetInfo.getDebugLogtime()+"\n"+targetInfo.getDebugLog()+"\n");
            	        getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_END+"]");
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                             	                
                }                
                                                     
                                
                
                
                
                //  From Target below
                
                else if (command.equals(FMFOFFICE_TARGETLOGIN)){
                	debugWrite("FMFOFFICE_TARGETLOGINd:");
                	userID = targetPhone;
                	connectionType=TARGET_CONNECTION;
                	targetLoginTime = getCurrentTime();
                	
                	
                	MainOfficeHandler targetHandler = MainOfficeServer.targetHT.get(targetPhone);                	
                    if (targetHandler != null) {
                        //
                        debugWrite("Userid " + targetPhone + " already exist.  Removing it");
                        //return false;  remove.
                        targetHandler.disconnectSocket();             
                    }
                    MainOfficeServer.targetHT.put (targetPhone, this);    
                    debugWrite("FMFOFFICE_TARGETLOGIN finished:");                            
                    
                	getOutBufferedWriter().write("["+FMFOFFICE_TARGETRESPONSE_LOGIN+":"+clientPhone+":"+targetPhone+"]\n");
        	        getOutBufferedWriter().write("["+FMFOFFICE_TARGETRESPONSE_END+"]\n");
                    getOutBufferedWriter().flush();                                             	                
        	        
                    
                    
                }
                else if (command.equals(FMFOFFICE_TARGETKEEPALIVE))
                {
                	debugWrite("FMFOFFICE_TARGETKEEPALIVE");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMFOFFICE_TARGETRESPONSE_END),null );
                    System.out.println("Extracted String:->"+retString+"<-");
                    MainOfficeServer.targetHT.put (targetPhone, this);  
                    
                    // Now put this info into LinkedList
                    MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo == null)
                    {
                    	System.out.println("--- Create new MainOfficeTargetInfo ---");
                    	targetInfo = new MainOfficeTargetInfo();
                    	MainOfficeServer.targetInfoListHT.put(targetPhone,targetInfo);
                    }
                  
                    // Check 
                    String latestLocString = targetInfo.getLatestLocation();

                    if (latestLocString == null)
                    {
                    	targetInfo.addLocation(retString);
                    }
                    else
                    {
                    	
                    	// Get timestamp, bestLoc of latestLoc.
                    	// Get timestamp, bestLoc of currentLoc.
                    	// compare
                    	// if currentTime - latestLoc > 1hour record it
                    	// else if currentBestLoc - latestBestLoc > 200 feet, record it
                    	
                    	// do this later
                    	targetInfo.addLocation(retString);                    	
                    }

                    

                    System.out.println("--- Linked List size: "+targetInfo.getNumOfLocations());
                    
                	getOutBufferedWriter().write("["+FMFOFFICE_TARGETRESPONSE_KEEPALIVE+":"+clientPhone+":"+targetPhone+"]\n");
                	// put some more commands if necessary
                	// e.g change keepalive interval
                	//     request to send log
                	//     
        	        getOutBufferedWriter().write("["+FMFOFFICE_TARGETRESPONSE_END+"]\n");
                    getOutBufferedWriter().flush();                                             	                                                 
                   
                    // Write to DB
                    
                }
                else if (command.equals(FMFOFFICE_TARGETLOG))
                {
                	debugWrite("FMFOFFICE_TARGETLOG");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMFOFFICE_TARGETRESPONSE_END),null );
                    System.out.println("Extracted String:->"+retString+"<-");
                    MainOfficeServer.targetHT.put (targetPhone, this);  
                    
                    // Now put this info into targetInfo
                    MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo == null)
                    {
                    	System.out.println("--- Create new MainOfficeTargetInfo  ---");
                    	targetInfo = new MainOfficeTargetInfo();
                    	MainOfficeServer.targetInfoListHT.put(targetPhone,targetInfo);
                    }
                    
                    targetInfo.udpateDebugLog(retString);

                    System.out.println("FMFOFFICE_TARGETLOG logupdated, size: "+targetInfo.getDebugLog().length());                    
                    
                }                
                else if (command.equals(FMFOFFICE_TARGETTRACK))
                {
                	debugWrite("FMFOFFICE_TARGETTRACK");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMFOFFICE_TARGETRESPONSE_END),null );
                    System.out.println("Extracted String:->"+retString+"<-");
                    
                    // Write to DB for Tracking
                    
                }   
                else if (command.equals(FMFOFFICE_TARGETRESPONSE_BEGIN))
                {
                	debugWrite("FMFOFFICE_TARGETRESPONSE_BEGIN");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMFOFFICE_TARGETRESPONSE_END),remoteBufferedWriter );
                    remoteBufferedWriter.flush();
                    System.out.println("Extracted String:->"+retString+"<-");
                    
                    // Write to DB for Tracking
                    
                }                                 
                else
                {
                	debugWrite("unrecgnozied command:"+command);
                	if (connectionType == CLIENT_CONNECTION)
                	{             		
                		getOutBufferedWriter().write("["+FMFOFFICE_CLIENTRESPONSE_ERR+":"+clientPhone+":"+targetPhone+"]");                	
                        getOutBufferedWriter().newLine();
                        getOutBufferedWriter().flush();
                	}
                }
                
                // Existing connections
                
                displayExistingConnections();
                
            }// While
    	}
        catch (Exception e){
            debugWrite("Trapped Office Handler Exception:" + e.getMessage());
           // setRemoteBufferedWriter(null);
        }
        System.out.println( "Type:"+connectionType+" end of run ");
        if (connectionType == DEBUG_CONNECTION) {
            MainOfficeServer.debugBufferedWriter = null;
        }
        disconnectSocket();
        if (connectionType == TARGET_CONNECTION)
        {
        	// We should not remove target because of timeout
//        	if (userID != null)
//       	{
//        		System.out.println( "Removing target user "+userID);
//        		MainOfficeServer.targetHT.remove(userID);
//        	}
        }   	
        else if (connectionType == CLIENT_CONNECTION)
        {
        	if (userID != null)
        	{
        		System.out.println( "Removing client user "+userID);        		
        		MainOfficeServer.clientHT.remove(userID);
        	}
        }         
        displayExistingConnections();

    }
    
    

    public void debugWrite(String s)
    {
        //char aa = 0x0a;
        //char dd = 0x0d;

        System.out.println(getCurrentTime()+":"+s);
        /*
        try {
            if (MainOfficeServer.debugBufferedWriter != null) {
                MainOfficeServer.debugBufferedWriter.write(s+aa+dd);
                MainOfficeServer.debugBufferedWriter.flush();
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected error at debugWrite :"+ e);
        }
        */


    }

    public void setRemoteBufferedWriter(BufferedWriter bw)
    {
        remoteBufferedWriter=bw;
    }

    public BufferedWriter getRemoteBufferedWriter()
    {
        return remoteBufferedWriter;
    }
    
    public String getTargetLastKeepAlive() { return targetLastKeepAlive;}
    public String getTargetLoginTime() { return targetLoginTime; }   

    public String getCurrentTime()
    {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date); //2014/08/06 15:59:48  
    }
    
    public String extraStringFromBF(
    		BufferedReader bf, String endString, BufferedWriter bw)
    {
    	String returnString="";
		boolean allRead = false;
		
		while (!allRead) 
		{
			try 
			{			
				String readString = bf.readLine();
				//System.out.println("extraStringFromBF:Read:"+readString);
			
				if (readString == null) break;
				//System.out.println("extraStringFromBF:Read 2:"+readString);
				
				//System.out.println("extraStringFromBF:Read 3:"+readString);				
				if (endString != null && endString.length() != 0)
				{
					//System.out.println("extraStringFromBF:Read 4:"+readString);							
					if (readString.equals(endString)) 
					{
						System.out.println("extraStringFromBF:Read Found End String:"+readString+"\n");
						allRead = true;
					}
					else
					{
						returnString += readString+"\n";
					}
				}
				else  // Only Read one line ?
				{				
					returnString += readString+"\n";
					//System.out.println("extraStringFromBF:Read 5:"+readString);							
					allRead = true;				
				}
				//System.out.println("extraStringFromBF:Read 6:"+readString);	
				if (bw != null)
				{			
					bw.write(readString + "\n");								

				}
			}
			catch (Exception e)
			{
				System.out.println("extraStringFromBF:Read Exception\n"+e);
			}			
		}    	
		return returnString;
    }
    
    public String addCommandBracket(String inString)
    {
    	if (inString != null)
    		return ("["+inString+"]");
    	
    	return "[]";
    }
    
    public String removeCommandBracket(String inString)
    {
    	String retString=inString;
    	
    	if (inString != null)
    	{
    		retString = inString.trim();
    		if (retString.length() > 2)
    			retString = retString.substring(1, retString.length()-1);
    		
    	}
    	
    	return retString;
    }
    
    public void displayExistingConnections()
    {
    	String dispString = "Client: Size:"+MainOfficeServer.clientHT.size()+", ->";
    	
    	Enumeration<String> enumKey = MainOfficeServer.clientHT.keys();
    	while(enumKey.hasMoreElements()) {
    		String key = enumKey.nextElement();
    		dispString = dispString + ", "+key;
    	}
    		
    	dispString += "\nTarget: Size:"+MainOfficeServer.targetHT.size()+"\n*****************\n";
    	
    	enumKey = MainOfficeServer.targetHT.keys();
    	while(enumKey.hasMoreElements()) {
    		String key = enumKey.nextElement();
    		dispString = dispString + ", "+key;
        	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(key);
            if (targetInfo != null)
            {
            	dispString = dispString + "("+targetInfo.getNumOfLocations()+")";
            }		
    	}
    	
    	System.out.println(dispString);
    }
    
}
