package com.wl.fmfServer.mainoffice;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

public class MainOfficeTargetInfo {

    private LinkedList<String> targetLocationList = new  LinkedList<String>();
    private String debugLog="NONE";
    private String debugLogTime = "NONE";
    private String latestLocationUpdateTime = "";
    private int numbeOfLocationUpdates = 0;
    private boolean latestInfoHasLoc = false;


    public MainOfficeTargetInfo()
    {

    }
    
    public void addLocation(String location)
    {
        // update lated time;
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        latestLocationUpdateTime = dateFormat.format(date); //2014-08-22 15:59:48  
        numbeOfLocationUpdates++;
        
        if (location.contains("gps") || location.contains("network") || location.contains("WF:ENC"))
        {
        	latestInfoHasLoc = true;
        }
        else
        {
        	latestInfoHasLoc = false;
        }
        
        
    	// look for DE: string and add current time there
    	String lines[] = location.split("\n");
    	String newLocation = "";
    	for (int i=0;i<lines.length;i++)
    	{
    		if (lines[i].indexOf("DE:") == 0)
    		{
    			newLocation = newLocation + "DE:"+ latestLocationUpdateTime +"\n";
    		}
    		else if (lines[i].trim().length() != 0)
    		{
    			newLocation = newLocation + lines[i] + "\n";
    		}
    	}        	  
    	targetLocationList.add(newLocation);
    	
        if (targetLocationList.size()>10)
        {
        	targetLocationList.removeFirst();
        }           
        	
        
    }
    
    public String getLatestLocation()
    {
    	if (targetLocationList.size() == 0) return null;
    	
    	return targetLocationList.getLast();
    }
    
    public String getLatestLocationUpdateTime()
    {
    	return latestLocationUpdateTime;
    }    
    
    public int getNumOfLocations()
    {
    	return targetLocationList.size();
    }
    
    public String getAllLocations(String seperator)
    {
    	String retString = "";
    	String sepString = "\n";
    	if (seperator != null && seperator.length() !=0)
    	{
    		sepString = seperator;
    	}
    		
        ListIterator<String> listIterator = targetLocationList.listIterator();
        while (listIterator.hasNext()) {
        	String locationString = listIterator.next();
        	retString += locationString+"\n" ;
        	if (listIterator.hasNext())
        	{
        		retString += sepString+"\n";
        	}
        }
        return retString;    	
    }
    
    public String getTargetInfo(String myPhone)
    {
    	String retString = "";

    	//myPhone: Last update time:hasLocation : updateTimes: delogTime
    	retString = myPhone+ "," + getLatestLocationUpdateTime() + ","+latestInfoHasLoc+","+
    			numbeOfLocationUpdates + ","+ debugLogTime + "\n"; 

        return retString;    	
    }    
    
    public void udpateDebugLog(String debugString)
    {
    	debugLog = debugString;
        // update lated time;
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        debugLogTime = dateFormat.format(date); //2014-08-22 15:59:48  
    	    	    
    }
    
    public String getDebugLog()
    {
    	return debugLog;
    }
    
    public String getDebugLogtime()
    {
    	return debugLogTime;
    }



}