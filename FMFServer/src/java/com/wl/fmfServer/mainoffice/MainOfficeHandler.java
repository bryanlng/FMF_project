package com.wl.fmfServer.mainoffice;

import com.william.fmfCommon.FMCRawLocation;
import com.william.fmfCommon.FMCLocationData;
import com.william.fmfCommon.FMCMessage;	//FMCMessage was created to house all the string names
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
            		//gets the BufferedReader from the TCP superclass and reads the first line
                String readString = getInReader().readLine();	
                	//Prints out the format: yyyy-MM-dd HH:mm:ss: MainOfficeHandler: readString is :readString
                	//where readstring = [TargetKAlive:0:+14696646540]
                debugWrite("MainOfficeHandler: readString is :"+readString);
                	
                	//if the line is empty, print message saying string is empty, then end by breaking out of while loop
                if (readString == null || readString.length() == 0)
                {
                	debugWrite("readString == null or length is 0. done");
                    done = true;
                    continue;
                }
                	
                
                readString=readString.substring(1, readString.length()-1); //Removes the [ ], ex: TargetKAlive:0:+14696646540
                String delims = "[:]";
                String[] tokens = readString.split(delims);	//extract the TargetKAlive, 0, +14696646540
                String command=null;
                String clientPhone = "X";		//Phone that has FMF on it
                String targetPhone = "Y";       //Phone that has FindMyPhone on it
                if (tokens.length == 3)
                {
                    command = tokens[0];		//command = TargetKAlive
                    clientPhone = tokens[1];	//clientPhone = 0
                    targetPhone = tokens[2]; 	//targetPhone = +14696646540
                }    
                else if (tokens.length == 2)	//Client request from FindMyFamily. Ex: [ClientGetServerStatus:1111111111:]
                {
                    command = tokens[0];		//command = ClientGetServerStatus
                    clientPhone = tokens[1]; 	//clientPhone = 1111111111
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
                targetPhone = Tools.trimPhoneNumber(targetPhone);	//takes out the "+1" from the phone number
                
                debugWrite("MainOfficeHandler::command:"+command+", clientPhone:"+clientPhone+", targetPhone:"+targetPhone);
                
                /************************Based on the command, do something different*******************************/
                
                if (command.equals(FMCMessage.FMFOFFICE_CLIENTLISTALL)){	//If command = "ClientListAll"
                		
                	//getOutBufferedWriter() simply returns the BufferedWriter we got in setBuilders()
                	//write "ClientListAllResponse" to the BufferedWriter
                	getOutBufferedWriter().write(FMCMessage.FMFOFFICE_CLIENTRESPONSE_LISTALL+"\n");	//ClientListAllResponse

                	//gets list of all the keys in targetHT, the target list
                	//Aka, gets all the targets and put them in an Enumeration<String>
                	Enumeration<String> enumKey = MainOfficeServer.targetHT.keys();
                	while(enumKey.hasMoreElements()) {	//while there are still keys 
                		String key = enumKey.nextElement();
                		
                		if(key==null || key.length() == 0)
                		{
                			continue;
                		}
                		else
                		{
                			System.out.println("Current key: " + key);
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
                	getOutBufferedWriter().write(FMCMessage.FMFOFFICE_CLIENTRESPONSE_END+"\n");  //ClientResponseEnd
                	getOutBufferedWriter().flush();		//empty everything from the 
                }
                else if (command.equals(FMCMessage.FMFOFFICE_CLIENTCOMMAND)){	//ClientCmdEnd
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
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_OK+":"+clientPhone+":"+targetPhone+"]");
                        getOutBufferedWriter().newLine();
                        getOutBufferedWriter().flush();     
                        
                        // Send to target
                        remoteBufferedWriter=targetHandler.getOutBufferedWriter();
                        remoteBufferedWriter.write(readString+"\n");
                        
                        String retString = extraStringFromBF(
                        		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_CLIENTCOMMAND_END),remoteBufferedWriter );

                        remoteBufferedWriter.flush();   
                	}
                	else
                	{
                    	debugWrite("FMFOFFICE_CLIENTCOMMAND. Target NOT exist:");
                    	
                    	// readalllines
                        String retString = extraStringFromBF(
                        		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_CLIENTCOMMAND_END),null );
                        
                        // REsponse with error
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");
                        getOutBufferedWriter().newLine();
                        getOutBufferedWriter().flush();                  		                	    		
                	}           	                	                
                }
                else if (command.equals(FMCMessage.FMFOFFICE_CLIENTGETLATEST)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLATEST Found MainOfficeTargetInfo:");
                    	String retString = targetInfo.getLatestLocation(); 
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLATEST+":"+clientPhone+":"+targetPhone+"]\n");
                    	getOutBufferedWriter().write(retString+"\n");
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_END+"]");                    	                    	               	
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                                               	                
                }                
                else if (command.equals(FMCMessage.FMFOFFICE_CLIENTGETALLHISTORY)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLATEST Found lindedlist:"+targetInfo.getNumOfLocations());
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLATEST+":"+clientPhone+":"+targetPhone+"]\n");
                    	String locations = targetInfo.getAllLocations("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_SEPERATOR+"]");
            	        getOutBufferedWriter().write(locations);
            	        getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_END+"]");
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                                               	                
                }
                else if (command.equals(FMCMessage.FMFOFFICE_CLIENTGETSERVERSTATUS)){	///////////////////////////////ClientGetServerStatus
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);	//put client phone into Hashtable of client phones that have FMP

                	// return Server status
                	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS+":"+clientPhone+":"+targetPhone+"]\n");

                	//Gets all the phones of targetInfoListHT (phones)
                	Enumeration<String> enumKey = MainOfficeServer.targetInfoListHT.keys();
                	boolean daysUpPrintedAlready = false;
                	while(enumKey.hasMoreElements()) {
                		String key = enumKey.nextElement();		//get 

                		if(key==null || key.length() == 0)
                		{
                			continue;
                		}
                		else
                		{
                			//get the MainOfficeTargetInfo from the key 
                			MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(key);
                    		if (targetInfo == null)
                    		{
                    			System.out.println("targetInfo is null");
                    			continue;
                    		}
//                    		else{
                    			//Display time in days since the server started running (aka
                    			//Display # of targets
	                    		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                            String serverStartFormatted = dateFormat.format(Tools.getServerStart()); //Ex: 2014/08/06 15:59:48  
                    			int daysUp = Tools.compareDate(serverStartFormatted);
//                    			debugWrite("DaysUp:" + daysUp + " , Size:" + MainOfficeServer.targetHT.size());
                    			if(!daysUpPrintedAlready){
                    				getOutBufferedWriter().write("DaysUp:" + daysUp + " , Size:" + MainOfficeServer.targetHT.size());
                    				getOutBufferedWriter().newLine();
                    				daysUpPrintedAlready = true;
                    			}
                    			
//                            	getOutBufferedWriter().newLine();                        
  
//                    		}
                			debugWrite("targetInfo: " + targetInfo.getTargetInfo(key));
//                			System.out.println(targetInfo.getTargetInfo(key));
                    		getOutBufferedWriter().write(targetInfo.getTargetInfo(key));	
                			//public String getTargetInfo(String myPhone)
                			//Write to BufferedWriter in format: myPhone: Last update time:hasLocation : updateTimes: delogTime
                		}
                	}            
                	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_END+"]");
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                              	                
                }                
                else if (command.equals(FMCMessage.FMFOFFICE_CLIENTGETLOG)){
                	connectionType=CLIENT_CONNECTION;
                	userID = clientPhone;               	
                	
                	MainOfficeServer.clientHT.put(clientPhone,this);	

                	// look for remote target.
                	MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    if (targetInfo != null)
                    {
                    	debugWrite("FMFOFFICE_CLIENTGETLOG Found targetInfo:"+targetInfo.getDebugLogtime());
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLOG+":"+clientPhone+":"+targetPhone+"]\n");
            	        getOutBufferedWriter().write(targetInfo.getDebugLogtime()+"\n"+targetInfo.getDebugLog()+"\n");
            	        getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_END+"]");
                    }
                    else
                    {
                    	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_NOTARGET+":"+clientPhone+":"+targetPhone+"]");                    	
                    }
                	getOutBufferedWriter().newLine();                        
                    getOutBufferedWriter().flush();                                             	                
                }                
                                                     
                                
                
                
                
                //  From Target below
                
                else if (command.equals(FMCMessage.FMFOFFICE_TARGETLOGIN)){
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
                    
                	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_TARGETRESPONSE_LOGIN+":"+clientPhone+":"+targetPhone+"]\n");
        	        getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_TARGETRESPONSE_END+"]\n");
                    getOutBufferedWriter().flush();                                             	                
        	        
                    
                    
                }
                else if (command.equals(FMCMessage.FMFOFFICE_TARGETKEEPALIVE))		//TargetKAlive
                {
                	debugWrite("FMFOFFICE_TARGETKEEPALIVE");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_TARGETRESPONSE_END),null );
                    System.out.println("retString is the extracted String: " + retString);
                    
                    // Convert String to a FMPLocationData Objcted String:->"+retString+"<-");
                    System.out.println("targetPhone: " + targetPhone);
                    FMCLocationData locationData = new FMCLocationData();
                    locationData.composeObjectFromMessage(retString, targetPhone);	//fill the FMPLocationData object with info from retString
                    System.out.println("Converted this record:"+locationData.getPhoneNumber()+" to Object printout:->"+locationData+"<-");
                    
                    
                    MainOfficeServer.targetHT.put (targetPhone, this);  //put the targetPhone into the MainOfficeServer hashtable targetHT
                    
                    // Now put this info into LinkedList
                    //Get the information of the target from targetInfoListHT, which is a Hashtable that 
                    //	takes a Hashtable <String, MainOfficeTargetInfo>
                    MainOfficeTargetInfo targetInfo = MainOfficeServer.targetInfoListHT.get(targetPhone);
                    
                    //If the target doesn't exist in the list, make a new one and add it into the HashTable
                    if (targetInfo == null)
                    {
                    	System.out.println("--- Create new MainOfficeTargetInfo ---");
                    	targetInfo = new MainOfficeTargetInfo();
                    	MainOfficeServer.targetInfoListHT.put(targetPhone,targetInfo);
                    }
                  
                    // Get the target's latest location
                    String latestLocString = targetInfo.getLatestLocation();
                    
                    //If the latest location is null, add retString to it
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

                    

                    System.out.println("--- Linked List size/ # of locations: "+targetInfo.getNumOfLocations());
                    
                	getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_TARGETRESPONSE_KEEPALIVE+":"+clientPhone+":"+targetPhone+"]\n");
                	// put some more commands if necessary
                	// e.g change keepalive interval
                	//     request to send log
                	//     
        	        getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_TARGETRESPONSE_END+"]\n");
                    getOutBufferedWriter().flush();                                             	                                                 
                   
                    // Write to DB
                    
                }
                else if (command.equals(FMCMessage.FMFOFFICE_TARGETLOG))
                {
                	debugWrite("FMFOFFICE_TARGETLOG");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_TARGETRESPONSE_END),null );
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
                /*  not implemented
                else if (command.equals(FMCMessage.FMFOFFICE_TARGETTRACK))
                {
                	debugWrite("FMFOFFICE_TARGETTRACK");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_TARGETRESPONSE_END),null );
                    System.out.println("Extracted String:->"+retString+"<-");
                    
                    // Write to DB for Tracking
                    
                } 
                */  
                else if (command.equals(FMCMessage.FMFOFFICE_TARGETRESPONSE_BEGIN))
                {
                	debugWrite("FMFOFFICE_TARGETRESPONSE_BEGIN");
                	connectionType=TARGET_CONNECTION;
                	targetLastKeepAlive = getCurrentTime();
                	userID = targetPhone;          
                    String retString = extraStringFromBF(
                    		getInReader(),addCommandBracket(FMCMessage.FMFOFFICE_TARGETRESPONSE_END),remoteBufferedWriter );
                    remoteBufferedWriter.flush();
                    System.out.println("Extracted String:->"+retString+"<-");
                    
                    // Write to DB for Tracking
                    
                }                                 
                else
                {
                	debugWrite("unrecgnozied command:"+command);
                	if (connectionType == CLIENT_CONNECTION)
                	{             		
                		getOutBufferedWriter().write("["+FMCMessage.FMFOFFICE_CLIENTRESPONSE_ERR+":"+clientPhone+":"+targetPhone+"]");                	
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
    
    
    /*
     * Prints out current time in format yyyy-MM-dd HH:mm:ss from the DateFormat object
     */
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

    /*
     * Gets the time using a DateFormat object
     */
    public String getCurrentTime()
    {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date); //Ex: 2014/08/06 15:59:48  
    }
    
    /*
     *  Extract the string from the buffered reader
     *  String retString = extraStringFromBF(  getInReader(),addCommandBracket(FMFOFFICE_TARGETRESPONSE_END),null ); 
     *  String retString = extraStringFromBF(  getInReader(),"TargetResponseEnd",null ); 
     *  TargetResponseEnd 
     */
    public String extraStringFromBF(
    		BufferedReader bf, String endString, BufferedWriter bw)
    {
    	String returnString="";
		boolean allRead = false;
		
		while (!allRead) 
		{
			try 
			{			
				String readString = bf.readLine();		//read a line from the BufferedReader
				//System.out.println("extraStringFromBF:Read:"+readString);
			
				if (readString == null) break;			//if empty, break
				//System.out.println("extraStringFromBF:Read 2:"+readString);
				
				//System.out.println("extraStringFromBF:Read 3:"+readString);	
				
				
				if (endString != null && endString.length() != 0)		
				{
					//System.out.println("extraStringFromBF:Read 4:"+readString);	
					
					//For TargetKeepAlive: if readString = "TargetResponseEnd"
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
    /*
     * Adds brackets [] to the command
     */
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
    
    /*
     * Display all clients and targets 
     */
    public void displayExistingConnections()
    {
    	System.out.println("Calling displayExistingConnections()");	
    	String dispString = "Clients: Size:"+MainOfficeServer.clientHT.size()+", ->";
    	
    	//Display all existing Clients
    	Enumeration<String> enumKey = MainOfficeServer.clientHT.keys();
    	while(enumKey.hasMoreElements()) {
    		String key = enumKey.nextElement();
    		dispString = dispString + ", "+key;
    	}
    		
    	dispString += "\nTarget: Size:"+MainOfficeServer.targetHT.size()+"\n*****************\n";
    	
    	//Display all existing Targets
    	
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
