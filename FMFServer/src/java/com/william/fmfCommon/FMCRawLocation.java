package com.william.fmfCommon;

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
}
