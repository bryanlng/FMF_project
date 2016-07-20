package com.william.findMyfamily;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.maps.GoogleMap;
import com.william.fmfCommon.FMCLocationData;
import com.william.fmfCommon.FMCMessage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/*
 * Tools is a class with all static Global variables and Helper methods.
 */
public class Tools {

    public static GoogleMap map;

    // HashMap to hold incoming SMS message
    public static HashMap<String, FMCLocationData> incomingMessageHash = new HashMap<String, FMCLocationData>();

    // ArrayList for currently users on Map
    public static ArrayList<FMFUserData> mapUserList = new ArrayList<FMFUserData>();

    // FMFOfficeComm object
    public static FMFOfficeComm myOfficeConnection = null;


    public static Ringtone notificationRingTone;
    public static boolean activityExists = false;
    public static String latestActivityPH = "";
    public static boolean readingUserFromFileOk = false;
    public static int circleColor[] = {0x40ff0000, 0x40ffff00, 0x4000ff00,
            0x4000ffff, 0x400000ff, 0x40ff00ff};


    public static String FMF_PROPERTIES_FILE = "FMF.properties";
    public static String FMF_USERS = "FMFUsers";
    public static String FMF_LASTUPDATE = "FMFLastUpdate";

    public static String FMF_TRACKSERVER = "FMFTrackServer";
    public static String lastUpdateDateString;

    public static String FMP_FMPGETINFO_COMMAND = "FMPGETINFO";
    public static String FMP_FMPTRACK_COMMAND = "FMPTRACK";
    public static String FMP_WIFI_COMMAND = "FMPWIFI";
    public static String FMP_GPS_COMMAND = "FMPGPS";
    public static String FMP_MDATA_COMMAND = "FMPMOBILE";

    public static String FMP_ON = "ON";
    public static String FMP_OFF = "OFF";

    public static String FMFTRACK_TIMER = "TIMER";
    public static String FMFTRACK_SVADDR = "SVADDR";
    public static String FMFTRACK_ROUTE = "ROUTE";
    public static String FMFTRACK_SVPORT = "SVPORT";
    public static String FMFTRACK_STATE = "STATE";

    public static int FMFTRACKTmer = 120000;
    public static String FMFTRACKSVAddr = "wleungtx.no-ip.biz";
//    public static String FMFTRACKSVAddr = "192.168.1.20"; //192.168.1.22/

    public static String FMFTRACKRoute = "location";
    public static int FMFTRACKSVPort = 8081;

    public static String FMFCOMMANDSERVERSTATUS = "STATUS";
    public static ArrayList<FMFUserData> trackUserList = new ArrayList<FMFUserData>();
    public static Context FMFMainContext = null;
    public static FMFMainScreen FMFMainScreen = null;
    private static boolean FMFServerResponse = false;
    private static String[] FMFServerResult = new String[3];

    public static synchronized boolean checkForUpdates() {
        if (activityExists) {
            activityExists = false;
            return true;
        } else return false;
    }

    public static synchronized boolean getServerResponse() {
        return FMFServerResponse;
    }

    public static synchronized void setServerResponse(boolean b) {
        FMFServerResponse = b;
    }

    public static synchronized String[] getServerResult() {
        return FMFServerResult;
    }

    public static synchronized void setServerResult(String[] result) {
        FMFServerResult = null;

        if (result != null) {
            FMFServerResult = new String[3];

            for (int i = 0; i < result.length; i++) {
                FMFServerResult[i] = result[i];
            }
        }
        setServerResponse(true);
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static void sendFindCommand(int itemIndex) {
        sendFMPCommand(itemIndex, FMP_FMPGETINFO_COMMAND, null);
    }

    public static void sendFindCommandwithLAN(int itemIndex) {
        //sendFMPCommandwithLAN(itemIndex, FMP_FMPGETINFO_COMMAND, null);
        sendFMPCommandToServerWithLAN(itemIndex, FMCMessage.FMFOFFICE_CLIENTGETLATEST);
    }

    public static void sendTrackCommand(int itemIndex) {
        sendFMPCommand(itemIndex, FMP_FMPTRACK_COMMAND, null);
    }

    public static void sendFMPCommand(int itemIindex, String command, String parameter) {
        String phoneNumber = mapUserList.get(itemIindex).getFmpLocationData().getPhoneNumber();
        String message = command;
        message += parameter == null ? "" : new String(" " + parameter);
        SmsManager sms = SmsManager.getDefault();
        System.out.println("SMS Command to be sent:->" + message + "<-");
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public static void sendFMPCommandwithLAN(int itemIindex, String command, String parameter) {
        String remotePhoneNumber = mapUserList.get(itemIindex).getFmpLocationData().getPhoneNumber();
        String message = "[" + FMCMessage.FMFOFFICE_CLIENTCOMMAND + ":" + Tools.getMyPhoneNumber() + ":" + remotePhoneNumber + "]\n";

        message += command;
        message += parameter == null ? "" : new String(" " + parameter);
        message += "\n[" + FMCMessage.FMFOFFICE_CLIENTCOMMAND_END + "]";

        sendMessageThroughLAN(message);
    }

    public static void sendFMPCommandToServerWithLAN(int itemIindex, String command) {
        String remotePhoneNumber = "";
        if (itemIindex != -1) {
            remotePhoneNumber = mapUserList.get(itemIindex).getFmpLocationData().getPhoneNumber();
        }
        String message = "[" + command + ":" + Tools.getMyPhoneNumber() + ":" + remotePhoneNumber + "]";
        sendMessageThroughLAN(message);
    }

    public static void sendMessageThroughLAN(String message) {
        System.out.println("LAN Command to be sent:->" + message + "<-");
        if (myOfficeConnection == null) {
            System.out.println("sendMessageThroughLAN with new connection");
            myOfficeConnection = new FMFOfficeComm(Tools.FMFMainScreen);
            myOfficeConnection.setConnectingHostIP(FMFTRACKSVAddr, FMFTRACKSVPort);
            myOfficeConnection.connectToOffice(message);
            System.out.println("sendMessageThroughLAN connectToOffice DONE");
        } else {
            if (!myOfficeConnection.isConnectedToOffice() || !myOfficeConnection.isMessageReceived()) {
                System.out.print("sendMessageThroughLAN isConnectedToOffice:" + myOfficeConnection.isConnectedToOffice());
                System.out.println(", isMessageReceived:" + myOfficeConnection.isMessageReceived());

                myOfficeConnection.setConnectingHostIP(FMFTRACKSVAddr, FMFTRACKSVPort);
                myOfficeConnection.connectToOffice(message);
            } else {
                System.out.println("sendMessageThroughLAN with existing connection");
                myOfficeConnection.sendMessageToOffice(message, true);
                System.out.println("sendMessageThroughLAN sendMessageToOffice DONE");
            }
        }
    }

    public static void sendFMPTrackCommand(int itemIindex, String parameter) {
        String phoneNumber = mapUserList.get(itemIindex).getFmpLocationData().getPhoneNumber();
        String message = FMP_FMPTRACK_COMMAND;
        message += " PH:" + phoneNumber;

        if (parameter.equals(FMP_OFF)) {
            message += " " + FMFTRACK_STATE + ":" + parameter;
        } else {
            message += " " + FMFTRACK_TIMER + ":" + FMFTRACKTmer;
            message += " " + FMFTRACK_SVADDR + ":" + FMFTRACKSVAddr;
            message += " " + FMFTRACK_ROUTE + " :" + FMFTRACKRoute;
            message += " " + FMFTRACK_SVPORT + ":" + FMFTRACKSVPort;
            message += " " + FMFTRACK_STATE + ":" + parameter;
        }

        SmsManager sms = SmsManager.getDefault();
        System.out.println("SMS Command to be sent:->" + message + "<-");
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    public static ArrayList<FMFUserData> readFromPropertiesfile(Context c) {
        ArrayList<FMFUserData> retArrayList = new ArrayList<FMFUserData>();
        try {
            FMFMainContext = c;
            readingUserFromFileOk = true;
            System.out.println("readFromPropertiesfile");
            FileInputStream inputStream = c.openFileInput(Tools.FMF_PROPERTIES_FILE);
            Properties prop = new Properties();
            prop.load(inputStream);

            // Check first time flag.
            lastUpdateDateString = prop.getProperty(Tools.FMF_LASTUPDATE);
            System.out.println("FMF_LASTUPDATE String is :->" + lastUpdateDateString + "<-");

            // Load FMFTrackServer info
            String fmfServerString = prop.getProperty(Tools.FMF_TRACKSERVER);
            String delims = " ";
            System.out.println("fmfServerString String is :->" + fmfServerString + "<-");

            if (fmfServerString != null && fmfServerString.trim().length() > 0) {
                String[] tokens = fmfServerString.split(delims);

                for (int i = 0; i < tokens.length; i++) {
                    String[] tokens2 = tokens[i].split(":");
                    if (tokens2.length == 2 && tokens2[0] != null && tokens2[1] != null && tokens2[1].length() > 0) {
                        if (tokens2[0].equalsIgnoreCase(FMFTRACK_TIMER)) {
                            FMFTRACKTmer = Integer.parseInt(tokens2[1]);
                        } else if (tokens2[0].equalsIgnoreCase(FMFTRACK_SVADDR)) {

                            FMFTRACKSVAddr = tokens2[1];
                        } else if (tokens2[0].equalsIgnoreCase(FMFTRACK_ROUTE)) {

                            FMFTRACKRoute = tokens2[1];
                        } else if (tokens2[0].equalsIgnoreCase(FMFTRACK_SVPORT)) {
                            FMFTRACKSVPort = Integer.parseInt(tokens2[1]);
                        }
                    }
                }
            }


            String fmfUsersString = prop.getProperty(Tools.FMF_USERS);

            delims = "\t";
            System.out.println("FMF_USERS String is :->" + fmfUsersString + "<-");

            if (fmfUsersString == null || fmfUsersString.trim().length() == 0) {
                return retArrayList;
            }

            String[] tokens = fmfUsersString.split(delims);

            if (tokens == null || tokens.length == 0) {
                return retArrayList;
            }
            System.out.println("" + tokens.length + " records to be read");

            for (int i = 0; i < tokens.length; i++) {
                // UN:ABCDEFG\n
                String userName = tokens[i].substring("UN:".length(), tokens[i].indexOf("\n")).trim();
                FMCLocationData fmpData = new FMCLocationData();
                fmpData.composeObjectFromMessage(tokens[i].substring(tokens[i].indexOf("\n") + 1), null);
                System.out.println("Record " + i + "User:" + userName + ",phoneno: " + fmpData.getPhoneNumber());
                FMFUserData fmpMData = new FMFUserData();
                fmpMData.setUserName(userName);
                fmpMData.setFmpLocationData(fmpData);
                retArrayList.add(fmpMData);
            }
            return retArrayList;
        } catch (IOException e) {
            System.out.println("readFromPropertiesfile exception:" + e);
            readingUserFromFileOk = false;
            return retArrayList;
        }
    }

    public static void saveCurrentMapUsersToPropertiesfile(Context c) {
        System.out.println("saveCurrentMapUsersToPropertiesfile:" + readingUserFromFileOk);

        try {
            Properties properties = new Properties();
            String userRecordsString = "";

            if (readingUserFromFileOk) {
                // Track User Info

                String fmfServerString = FMFTRACK_TIMER + ":" + FMFTRACKTmer + " " +
                        FMFTRACK_SVADDR + ":" + FMFTRACKSVAddr + " " +
                        FMFTRACK_ROUTE + ":" + FMFTRACKRoute + " " +
                        FMFTRACK_SVPORT + ":" + FMFTRACKSVPort;
                System.out.println("Final fmfServerString string writes to property file is:\n" + fmfServerString);
                properties.setProperty(Tools.FMF_TRACKSERVER, fmfServerString);

                // User List
                if (mapUserList != null && mapUserList.size() != 0) {
                    System.out.println("saveCurrentMapUsersToPropertiesfile mapUserList size:" + mapUserList.size());

                    for (int i = 0; i < mapUserList.size(); i++) {
                        userRecordsString = userRecordsString +
                                "UN:" + mapUserList.get(i).getUserName() + " \n";
                        if (mapUserList.get(i).getFmpLocationData() != null)
                            userRecordsString += mapUserList.get(i).getFmpLocationData().getMessageArrayFromObject(true)[0];
                        if (i + 1 < mapUserList.size()) {
                            userRecordsString = userRecordsString + "\t";
                        }
                    }
                    System.out.println("Final string writes to property file is:\n" + userRecordsString);
                    properties.setProperty(Tools.FMF_USERS, userRecordsString);
                }
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            properties.setProperty(Tools.FMF_LASTUPDATE, dateFormat.format(cal.getTime()));

            FileOutputStream fos = c.openFileOutput(Tools.FMF_PROPERTIES_FILE, Context.MODE_PRIVATE);
            properties.store(fos, null);
            fos.close();

            String[] fList = c.fileList();
            System.out.println("dir is " + c.getFilesDir() + "," + fList.length);
            for (int i = 0; i < fList.length; i++) {
                System.out.println(fList[i] + ", ");
            }

        } catch (Exception e) {
            System.out.println("exception at write " + e);

        }

    }

    public static void playNotificationSound(Context context) {
        try {
            if (notificationRingTone == null) {
                RingtoneManager ringtoneMgr = new RingtoneManager(context);
                ringtoneMgr.setType(RingtoneManager.TYPE_NOTIFICATION);
                Cursor notiCursor = ringtoneMgr.getCursor();
                int notiCount = notiCursor.getCount();
                if (notiCount == 0 && !notiCursor.moveToFirst()) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    notificationRingTone = RingtoneManager.getRingtone(context, notification);
                } else {
                    Uri[] noti = new Uri[notiCount];

                    while (!notiCursor.isAfterLast() && notiCursor.moveToNext()) {
                        int currentPosition = notiCursor.getPosition();
                        noti[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
                        notificationRingTone = RingtoneManager.getRingtone(context, noti[currentPosition]);
                        if (notificationRingTone.getTitle(context).equalsIgnoreCase("Ascend"))
                            break;
                    }
                    notiCursor.close();
                }

            }

            if (notificationRingTone == null) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationRingTone = RingtoneManager.getRingtone(context, notification);
            }
            notificationRingTone.play();
        } catch (Exception e) {
        }

    }

    public static String trimPhoneNumber(String ph) {
        String retString = "";
        // remove all non alpabets
        if (ph == null || ph.length() == 0) {
            return retString;
        }

        for (int i = 0; i < ph.length(); i++) {
            char c = ph.charAt(i);
            if (c >= '0' && c <= '9') {
                retString = retString + c;
            }
        }

        // remove first 1
        if (retString.length() == 11 && retString.charAt(0) == '1')
            retString = retString.substring(1);

        return retString;
    }

    public static void removeItem(FMFUserData rData) {
        mapUserList.remove(rData);
    }


    public static Bitmap getContactPhoto(Context context, String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = context.getContentResolver();
        Cursor contact = cr.query(phoneUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
            return defaultPhoto;
        }
        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    cr, photoUri);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        } else {
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
            return defaultPhoto;
        }
        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
        return defaultPhoto;
    }

    public static String[] getPhoneContacts(Context context) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        String by = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC ";
        Cursor people = context.getContentResolver().query(uri, projection, null, null, by);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        people.moveToFirst();
        if (people.getCount() == 0) return null;
        String[] records = new String[people.getCount()];

        for (int i = 0; i < people.getCount(); i++) {
            records[i] = people.getString(indexName) + "\t" + people.getString(indexNumber);
            people.moveToNext();
        }
        return records;
    }

    public static String[] getRingToneList(Context context) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        String by = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC ";
        Cursor people = context.getContentResolver().query(uri, projection, null, null, by);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        people.moveToFirst();
        if (people.getCount() == 0) return null;
        String[] records = new String[people.getCount()];

        for (int i = 0; i < people.getCount(); i++) {
            records[i] = people.getString(indexName) + "\t" + people.getString(indexNumber);
            people.moveToNext();
        }
        return records;
    }

    public static void addNewFMFUserData(String userName, String phoneNumber) {
        FMFUserData FMFUserData = new FMFUserData(Tools.trimPhoneNumber(phoneNumber));
        FMFUserData.setUserName(userName);
        Tools.mapUserList.add(0, FMFUserData);
    }

    public static int getPositionOfMapUserList(String phone) {
        if (mapUserList == null || mapUserList.size() == 0) return -1;

        for (int i = 0; i < mapUserList.size(); i++) {
            FMFUserData aUser = mapUserList.get(i);
            if (phone.equals(aUser.getFmpLocationData().getPhoneNumber())) {
                return i;
            }
        }
        return -1;
    }

    public static boolean handleReceivedLocation(String updatedPH, String mBody) {
        System.out.println("handleReceivedLocation:From " + updatedPH + ", Body received\n" + mBody + "<-");
        if (mBody.contains(FMCLocationData.HEADER)) {

            FMCLocationData fmpLData = new FMCLocationData();
            fmpLData.composeObjectFromMessage(mBody, updatedPH);

            FMFUserData testData = new FMFUserData(fmpLData);

            int index = Tools.mapUserList.indexOf(testData);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));

            if (index != -1) {   // exist
                System.out.println("**** Found the same user ****");
                FMFUserData existingUserData = Tools.mapUserList.get(index);// get the existing
                if (existingUserData.getMarker() != null) {
                    existingUserData.getMarker().remove();
                    existingUserData.setMarker(null);
                }
                if (existingUserData.getCircle() != null) {
                    existingUserData.getCircle().remove();
                    existingUserData.setCircle(null);
                }

                if (fmpLData.getUpdateDateString() == null || fmpLData.getUpdateDateString().length() == 0) {
                    fmpLData.setUpdateDateString(dateFormat.format(cal.getTime()));
                }
                existingUserData.addNewLocationData(fmpLData);
                existingUserData.setMapState(FMFUserData.MAP_STATE_PENDING_DISPLAY);
                existingUserData.setColor(Tools.circleColor[index % 6]);
                Tools.latestActivityPH = existingUserData.getFmpLocationData().getPhoneNumber();

                // Only save it if existing user exists
                Tools.saveCurrentMapUsersToPropertiesfile(FMFMainContext);
            } else {
                if (testData.getFmpLocationData().getUpdateDateString() == null || testData.getFmpLocationData().getUpdateDateString().length() == 0) {
                    testData.getFmpLocationData().setUpdateDateString(dateFormat.format(cal.getTime()));
                }
                testData.setUserName("User" + testData.getFmpLocationData().getPhoneNumber());
                Tools.mapUserList.add(0, testData);
                testData.setColor(Tools.circleColor[Tools.mapUserList.size() % 6]);
                testData.setMapState(FMFUserData.MAP_STATE_PENDING_DISPLAY);
                Tools.latestActivityPH = testData.getFmpLocationData().getPhoneNumber();
            }
            return true;
        }
        return false;
    }

    public static String getMyPhoneNumber() {
        if (FMFMainContext == null) {
            return Double.toString(Math.random() * 10000000000.0).substring(0, 10);
        }
        TelephonyManager tMgr = (TelephonyManager) FMFMainContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tMgr == null) {
            return Double.toString(Math.random() * 10000000000.0).substring(0, 10);
        }

        String ph = tMgr.getLine1Number();
        if (ph == null || ph.length() == 0) {
            return Double.toString(Math.random() * 10000000000.0).substring(0, 10);
        }
        return tMgr.getLine1Number();
    }


}