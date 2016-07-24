package com.wl.fmfServer.data;

import java.text.*;
import java.util.Vector;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;

import com.wl.fmfServer.mainoffice.MainOfficeHandler;
import com.wl.fmfServer.mainoffice.MainOfficeServer;
import com.william.fmfCommon.FMCLocationData;



public class Tools
{

    public static String OS = "";
    public static String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss EEE";
    public static String DATE_FORMAT_DAYONLY = "yyyy/MM/dd";
    public static int TIMEUPWARNING = 5;
    public static int KILLWARNING = 2;

    public static String CLIENT_PROPERTIES_FILE = "client.properties";
    public static String CLIENT_FIRST_TIME = "firsttime";
    public static String CLIENT_HOST_PORT = "hostPort";
    public static boolean isFirstTime = false;

    public static String SERVER_PROPERTIES_FILE = "goodkid.properties";
    public static String SERVER_DATAFILE = "data";
    public static String SERVER_CONFIGFILE = "conf";
    public static String SERVER_HISTORYFILE = "hist";
    public static String SERVER_LOGFILE = "log";
    public static String SERVER_WARNINGAUDIO = "defaultWarningAudio";
    public static String SERVER_CUSTOMAUDIO1 = "customAudio1";
    public static String SERVER_CUSTOMAUDIO2 = "customAudio2";
    public static String SERVER_CUSTOMAUDIO3 = "customAudio3";
    public static String SERVER_CUSTOMAUDIO4 = "customAudio4";
    public static String SERVER_IPPORT = "ipPort";
    public static String SERVER_PASSWORD = "pw";
    public static String SERVER_USERNAME = "username";

    public static String MAINOFFICE_PROPERTIES_FILE = "mainoffice.properties";
    public static String MAINOFFICE_DATAFILE = "m_data";
    public static String MAINOFFICE_CONFIGFILE = "m_conf";
    public static String MAINOFFICE_HISTORYFILE = "m_hist";
    public static String MAINOFFICE_LOGFILE = "m_log";
    public static String MAINOFFICE_HOSTNAME1 = "m_officeHost1";
    public static String MAINOFFICE_IPPORT1 = "m_officeIpPort1";
    public static String MAINOFFICE_HOSTNAME2 = "m_officeHost2";
    public static String MAINOFFICE_IPPORT2 = "m_officeIpPort2";
    public static String MAINOFFICE_PASSWORD = "m_pw";
    public static String MAINOFFICE_USERNAME = "m_username";
    public static String MAINOFFICE_MYSQLHOST = "m_mysqlHost";
    public static String MAINOFFICE_MYSQLUSERID = "m_mysqlUserID";
    public static String MAINOFFICE_MYSQLPASSWORD = "m_mysqlPassword";

    public static String MOBILE_PROPERTIES_FILE = "mobileclient.properties";

    public static Logger logger;

//    public static Date serverStart;
    public static long startTime;
    public static String sqlUp = "DOWN";

    /*
     * Assumes that prevDayAndTime comes in format yyyy/mm/dd
     */
    public static int compareDate(String prevDayAndTime){
        // First line is the date
        Calendar today = Calendar.getInstance();
        Calendar todayRefined = Calendar.getInstance();
        Calendar prevDay = Calendar.getInstance();

//        System.out.println("Today is : " + todayDay +", prevDay is:"+ prevDay);

        todayRefined.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1,today.get(Calendar.DAY_OF_MONTH));
        prevDay.set((new Integer (prevDayAndTime.substring(0,4))).intValue(),
                    (new Integer (prevDayAndTime.substring(5,7))).intValue(),
                    (new Integer (prevDayAndTime.substring(8,10))).intValue());

        long todayMill = todayRefined.getTimeInMillis();
        long prevMill = prevDay.getTimeInMillis();

        long diffDays = (todayMill - prevMill) / (24 * 60 * 60 * 1000);

        System.out.println("today Refined day is yyyy/mm/dd:" +today.get(Calendar.YEAR) + "," +
                (today.get(Calendar.MONTH)+1) + ","+
                today.get(Calendar.DAY_OF_MONTH));
        System.out.println("prev day is yyyy/mm/dd:" +(new Integer (prevDayAndTime.substring(0,4))).intValue() + "," +
                (new Integer (prevDayAndTime.substring(5,7))).intValue() + ","+
                (new Integer (prevDayAndTime.substring(8,10))).intValue());

        return (int)diffDays;
    }


    public static String encryptPassword(String pw)
    {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash = new byte[40];
            md.update(pw.getBytes("iso-8859-1"), 0, pw.length());
            sha1hash = md.digest();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < sha1hash.length; i++) {
                int halfbyte = (sha1hash[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = sha1hash[i] & 0x0F;
                } while(two_halfs++ < 1);
            }
            return buf.toString();
        }
        catch ( Exception e)
        {
            return null;
        }
    }

    public static Properties getProperties(String pFileString){
        Properties prop = new Properties();
        try {

            FileInputStream propFile = new FileInputStream(pFileString);
            prop.load(propFile);
            propFile.close();
            return prop;
        }

        catch( FileNotFoundException filenotfoundexxption ) {
            System.out.println( "goodkid.properties, does not exist" );
        }
        catch( IOException ioexception ){
            ioexception.printStackTrace( );
        }
        return null;
    }


    public static boolean setProperties(String propFileString, Properties prop){
         // Update properties file
        try {
            FileOutputStream propFile = new FileOutputStream(propFileString);
            prop.store(propFile, null);
            propFile.close();
            return true;
        }

        catch( FileNotFoundException filenotfoundexxption ) {
            System.out.println( Tools.SERVER_PROPERTIES_FILE+" does not exist" );
        }
        catch( IOException ioexception ){
            ioexception.printStackTrace( );
        }
        return false;
    }


    public static Vector <UserItem>readFromPropertiesfile(String propFileString )
    {
        Properties prop = null;
        prop = Tools.getProperties(propFileString);

        return readFromPropertiesfile(prop);
   }

    public static Vector <UserItem>readFromPropertiesfile(Properties prop)
    {
        Vector <UserItem>retVector = new Vector<UserItem>();

        if (prop == null){
            return retVector;
        }

        // Check first time flag.

        if (prop.getProperty(Tools.CLIENT_FIRST_TIME) != null &&
                prop.getProperty(Tools.CLIENT_FIRST_TIME).equals("1")) {
            isFirstTime = true;
        }

        String hostPortString = prop.getProperty(Tools.CLIENT_HOST_PORT);

        String delims = "[,]";
        System.out.println("Host port String is :"+hostPortString);

        if (hostPortString == null || hostPortString.trim().length() == 0) {
            return retVector;
        }

        String[] tokens = hostPortString.split(delims);

        if (tokens == null || tokens.length == 0) {
            return retVector;
        }


        for (int i=0;i<tokens.length;i++) {
            String beanDelims = "[:]";
            String[] beanTokens = tokens[i].split(beanDelims);
            UserItem uItem = new UserItem(beanTokens[0], beanTokens[2], Integer.parseInt(beanTokens[3]));
            uItem.setDescription(beanTokens[1]);
            if (beanTokens.length == 5) {
                uItem.setUserPassword(beanTokens[4]);
            }
            System.out.println("Read "+ uItem +", pw:"+uItem.getUserPassword());
            retVector.add(uItem);
        }

        return retVector;

    }

    public static String trimPhoneNumber(String ph)
    {
        String retString="";
        // remove all non alphabets
        if (ph == null || ph.length() == 0)
        {
            return retString;
        }

        for (int i=0;i<ph.length();i++)
        {
            char c = ph.charAt(i);
            if (c >= '0' && c<='9')
            {
                retString = retString + c;
            }
        }

        // remove first 1
        if (retString.length()== 11 && retString.charAt(0)=='1')
            retString = retString.substring(1);

        return retString;
    }


    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 5280 ;  // 1 mile = 52980 feet

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts decimal degrees to radians                        :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts radians to decimal degrees                        :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static boolean shouldUpdateLink(String previousLocInfo, String currentLocInfo)
    {
        if (previousLocInfo==null || previousLocInfo.length()==0)
            return true;

        // 0 is prev, 1 is curr
        final String MSGTAG_LOCATION="LOC:";
        final int LOCATION_GPS=0;
        final int LOCATION_NETWORK=1;

        long [] locTime =  new long[2];
        double [] locLatt = new double[2];
        double [] locLong =  new double[2];
        String [] locString = {previousLocInfo, currentLocInfo};

        for (int index=0; index<2;index++)
        {
            // split into lines
            locLatt[index] = 0;
            locLong[index] = 0;
            locTime[index] = 0;


            // parse Location
            // 0 GPS, 1 is network
            long [] tempLocTime =  new long[2];
            double [] tempLocLatt =  new double[2];
            double [] tempLocLong =  new double[2];

            float [] tempAccuracy = new float[2];

            String lines[] = locString[index].split("\\r?\\n");
            for (int i=0;i<lines.length;i++)
            {
                if (lines[i].contains(MSGTAG_LOCATION))
                {
                    // * LOC:1 <network 2014/01/20 17:33:22 32.9759 -96.7204 1210>
                    String a = lines[i].substring(lines[i].indexOf("<")+1,lines[i].indexOf(">"));
                    String[] result = a.split(" ");
                    System.out.println("location has elmements:"+result.length);

                    int loctype = -1;

                    if (result[0].equalsIgnoreCase("network"))
                    {
                        loctype = LOCATION_NETWORK;
                    }
                    else if (result[0].equalsIgnoreCase("gps"))
                    {
                        loctype = LOCATION_GPS;
                    }
                    else break;

                    tempLocTime[loctype] =0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
                    try{
                        java.util.Date date = sdf.parse(new String(result[1]+" "+result[2]));
                        tempLocTime[loctype] = date.getTime();
                    }
                    catch (Exception e) {
                        System.out.println("date parse exception");
                    }

                    tempLocLatt[loctype] = Double.parseDouble(result[3]);
                    tempLocLong[loctype] = Double.parseDouble(result[4]);
                    tempAccuracy[loctype] = Float.parseFloat(result[5]);
                }
            }// inner for

            // best of this one
            int bestIndex = getBestLoc(tempLocTime, tempAccuracy);
            locTime[index] = tempLocTime[bestIndex];
            locLong[index] = tempLocLong[bestIndex];
            locLatt[index] = tempLocLatt[bestIndex];

        }// outer for

        // Now compare distances between the two locatins.
        // return true if it is more than 200 feet

        //
        return false;
    }

    public static int getBestLoc(long [] locTime, float [] acc)
    {
        // Decide Best Location
        int bestLocation = -1;

        if (locTime[0]==0 && locTime[1]!=0)
        {
            return 1;
        }
        else if (locTime[0]!=0 && locTime[1]==0)
        {
            return 0;
        }
        else if (locTime[0]!=0 && locTime[1]!=0)
        {
            // Both are not null
            // Check time
            bestLocation = 0;
            long delta  = locTime[1] - locTime[0];

            if (Math.abs(delta) < 20000) {
                // within 20 sec
                if (acc[0] < acc[1])
                {
                    bestLocation = 1;
                }
            }
            else
            {
                if (delta>0) bestLocation = 1;
            }
        }
        return bestLocation;

    }

//    public static void startServerCalendar(){
////        serverStart = Calendar.getInstance();
//      serverStart = new Date();
//    }

//    public static Date getServerStart(){
//      return serverStart;
//    }
    public static void startServerTimer(){
        startTime = System.currentTimeMillis();
    }
    public static long compareTimeInDays(long currentTime){
        long diff = currentTime - startTime;
        return diff / 86400000; //1000 milliseconds x 60 seconds x 60 min x 24 hours
    }

    public static String getIsSQLUP(){
        return sqlUp;
    }

    public static void setIsSQLUP(String b){
        sqlUp = b;
    }

    /*
     * Deletes records from table2 (locations) database
     * Ex: if numDaysOld = 2, then we delete any records
     * Since millisecond count increases every day, less milliseconds ==> "older" days ==> thus we would delete
     */
    public static void cleanupRecords(long numDaysOld){
        if(numDaysOld <= 0){
            System.out.println("You cannot clean up records less than a day old");
        }
        else{
            long timeRightNow = System.currentTimeMillis();
            long nthday = timeRightNow - (numDaysOld*86400000);     //86400000 = # milliseconds per day. ,
            System.out.println("Deleting rows from location_fields where DateInMilliseconds >= " + nthday);

            try{
                int rowsAffected = 0;   //field required for executeUpdate(), which works with DELETE statements

                /******1) Delete row from non_location_fields******/
                String query = "DELETE FROM non_location_fields WHERE DateInMilliseconds <= ?";
                MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
                MainOfficeServer.statement.setLong(1, nthday);

                //printout so we can see what's going on
                try{
                    rowsAffected = MainOfficeServer.statement.executeUpdate();
                }
                catch(SQLException se1){
                    se1.printStackTrace();
                    se1.getMessage();
                }

                /******2) Delete row from location_fields******/
                query = "DELETE FROM location_fields WHERE DateInMilliseconds <= ?";
                MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
                MainOfficeServer.statement.setLong(1, nthday);

                //printout so we can see what's going on
                try{
                    rowsAffected = MainOfficeServer.statement.executeUpdate();
                }
                catch(SQLException se1){
                    se1.printStackTrace();
                    se1.getMessage();
                }

            }
            catch(SQLException se){
                se.printStackTrace();
                se.getMessage();
            }


        }
    }


    /*
     * For a user, get data from databases ==> return an array of FMCLocationData Objects within the given times
     * Assume Date objects for begin and end are accurate
     * Conditions:
     *  1) numLocations >= 1
     *  2) Date begin must be past Date end
     *  3) Stuff regarding # of times:
     *      a) By default, if you ask for more locations than there are available locations for that phone #
     *          ==> return max amount of available locations for that phone #
     *      b) If there are 200 locations available within the given time period, and they ask for 10
                Return 10 evenly spaced (in terms of time) locations, or the closest possible
     */
    public static FMCLocationData[] getLocationsBetweenTimes(String phoneNumber, Date begin, Date end,int numLocations){
        FMCLocationData[] locations = new FMCLocationData[numLocations];

        if(numLocations < 1){
            System.out.println("numLocations must be at least 1, it currently is: " + numLocations);
        }

        long beginTime = begin.getTime();
        long endTime = end.getTime();

        if((endTime - beginTime) < 0){
            System.out.println("Beginning time must start before end time");
            return locations;
        }

        try{ //Get fields from 2 tables: non_location_fields and location_fields.
             //This means there will be 3 separate queries ==> 3 separate ResultSet objects
             /*
              * 1) Query 1: Find out # rows that fit the conditions. Only have to do this on one table because
              *         both tables should have the SAME AMOUNT of entries, of which each entry has a
              *         corresponding entry in the other table
              * 2) Query 2: Get all the rows that fit the conditions from non_location_fields ==> extract data
              * 3) Query 3: Get all the rows that fit the conditions from location_fields ==> extract data
              *
              * Then, we will use the extracted data ==> create FMCLocationData objects ==> fill array
              */
            /*********** Query 1 from LOCATION_FIELDS: Get # of rows********************************************************************************************/
            ResultSet resultSet;
            String query = "SELECT COUNT(PhoneNumber) FROM (SELECT PhoneNumber FROM location_fields WHERE PhoneNumber = ? and DateInMilliseconds between ? and ?) AS T";
            MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
            MainOfficeServer.statement.setString(1, phoneNumber);
            MainOfficeServer.statement.setLong(2, beginTime);
            MainOfficeServer.statement.setLong(3, endTime);

            //printout so we can see what's going on
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/DD HH:MM:SS");
            System.out.println("Finding # of rows from location_fields during time period: "
                    + dateFormat.format(begin) + " - " + dateFormat.format(end));

            resultSet = MainOfficeServer.statement.executeQuery();

            int locationsExtracted = 0; //# of FMCLocationData objects actually found in the database
            if(resultSet.next()){   //if current row is valid, then extract
                locationsExtracted = resultSet.getInt("COUNT(PhoneNumber)");
            }
            else{

            }



            /***********Query 2 from NON_LOCATION_FIELDS: Extract data************************************************************************/
            query = "SELECT * FROM non_location_fields WHERE PhoneNumber = ? and DateInMilliseconds between ? and ? ORDER BY DateInMilliseconds desc";
            MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
            MainOfficeServer.statement.setString(1, phoneNumber);
            MainOfficeServer.statement.setLong(2, beginTime);
            MainOfficeServer.statement.setLong(3, endTime);

            //printout so we can see what's going on
            System.out.println("Extracting rows from non_location_fields during time period: "
                    + dateFormat.format(begin) + " - " + dateFormat.format(end));

            //execute query1 and extract data elements. Should only return 1 row
            //Create variables here so that they can be used at the very end when we put stuff into FMCLocationData objects ==> then into the array
            resultSet = MainOfficeServer.statement.executeQuery();
            String charging = "";
            boolean mobiledata = false;
            boolean gps = false;
            boolean mobilenetwork = false;
            String wifi = "";

            if(resultSet.next()){   //if current row is valid, then extract
                //don't need phoneNumber, we got that already
                charging = resultSet.getString("ChargingMode");
                String mobiledataString = resultSet.getString("MobileData");
                String gpsString = resultSet.getString("GPS");
                String mobilenetworkString = resultSet.getString("MobileNetwork");
                wifi = resultSet.getString("Wifi");

                //since inside FMCLocationData, the fields above (except charging,wifi) are booleans, we need to do some logic here
                mobiledata = (mobiledataString.equals("ON")) ? true : false;
                gps = (gpsString.equals("ON")) ? true : false;
                mobilenetwork = (mobilenetworkString.equals("ON")) ? true : false;


            }
            else{
                throw new SQLException("getLocationsBetweenTimes method: something messed up");
            }

            /***********Query 3 from LOCATION_FIELDS: Extract data ************************************************************************/

            query = "SELECT * FROM location_fields WHERE PhoneNumber = ? and DateInMilliseconds between ? and ? ORDER BY DateInMilliseconds desc";
            MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
            MainOfficeServer.statement.setString(1, phoneNumber);
            MainOfficeServer.statement.setLong(2, beginTime);
            MainOfficeServer.statement.setLong(3, endTime);

            //printout so we can see what's going on
            System.out.println("Extracting rows from location_fields during time period: "
                    + dateFormat.format(begin) + " - " + dateFormat.format(end));

            //execute query2 and extract data elements
            /* For each row
             *  1) Extract data elements
             *  2) Combine w/ query1 elements to form a FMCLocationData object
             *  3) Add into array
             */

//              ArrayList<FMCLocationData> allLocations = new ArrayList<FMCLocationData>();
            FMCLocationData[] allLocations = new FMCLocationData[locationsExtracted];   //temporary array to house all the FMCLocationData objects
            resultSet = MainOfficeServer.statement.executeQuery();
            int index = 0;
            while(resultSet.next()){
                String datetime = resultSet.getString("DateTimeReceived");
                long datemillis = resultSet.getLong("DateInMilliseconds");
                String best = resultSet.getString("BestLocation");
                String loc1 = resultSet.getString("Location_1");
                String loc2 = resultSet.getString("Location_2");
                int battery = resultSet.getInt("BatteryLevel");

//                  public FMCLocationData(String phone, String timeR, long timemillis, int battery, String charging, boolean mobiled,
//                          boolean gps, boolean network, String wifi, String best, String loc1, String loc2)

                //create a new FMCLocationData object with the extracted data
                FMCLocationData temp = new FMCLocationData(phoneNumber, datetime,datemillis, battery, charging, mobiledata, gps,mobilenetwork,wifi, best, loc1, loc2);
                allLocations[index] = temp;                 //place FMCLocationData object inside a temporary array
                index++;
            }

            /*
             * Now compare values of locationsExtracted and numLocations
             * Possible situations:
             * 1) numLocations >= locationsExtracted:
             *      -Means that we wanted more locations than were available
             *      -Thus, we just return all the locations
             * 2) numLocations < locationsExtracted:
             *      -Means that we have to evenly space out (numLocation) amount of locations over the entire timespan
             *      -Also, there's the added fact that most likely, the split will not exactly coincide with a location
             *      -Situations:
             *          A)If numLocations == 1
             *              -Get the most recent location
             *              -This should be at the top, since we ordered it by DateInMilliseconds and put it in descending order
             *          B)If numLocations == 2
             *              -Get the most recent location + least recent location
             *              -Locations should be at the top and bottom, since we ordered it by DateInMilliseconds and put it in descending order
             *          C) Anything else
             *
             */

            if( numLocations >= locationsExtracted){
                for(int i = 0; i < locationsExtracted; i++){
                    locations[i] = allLocations[i];
                }
            }
            else{
                if(numLocations == 1){
                    locations[0] = allLocations[0];
                }
                else if(numLocations == 2){
                    locations[0] = allLocations[0];
                    locations[1] = allLocations[allLocations.length - 1];
                }
                else{
                     /*     b) If there are 200 locations available within the given time period, and they ask for 10
                    Return 10 evenly spaced (in terms of time) locations, or the closest possible*/
                    int average = locationsExtracted / numLocations;    //this will truncate so we can get a somewhat even distribution
                    int locationIndex = 0;
                    for(int k = 0; k < locationsExtracted; k += average){
                        locations[locationIndex] = allLocations[k];
                        locationIndex++;
                    }
                }
            }
        }
        catch(SQLException se){
            se.printStackTrace();
        }


        return locations;



    }









}