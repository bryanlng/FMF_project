package com.william.findMyfamily;

import com.william.fmfCommon.*;
import com.william.fmAndroidCommon.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/*
 * This is the SMS receiver in version 1.
 * This receives SMS TEXT message and handle it.
 */
public class FMFSMSReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) 
    {	
		System.out.println("At FMFSMSReceiver onReceive");

	    String action = intent.getAction();
	    String type = intent.getType();
    	System.out.println("FMFSMSReceiver receives Action:"+action+", Type:"+type);	    	

        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs;
        if (bundle != null)
        {
        	System.out.println("FMFSMSReceiver onReceive:bundle");
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null)
            {
                msgs = new SmsMessage[pdus.length];
            }
            else return;

            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                String mBody = msgs[i].getMessageBody();
                String phoneNumber = msgs[i].getOriginatingAddress();    
                
                String updatedPH = Tools.trimPhoneNumber(phoneNumber);
                
                System.out.println("FMFSMSReceiver onReceive:From "+phoneNumber+":"+updatedPH+", bundle received\n"+mBody+"<-");
                if (mBody.contains(FMCLocationData.HEADER)){
                	abortBroadcast();
                	// mBody = mBody + "Phone:"+msgs[i].getOriginatingAddress();
                	
              		FMCLocationData fmpLData = Tools.incomingMessageHash.get(updatedPH);
                	if (fmpLData == null)
                	{
                		System.out.println("First message of "+updatedPH);
                		fmpLData = new FMCLocationData();	
                	}
                	else System.out.println("Subsequent message of "+updatedPH);
                	
                	fmpLData.composeObjectFromMessage(mBody,updatedPH);
                	Tools.incomingMessageHash.put(updatedPH,fmpLData);
                	
                	if (fmpLData.isMessageDecodeCompleted())
                	{

                		FMFUserData testData = new FMFUserData(fmpLData);                		

            		    int index = Tools.mapUserList.indexOf(testData);
            			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));	
            			           		    
            		    if (index != -1)
            		    {   // exist
            		    	System.out.println("**** Found the same user ****");
            		    	FMFUserData existingUserData = Tools.mapUserList.get(index);// get the existing
            		    	if (existingUserData.getMarker() != null)
            		    	{
            		    		existingUserData.getMarker().remove();
            		    		existingUserData.setMarker(null);
            		    	}
            		    	if (existingUserData.getCircle() != null) 
            		    	{
            		    		existingUserData.getCircle().remove(); 
            		    		existingUserData.setCircle(null);
            		    	}
            		    	
            		    	fmpLData.setUpdateDateString(dateFormat.format(cal.getTime())); 
            		    	existingUserData.addNewLocationData(fmpLData);
            		    	existingUserData.setMapState(FMFUserData.MAP_STATE_PENDING_DISPLAY);
            		    	existingUserData.setColor(Tools.circleColor[index % 6]);

							Tools.latestActivityPH = existingUserData.getFmpLocationData().getPhoneNumber();
									// Only save it if existing user exists
                    		Tools.saveCurrentMapUsersToPropertiesfile(context);            		    	
            		    }
            		    else
            		    {
            		    	testData.getFmpLocationData().setUpdateDateString(dateFormat.format(cal.getTime()));
            		    	testData.setUserName("User" + testData.getFmpLocationData().getPhoneNumber());
            		    	Tools.mapUserList.add(0, testData);
            		    	testData.setColor(Tools.circleColor[Tools.mapUserList.size() % 6]);   
            		    	testData.setMapState(FMFUserData.MAP_STATE_PENDING_DISPLAY);
							Tools.latestActivityPH = testData.getFmpLocationData().getPhoneNumber();
            		    }               		 		                		    
                		
            		    Tools.incomingMessageHash.remove(updatedPH);
            			System.out.println("About to startActivity .......... " + Tools.mapUserList.size());
            			Tools.activityExists = true;
                		Intent newIntent = new Intent(context,FMFMainScreen.class);
                		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);

                		context.startActivity(newIntent);
                		Tools.playNotificationSound(context);
                		
                	}
                }
            }

        }        
        System.out.println("FMFSMSReceiver:onReceive ends");
    }

}
