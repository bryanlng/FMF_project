package com.william.fmfCommon;

public class FMCLocationData{
    /*
     * 
     * The following is the locationData in String format
     * ------------------ MESSAGE BEGIN ----
     * [FMPRSP:0 9]
     * PH:
     * DE:
     * BA:100%
     * CH:USB
     * MD:ON
     * GP:ON
     * NW:ON
     * TK:ON
     * IN:ON
     * LOC:1 <network 2014/01/20 17:33:22 32.9759 -96.7204 1210>
     * LOC:2 <gps 2014/01/20 17:33:22 32.9759 -96.7204 1210 270 20 5>
     * WF:ENC <Yeah5> or WF:DIS or WF:ENG or WF:ENN <ATT88,ADF,WWFWE>
     * ------------------- MESSAGE END ----
     */

    private String phoneNumber="";
    private String updateDateString="";
    private String timeReceived="";
    private long timeReceivedInMillis=0;
    private int batteryLevel=0;
    private String chargingMethod="";
    private boolean isMobileDataON=false;
    private boolean isGPSON=false;
    private boolean isNetworkON=false;
    private boolean isTrackON=false;
    private boolean interactiveON=false;
    private FMCRawLocation[] fMPLocations = new FMCRawLocation[NO_OF_LOCATIONS];  // network and GPS

    private FMCRawLocation bestLocation ;
    private String wifiStatus="";
    private String [] wifiAvailableSSIDs;
    private String connectedWifiSSID="";
    private String fullWifiMessage="";

    private String[] messagesToSend=null;
    private String[] messagesReceived=null;
    private boolean messageDecodeCompleted=false;
    
    


    private int maxMessageSize=140;
    /*
     * [FMPRSP:0 9 +11234567890]
     * BA:100%
     * CH:USB
     * MD:ON
     * GP:ON
     * NW:ON
     * LOC:1 <network 2014/01/20 17:33:22 32.9759 -96.7204 1210>
     * LOC:2 <gps 2014/01/20 17:33:22 32.9759 -96.7204 1210 5>
     * WF:ENC <Yeah5> or WF:DIS or WF:ENG or WF:ENN <ATT88,ADF,WWFWE>
     */
    public static String HEADER="FMPRSP";
    public static String MSGTAG_PHONENO="PH:";
    public static String MSGTAG_DATE="DE:";
    public static String MSGTAG_BATTERY="BA:";
    public static String MSGTAG_CHARGE="CH:";
    public static String MSGTAG_MODATA="MD:";
    public static String MSGTAG_GPS="GP:";
    public static String MSGTAG_NETWORK="NW:";
    public static String MSGTAG_TRACK="TK:";
    public static String MSGTAG_INTERACTIVE="IN:";
    public static String MSGTAG_LOCATION="LOC:";
    public static String MSGTAG_WIFI="WF:";

    public static int LOCATION_GPS=0;
    public static int LOCATION_NETWORK=1;
//  public static int LOCATION_PASSIVE=2;
    public static final int NO_OF_LOCATIONS=2;

    public FMCLocationData()
    {

    }
    
    /*
     * New constructor created to be able to help get stuff from the database
     * Go ahead and do the wifi logic inside here
     * Go ahead and also fill in fmpLocations[], bestlocation here as well
     */
    public FMCLocationData(String phone, String timeR, long timemillis, int battery, String charging, boolean mobiled,
    		boolean gps, boolean network, String wifi, String best, String loc1, String loc2)
    {
    		//quickly fill out fields in here
    		phoneNumber = phone;
    		timeReceived = timeR;
    		timeReceivedInMillis = timemillis;
    		batteryLevel = battery;
    		chargingMethod = charging;
    		isMobileDataON = mobiled;
    		isGPSON = gps;
    		isNetworkON = network;
    		
    		//wifi stuff. Use same extraction method as composeObjectFromMessage
    		//Example wifi: "WF:ENC <<Yeah5>>
    		//Fill in variables wifiStatus, wifiAvailableSSIDs, connectedWifiSSID, fullWifiMessage="";
    		fullWifiMessage = wifi;
    		setWifiStatus(wifi.substring(wifi.indexOf(":")+1, wifi.indexOf(":")+4));
            if (wifi.contains("<<"))
            {
                this.connectedWifiSSID = wifi.substring(wifi.indexOf("<<")+2, wifi.indexOf(">>"));
                //Wifi SSID = wifi name
                //Ex: connectedWifiSSID = Yeah5
                
            }
            else if (wifi.contains("<"))
            {
                String a = wifi.substring(wifi.indexOf("<")+1, wifi.indexOf(">"));
                this.wifiAvailableSSIDs = a.split(",");
                //Wifi SSID = wife name
                //Ex: Puts ATT88,ADF,WWFWE ==> String array of available WIFIs
            }
    		
    		//location stuff. Just making FMCRawLocations and putting them inside the array
    		bestLocation = new FMCRawLocation();
    		bestLocation.convertFromStringToObject(best);
    		
    		//check if network or gps is null, cause they may be
    		//idk if retrieval from SQL database will make it null or ""
    		if(loc1 == null || loc2 == null){
    			if(loc1 != null && loc2 == null){	//loc1 is good, loc2 isn't
    				FMCRawLocation one = new FMCRawLocation();
        			one.convertFromStringToObject(loc1);
        			fMPLocations[0] = one;
    			}
    			else if(loc1 == null && loc2 == null){ //loc2 is good, loc1 isn't
        			FMCRawLocation two = new FMCRawLocation();
        			two.convertFromStringToObject(loc2);
        			fMPLocations[1] = two;
    			}
    		}
//    		if(loc1.equals("") || loc2.equals("")){
//    			if(!loc1.equals("") && loc2.equals("")){	//loc1 is good, loc2 isn't
//    				FMCRawLocation one = new FMCRawLocation();
//        			one.convertFromStringToObject(loc1);
//        			fMPLocations[0] = one;
//    			}
//    			else if(loc1.equals("") && !loc2.equals("")){ //loc2 is good, loc1 isn't
//        			FMCRawLocation two = new FMCRawLocation();
//        			two.convertFromStringToObject(loc2);
//        			fMPLocations[1] = two;
//    			}
//    		}
    		//both are good
    		else{
    			FMCRawLocation one = new FMCRawLocation();
    			FMCRawLocation two = new FMCRawLocation();
    			one.convertFromStringToObject(loc1);
    			two.convertFromStringToObject(loc2);
    			
    			fMPLocations[0] = one;
    			fMPLocations[1] = two;
    		}
    }

    //  Used to compose Outgoing Message from Object
    public String[] getMessageArrayFromObject(boolean singleMessage)
    {
        String wholeMessage="";
        wholeMessage += MSGTAG_BATTERY + this.batteryLevel +"\n";
        if (this.chargingMethod != null && this.chargingMethod.length() != 0)
        {
            wholeMessage += MSGTAG_CHARGE + this.chargingMethod +"\n";
        }
        wholeMessage += MSGTAG_MODATA + new String(this.isMobileDataON ? "ON":"OFF") + "\n";
        wholeMessage += MSGTAG_GPS + new String(this.isGPSON ? "ON":"OFF") + "\n";
        wholeMessage += MSGTAG_NETWORK + new String(this.isNetworkON ? "ON":"OFF") + "\n";
        wholeMessage += MSGTAG_TRACK + new String(this.isTrackON ? "ON":"OFF") + "\n";
        wholeMessage += MSGTAG_INTERACTIVE + new String(this.interactiveON ? "ON":"OFF") + "\n";
        int locationCount=1;
        for (int i=0;i<NO_OF_LOCATIONS;i++)
        {
            if (this.fMPLocations[i]!=null)
            {
                wholeMessage += MSGTAG_LOCATION+ Integer.toString(locationCount) + " <";
                wholeMessage += this.fMPLocations[i].returnStringFromObject();
                /*
                wholeMessage += fMPLocations[i].getProvider() + " ";
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                cal.setTimeInMillis(fMPLocations[i].getTime());
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
                wholeMessage += dateFormat.format(cal.getTime())+ " ";
                DecimalFormat dFormat = new DecimalFormat("##.0000");

                wholeMessage += dFormat.format(fMPLocations[i].getLatitude())+ " ";
                wholeMessage += dFormat.format(fMPLocations[i].getLongitude())+ " ";
                wholeMessage += Math.round(fMPLocations[i].getAccuracy())+ " ";
                // New added bearing and speed
                wholeMessage += Math.round(fMPLocations[i].getBearing())+" ";
                wholeMessage += Math.round((fMPLocations[i].getSpeed()*3600)/1609); // m/s to mi/h
                * Ignore no. of Sat for now
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
                *
                
                **/
                wholeMessage +=  ">\n";
                locationCount++;
            }
        }

        //WF:ENC <Yeah5> or WF:DIS or WF:ENG or WF:ENN <ATT88,ADF,WWFWE>
        wholeMessage += MSGTAG_WIFI + this.getWifiStatus() + " ";
        if (connectedWifiSSID != null){
            wholeMessage += "<<"+this.connectedWifiSSID + ">>\n";
        }
        else if (this.wifiAvailableSSIDs != null)
        {
            wholeMessage += "<";
            for (int i=0;i<this.wifiAvailableSSIDs.length;i++)
            {
                wholeMessage += wifiAvailableSSIDs[i];
                if (i!=this.wifiAvailableSSIDs.length-1)
                {
                    wholeMessage += ",";
                }
            }
            wholeMessage += ">\n";
        }

        System.out.println("Return Whole message length "+ wholeMessage.length()+" is\n" + wholeMessage);

        if (singleMessage)
        {
            String hearder = "[" + HEADER +":0 0]\n" +
             MSGTAG_PHONENO + this.phoneNumber + "\n" +
             MSGTAG_DATE+ this.updateDateString + "\n";
            wholeMessage = hearder +wholeMessage;
            messagesToSend = new String[1];
            messagesToSend[0] = wholeMessage;
            return messagesToSend;
        }

        int wMessageLength=wholeMessage.length();
        // [FMPRSP:1 9]\n has size of 13


        int messSegmentMaxLength = maxMessageSize - 15;

        int numberOfSegments = (wMessageLength / messSegmentMaxLength) + 1;
        if (numberOfSegments > 10) numberOfSegments = 10;
        messagesToSend = new String[numberOfSegments];
        int index=0;
        for (int i=0;i<numberOfSegments;i++)
        {
            int endIndex = (index+messSegmentMaxLength) > wholeMessage.length()? wholeMessage.length(): index+messSegmentMaxLength;
            messagesToSend[i] = "[" + HEADER +":"+ Integer.toString(i) + " " +  Integer.toString(numberOfSegments-1) +"]\n";
            messagesToSend[i] += wholeMessage.substring(index,endIndex);
            index = index +messSegmentMaxLength;
        }

        System.out.println("Resulting messages to be sent:"+messagesToSend.length+"\n"+messagesToSend);

        return messagesToSend;
    }

    /*
     * Used to compose Object from incoming message
     * Sample retString:
     * [FMPRSP:0 0]
		PH:
		DE:
		BA:99
		CH:AC
		MD:OFF
		GP:ON
		NW:ON
		TK:OFF
		LOC:1 <gps 2015/10/16 07:14:27 33.0738 -96.7236 8 237 2 0>
		LOC:2 <network 2015/10/16 07:14:25 33.0740 -96.7235 24 0 0>
		WF:ENC <<Yeah5>>
     */
    public synchronized boolean composeObjectFromMessage(String inputString, String phoneNumber)
    {
        System.out.println("Ready to composeMessageFromString");

        //If phoneNumber is valid, make our private variable phoneNumber to be the string parameter
        if (phoneNumber != null && phoneNumber.length() != 0)
        {
            this.phoneNumber = phoneNumber;
        }
        else
        {
            System.out.println("composeObjectFromMessage: Ready to extract from p file");
            messagesReceived = new String[1];
        }


        try {
            // first time. Find array size
            if (messagesReceived==null||messagesReceived.length==0)
            {
                // [FMPRSP:0 0]
                int fmpIndex = inputString.indexOf(" ");
                int closeB = inputString.indexOf("]", fmpIndex);
                int size = Integer.parseInt(inputString.substring(fmpIndex+1, closeB)) + 1;	//Get the 2nd number of [FMPRSP:0 0] in inputString, which is actually retString
                messagesReceived = new String[size];	//make messagesRecieved by the size of that array
                System.out.println("New Size is "+size);
            }
            //System.out.println("*****************************");
            //System.out.println("Input String is now:"+inputString+"\nSize is "+messagesReceived.length);
            //System.out.println("*****************************");
 
            //Get the 1st number of [FMPRSP:0 0] in inputString, which is actually retString
            int msgIndex = Integer.parseInt(inputString.substring(inputString.indexOf(":")+1, inputString.indexOf(" ")));
            
            messagesReceived[msgIndex]=inputString;		//put inputString into messagedRecieved[first number]

            // Now check if all the messages arrived by parsing through the entire messagesReceived string array

            String fullMessage = "";
            for (int i=0;i<messagesReceived.length;i++)
            {
                if (messagesReceived[i]==null)	//if message isn't there, messageDecodeCompleted=false;
                {
                    // message not completed
                    messageDecodeCompleted=false;
                    System.out.println("Msg not completed yet");
                    return true;
                }
                else	//if message is there, then add it onto fullMessage
                {
                	//HEADER = "FMPRSP"
                    fullMessage +=messagesReceived[i].substring(HEADER.length()+7);
                }
            }

            // All the message array has been filled up.
            // System.out.println("Full Message length "+fullMessage.length()+" is->"+ fullMessage);

            /*
             * [FMPRSP:0 9]
             * PH:123123213
             * BA:100%
             * CH:USB
             * MD:ON
             * GP:ON
             * NW:ON
             * TK:ON
             * LOC:1 <network 2014/01/20 17:33:22 32.9759 -96.7204 1210>
             * LOC:2 <gps 2014/01/20 17:33:22 32.9759 -96.7204 1210 5>
             * WF:ENC <Yeah5> or WF:DIS or WF:ENG or WF:ENN <ATT88,ADF,WWFWE>
    public static String MSGTAG_LOCATION="LOC:";
    public static String MSGTAG_WIFI="WF:";
                 *
             */
            //Split the fullmessage by \n and \r ==> put inside of an array of Strings
            String lines[] = fullMessage.split("\\r?\\n");	
            System.out.println("Found number of lines:"+lines.length);
            
            //Parse through each String, check if it's "PH:", "DE:", etc
            for (int i=0;i<lines.length;i++)
            {
                if (lines[i].contains(MSGTAG_PHONENO) && phoneNumber == null)	//"PH:", length = 3. Extract number
                {
                    this.setPhoneNumber( lines[i].substring(MSGTAG_PHONENO.length() )) ;
                }
                else if (lines[i].contains(MSGTAG_DATE))						//"DE:", length = 3, Extract date
                {
//                    this.setUpdateDateString( lines[i].substring(MSGTAG_DATE.length() )) ;
                	 //Leave deprecated
                }
                else if (lines[i].contains(MSGTAG_BATTERY))						//"BA:", length = 3
                {
                    int bLevel;
                    try {
                        bLevel = Integer.parseInt(lines[i].substring(MSGTAG_BATTERY.length()));
                        this.setBatteryLevel(bLevel);
                    }
                    catch (NumberFormatException e){	//no battery left
                        this.setBatteryLevel(-1);
                    }
                }
                else if (lines[i].contains(MSGTAG_CHARGE))						//"CH:", length = 3	
                {
                    this.setChargingMethod(lines[i].substring(MSGTAG_CHARGE.length()));
                }
                else if (lines[i].contains(MSGTAG_MODATA))						//"MD:", length = 3
                {
                    this.setMobileDataON(lines[i].contains("ON"));
                }
                else if (lines[i].contains(MSGTAG_GPS))							//"GP:", length = 3
                {
                    this.setGPSON(lines[i].contains("ON"));
                }
                else if (lines[i].contains(MSGTAG_NETWORK))						//"NW:", length = 3
                {
                    this.setNetworkON(lines[i].contains("ON"));
                }
                else if (lines[i].contains(MSGTAG_TRACK))						//"TK:", length = 3		
                {
                    this.setTrackON(lines[i].contains("ON"));
                }
                else if (lines[i].contains(MSGTAG_INTERACTIVE))					//"IN:", length = 3
                {
                    this.setInteractiveON(lines[i].contains("ON"));
                }
                else if (lines[i].contains(MSGTAG_LOCATION))					//"LOC:", length = 4
                {
                // * LOC:1 <network 2014/01/20 17:33:22 32.9759 -96.7204 1210>
                	FMCRawLocation location = new FMCRawLocation();
                	//extract the "network 2014/01/20 17:33:22 32.9759 -96.7204 1210" part out
                    String locationString = lines[i].substring(lines[i].indexOf("<")+1,lines[i].indexOf(">"));
                    System.out.println("locationString: " + locationString);
                    
                	location.convertFromStringToObject(locationString);     		//fill location with data 
                	setTimeReceived(location.getTimeInDateFormat());				//set time field (string) so MainOfficeHandler can use it
                	setTimeReceivedInMillis(location.getTime());					//set time field in millis(long) so MainOfficeHandler can use it
                	
                	// System.out.println("My location is "+location.getProvider());	//either "network" or "gps"
                    if (location.getProvider().equalsIgnoreCase(FMCRawLocation.LOCATION_GPS_STRING)){	//LOCATION_GPS_STRING = gps
                        fMPLocations[LOCATION_GPS] = location; //fMPLocations[0] = location; Add location into fMPLocations[0]
                    }
                    else if (location.getProvider().equalsIgnoreCase(FMCRawLocation.LOCATION_NETWORK_STRING)){//LOCATION_NETWORK_STRING = network
                        fMPLocations[LOCATION_NETWORK] = location;	// //fMPLocations[1] = location; Add location into fMPLocations[1]
                    }

                }
                else if (lines[i].contains(MSGTAG_WIFI)) {						//"WF:", length = 3
                //* WF:ENC <<Yeah5>> or WF:DIS or WF:ENG or WF:ENN <ATT88,ADF,WWFWE>
                	fullWifiMessage = lines[i];
                	setWifiStatus(lines[i].substring(lines[i].indexOf(":")+1, lines[i].indexOf(":")+4));	//set wifi status to be the word after "WF:". Ex: "ENC"
                    if (lines[i].contains("<<"))
                    {
                        this.connectedWifiSSID = lines[i].substring(lines[i].indexOf("<<")+2, lines[i].indexOf(">>"));
                        //Wifi SSID = wifi name
                        //Ex: connectedWifiSSID = Yeah5
                        
                    }
                    else if (lines[i].contains("<"))
                    {
                        String a = lines[i].substring(lines[i].indexOf("<")+1, lines[i].indexOf(">"));
                        this.wifiAvailableSSIDs = a.split(",");
                        //Wifi SSID = wife name
                        //Ex: Puts ATT88,ADF,WWFWE ==> String array of available WIFIs
                    }

                }
            }

            // Decide Best Location
           //if only network location is available ==> get the network location
            if (fMPLocations[LOCATION_GPS]==null && fMPLocations[LOCATION_NETWORK]!=null)	//if only network location is available ==> get 
            {
                bestLocation = fMPLocations[LOCATION_NETWORK];	//LOCATION_NETWORK = 1
            }
           //if only gps location is available ==> get the gps location 
            else if (fMPLocations[LOCATION_GPS]!=null && fMPLocations[LOCATION_NETWORK]==null)
            {
                bestLocation = fMPLocations[LOCATION_GPS];		//LOCATION_GPS= 0
            }
            //if both gps and network locations are available, choose the best one
            else if (fMPLocations[LOCATION_GPS]!=null && fMPLocations[LOCATION_NETWORK]!=null)
            {
                // Both are not null
                // Check time
                bestLocation = fMPLocations[LOCATION_GPS];	//By default, make the best location from GPS
                //find time difference (in milliseconds) between GPS and network 
                long delta  = fMPLocations[LOCATION_NETWORK].getTime() - fMPLocations[LOCATION_GPS].getTime();
                
                //if within 10 second difference, compare accuracy. Accuracy = accuracy to x feet, thus smaller = better
                if (Math.abs(delta) < 10000) {
                    // within 10 sec
                	//If network accuracy < gps accuracy ==> use network accuracy
                    if (fMPLocations[LOCATION_NETWORK].getAccuracy() < fMPLocations[LOCATION_GPS].getAccuracy())
                    {
                        bestLocation = fMPLocations[LOCATION_NETWORK];
                    }
                }
                else
                {
                    if (delta>0) bestLocation = fMPLocations[LOCATION_NETWORK];
                }
            }
            if (bestLocation != null)
                System.out.println("Best Provider is "+bestLocation.getProvider());

            // System.out.println("Finish composeObjectFromString: completed");
            messagesReceived = null;
            messageDecodeCompleted=true;

            return true;
        }
        catch (Exception e)
        {
            System.out.println("composeMessageFromString exception:"+e);
            e.printStackTrace();
            return false;
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        int plusLoc = phoneNumber.indexOf("+");
        if (plusLoc >= 0)
        {
            phoneNumber = phoneNumber.substring(plusLoc+1);
        }
        this.phoneNumber = phoneNumber;
    }

    public String getUpdateDateString() {
        return updateDateString;
    }

    public void setUpdateDateString(String updateDateString) {
        this.updateDateString = updateDateString;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getChargingMethod() {
        return chargingMethod;
    }

    public void setChargingMethod(String chargingMethod) {
        this.chargingMethod = chargingMethod;
    }

    public boolean isMobileDataON() {
        return isMobileDataON;
    }

    public void setMobileDataON(boolean isMobileDataON) {
        this.isMobileDataON = isMobileDataON;
    }

    public boolean isGPSON() {
        return isGPSON;
    }
    public void setGPSON(boolean isGPSON) {
        this.isGPSON = isGPSON;
    }

    public boolean isNetworkON() {
        return isNetworkON;
    }
    public void setNetworkON(boolean isNetworkON) {
        this.isNetworkON = isNetworkON;
    }

    public boolean isTrackON() {
        return isTrackON;
    }
    public void setTrackON(boolean isTrackON) {
        this.isTrackON = isTrackON;
    }

    public boolean isInteractiveON() {return interactiveON;}
    public void setInteractiveON(boolean interactive) {
        this.interactiveON = interactive;
    }

    public FMCRawLocation[] getFMPLocations() {
        return fMPLocations;
    }

    public void setFMPLocations(FMCRawLocation[] fMPLocations) {
        this.fMPLocations = fMPLocations;
    }

    public void setFMPLocation(FMCRawLocation fMPLocation, int index) {
        fMPLocations[index] = fMPLocation;
    }

    public FMCRawLocation getFMPLocation(int locationIndex) {
        return fMPLocations[locationIndex];
    }

    public String getWifiStatus() {
        if (wifiStatus == null|| wifiStatus.length() == 0)
            return "UNK";
        else
            return wifiStatus;
    }

    public String getWifiDetailStatus() {
        if (wifiStatus == null|| wifiStatus.length() == 0)
            return "UNK";

        if (wifiStatus.equalsIgnoreCase("ENC")) {
            if (getConnectedWifiSSID()==null || getConnectedWifiSSID().length()==0)
                return "Enabled Connecting";
            else
                return "Enabled Connected";
        }
        else if (wifiStatus.equalsIgnoreCase("ECG"))
            return "Enabled Connecting";
        else if (wifiStatus.equalsIgnoreCase("ENG"))
            return "Enabling";
        else if (wifiStatus.equalsIgnoreCase("DIS"))
            return "Disconnected";
        else if (wifiStatus.equalsIgnoreCase("DIG"))
            return "Disconnecting";
        else return "UNK";
    }

    public void setWifiStatus(String wifiStatus) {
        this.wifiStatus = wifiStatus;
    }

    public String[] getWifiAvailableSSIDs() {
        return wifiAvailableSSIDs;
    }

    public void setWifiAvailableSSIDs(String[] wifiAvailableSSIDs) {
        this.wifiAvailableSSIDs = wifiAvailableSSIDs;
    }

    public String getConnectedWifiSSID() {
        return connectedWifiSSID;
    }

    public void setConnectedWifiSSID(String connectedWifiSSID) {
        this.connectedWifiSSID = connectedWifiSSID;
    }

    public String[] getMessagesToSend() {
        return messagesToSend;
    }

    public void setMessagesToSend(String[] messagesToSend) {
        this.messagesToSend = messagesToSend;
    }

    public String[] getMessagesReceived() {
        return messagesReceived;
    }

    public void setMessagesReceived(String[] messagesReceived) {
        this.messagesReceived = messagesReceived;
    }

    public FMCRawLocation getBestLocation() {
        return bestLocation;
    }

    public void setBestLocation(FMCRawLocation bestLocation) {
        this.bestLocation = bestLocation;
    }

    public boolean isMessageDecodeCompleted() {
        return messageDecodeCompleted;
    }

    public void setMessageDecodeCompleted(boolean messageDecodeCompleted) {
        this.messageDecodeCompleted = messageDecodeCompleted;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    
    public String getRawWifiMessage(){
    	return fullWifiMessage;
    }
    
    public String getTimeReceived(){
    	return timeReceived;
    }
    
    public void setTimeReceived(String s){
    	timeReceived = s;
    }
    
    public long getTimeReceivedInMillis(){
    	return timeReceivedInMillis;
    }
    
    public void setTimeReceivedInMillis(long s){
    	timeReceivedInMillis = s;
    }

    public boolean equals(Object o)
    {
    	FMCLocationData other = (FMCLocationData)o;
        System.out.println("Comparing local:"+this.phoneNumber+" and "+ other.phoneNumber + ", should rtn:"+(this.phoneNumber.equals(other.phoneNumber)));

        return (this.phoneNumber.equals(other.phoneNumber));
    }

    public String toString()
    {
        String retString[] =getMessageArrayFromObject(true);

        String rString = retString[0].toString();

        return rString;

    }
}