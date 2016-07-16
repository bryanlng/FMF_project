package com.william.fmfCommon;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class FMCRawLocation {
	public static String LOCATION_GPS_STRING = "gps";
	public static String LOCATION_NETWORK_STRING = "network";
	
	private String mProvider="";
	private long mTime=0;
	private double mLatitude=0;
	private double mLongitude=0;
	private float mAccuracy=0;
	private float mBearing=0;	
	private float mSpeed=0;
	//private int mNumOfSat=0;
			
	
	public FMCRawLocation()
	{}
	
	public FMCRawLocation(String provider)
	{
		mProvider = provider;
	}		
	
	public boolean convertFromStringToObject(String locationString)
	{
        // network 2014/01/20 17:33:22 32.9759 -96.7204 1210 21 22
       // String a = locationString.substring(locationString.indexOf("<")+1,locationString.indexOf(">"));
        String[] result = locationString.split(" ");
        System.out.println("location has elmements:"+result.length);
        setProvider(result[0]);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);

        try {
        	java.util.Date date = sdf.parse(result[1]+" "+result[2]);
            setTime(date.getTime());
        }
        catch (Exception e) {
            setTime(0);
        }
        
        setLatitude (Double.parseDouble(result[3]));
        setLongitude(Double.parseDouble(result[4]));
        setAccuracy(Float.parseFloat(result[5]));
        if (result.length>=8)
        {
            setBearing(Float.parseFloat(result[6]));
            setSpeed(Float.parseFloat(result[7]));                            	
        }

		return true;		
	}
	
	public String returnStringFromObject()
	{
        // network 2014/01/20 17:33:22 32.9759 -96.7204 1210 21 22
		String wholeMessage = "";
		
//        wholeMessage += MSGTAG_LOCATION+ Integer.toString(locationCount) + " <";
        wholeMessage += getProvider() + " ";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
        cal.setTimeInMillis(getTime());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        wholeMessage += dateFormat.format(cal.getTime())+ " ";
        DecimalFormat dFormat = new DecimalFormat("##.0000");

        wholeMessage += dFormat.format(getLatitude())+ " ";
        wholeMessage += dFormat.format(getLongitude())+ " ";
        wholeMessage += Math.round(getAccuracy())+ " ";
        // New added bearing and speed
        wholeMessage += Math.round(getBearing())+" ";
//        wholeMessage += Math.round((getSpeed()*3600)/1609); // m/s to mi/h
        wholeMessage += Math.round((getSpeed())); 
        
        /* Ignore no. of Sat for now
        try {
            if (i==LOCATION_GPS)
            {
                int sat = fMPLocations[i].getExtras().getInt("satellites");
                wholeMessage += " " + sat;
            }
        }
        catch (Exception e2)
        {
            System.out.println("No satallites info");
        }
        */
        return wholeMessage;
	}
	
	public void setProvider(String provider)
	{
		 mProvider = provider;
	}
	public String getProvider()
	{
		return mProvider;
	}
	
	public void setTime( long time)
	{
		mTime = time;	
	}	
	public long getTime()
	{
		return mTime;	
	}
	
	public void setLatitude(double latitude)
	{
		mLatitude=latitude;		
	}
	public double getLatitude()
	{
		return mLatitude;		
	}
	
	public void setLongitude(double longitude)
	{
		mLongitude=longitude;		
	}
	public double getLongitude()
	{
		return mLongitude;		
	}
	
	public void setAccuracy(float accuracy)
	{
		mAccuracy=accuracy;		
	}
	public float getAccuracy()
	{
		return mAccuracy;		
	}
	
	public void setBearing(float bearing)
	{
		mBearing=bearing;		
	}
	public float getBearing()
	{
		return mBearing;		
	}
	
	public void setSpeed(float speed)
	{
		mSpeed=speed;		
	}
	public float getSpeed()
	{
		return mSpeed;		
	}
	
	public String toString()
	{
		return returnStringFromObject();		
	}
}
