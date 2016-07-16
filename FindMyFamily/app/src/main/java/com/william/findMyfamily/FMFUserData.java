package com.william.findMyfamily;


import com.william.fmfCommon.*;
import java.util.ArrayList;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;


public class FMFUserData{
	private String userName="";
	private FMCLocationData fmpLocationData;
	private int maxHistoryRecords=10;	

	private int trackCount=0;
	private String trackBeginTime;
	private String trackEndTime;
	
	private ArrayList <FMCLocationData >oldLocationDatas = new ArrayList<FMCLocationData >(maxHistoryRecords);
	
	// Presentations
	public static int MAP_STATE_NOT_IN_MAP=0;
	public static int MAP_STATE_PENDING_DISPLAY=1;	
	public static int MAP_STATE_DISPLAYED_IN_MAP=2;
	public static int MAP_STATE_PENDING_REMOVE=3;	

	private int mapState=MAP_STATE_NOT_IN_MAP; 
	private Marker  marker=null;	
	private Circle circle=null;
	private int color;
	

	
	
	public FMFUserData()
	{}	
	
	public FMFUserData(String phoneNumber)
	{
		fmpLocationData = new FMCLocationData();
		fmpLocationData.setPhoneNumber(phoneNumber);
	}		
	
	public FMFUserData(FMCLocationData d)
	{
		fmpLocationData  = d;
	}
	
	public void addNewLocationData(FMCLocationData newLocationData)
	{
		if (oldLocationDatas != null && oldLocationDatas.size() > maxHistoryRecords)
		{
			oldLocationDatas.remove(maxHistoryRecords-1);  // remove the last one
		}
		oldLocationDatas.add(0, fmpLocationData);
		fmpLocationData = newLocationData;
		System.out.println("Now "+userName+" has "+oldLocationDatas.size()+" history records");
	}
	
	public FMCLocationData getFmpLocationData() {
		return fmpLocationData;
	}
	public void setFmpLocationData(FMCLocationData fmpLocationData) {
		this.fmpLocationData = fmpLocationData;
	}
	
	public ArrayList <FMCLocationData> getOldLocationDatas() {
		return oldLocationDatas;
	}

	public void setOldLocationDatas(ArrayList <FMCLocationData> oldLocationDatas) {
		this.oldLocationDatas = oldLocationDatas;
	}
	
	public int getMaxHistoryRecords() {
		return maxHistoryRecords;
	}

	public void setMaxHistoryRecords(int maxHistoryRecords) {
		this.maxHistoryRecords = maxHistoryRecords;
	}
	
	public Marker getMarker() {
		return marker;
	}
	public void setMarker(Marker marker) {
		this.marker = marker;
	}	
	public int getColor() {
		return color;
	}
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getMapState() {
		return mapState;
	}

	public void setMapState(int mapState) {
		this.mapState = mapState;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}
	
	public int getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(int trackCount) {
		this.trackCount = trackCount;
	}

	public String getTrackBeginTime() {
		return trackBeginTime;
	}

	public void setTrackBeginTime(String trackBeginTime) {
		this.trackBeginTime = trackBeginTime;
	}

	public String getTrackEndTime() {
		return trackEndTime;
	}

	public void setTrackEndTime(String trackEndTime) {
		this.trackEndTime = trackEndTime;
	}
	
	
	public void removeFromMap()
	{
		if (circle != null)
			circle.remove();
		if (marker != null)
			marker.remove();
		mapState = MAP_STATE_NOT_IN_MAP;
	}	

	public String getSavableStringofObject()
	{
		String retString = "UN:"+getUserName()+" \n";
		
		retString += getFmpLocationData().getMessageArrayFromObject(true)[0];
		for (int i=0;i<getOldLocationDatas().size();i++)
		{
			retString += getOldLocationDatas().get(i).getMessageArrayFromObject(true)[0];			
		}		
		return retString;
	}
	
	public int buildObjectFromString(String inputString)
	{
		// UN:abcde\n
		// [FMFRSP 0 0]\n

		// UN:ABCDEFG\n
		userName = inputString.substring("UN:".length(),inputString.indexOf("\n")).trim();
		String recordString  = inputString.substring(inputString.indexOf("\n")+1);

		String [] userData = recordString.split("[" + FMCLocationData.HEADER +":0 0]\n");

		if (userData == null || userData.length < 1)
		{
			System.out.print("No location record !!!");
			return 0;
		}
		
		System.out.print("buildObjectFromString: " + userData.length+ " location records\n");
		
		fmpLocationData.composeObjectFromMessage(userData[0] , null);

		if (userData.length <= 1)
			return 1;

		for (int i=1;i<userData.length;i++)
		{
			FMCLocationData fmpLocationHistoryData = new FMCLocationData();
			fmpLocationHistoryData.composeObjectFromMessage(userData[i] , null);
			oldLocationDatas.add(0, fmpLocationHistoryData);
		}
		return userData.length;
	}
	
	public boolean equals (Object o)
	{
		if (o == null || o.getClass() != FMFUserData.class)
            return false;

		FMFUserData other = (FMFUserData)o;
		if (this.fmpLocationData == null  || other.fmpLocationData == null)
			return false;
		
		System.out.println("comparing "+this.fmpLocationData.getPhoneNumber()+ " with " + other.fmpLocationData.getPhoneNumber()
		+ "result shd be: " + (fmpLocationData.equals(other.fmpLocationData)));
		
	    return (fmpLocationData.equals(other.fmpLocationData));
	}
	
}