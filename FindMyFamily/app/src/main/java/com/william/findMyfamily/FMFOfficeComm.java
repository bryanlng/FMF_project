package com.william.findMyfamily;

import com.william.fmAndroidCommon.FMAOfficeCommunication;
import com.william.fmfCommon.FMCMessage;

/**
 * FMFOffice extends FMAOfficeCommunication
 * It also has to implement the handleReceivedMessage method, which is an abstract method in
 * FMAOfficeCommunication class
 */
public class FMFOfficeComm extends FMAOfficeCommunication{

    private FMFCallBackInterface callback = null;

    public FMFOfficeComm(FMFCallBackInterface mainCallback)
    {
        callback = mainCallback;
    }

    // This method is need to be implemented for abstract class FMFOfficeCommunication
    // It is called from FMFOfficeCommunication to handle received message

    /*  Message as shown
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
   */

    public void handleReceivedMessage(String receivedMsg)
    {
        System.out.println("  ------  FMFOfficeComm:handleReceivedMessage: Msg: " + receivedMsg);

        if (receivedMsg == null || receivedMsg.length() == 0 ||
                receivedMsg.contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_ERR) ||
                receivedMsg.contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_OK))
        {
            return;
        }

        String delimsLine = "\\r?\\n";
        String[] msgArray = receivedMsg.split(delimsLine);

        if (msgArray == null || msgArray.length < 2)
            return;
        System.out.println("  ------  FMFOfficeComm:handleReceivedMessage: Msg lines: " + msgArray.length);


        String readString=msgArray[0].substring(1, msgArray[0].length()-1);
        String delims = "[:]";
        String[] tokens = readString.split(delims);
        String command;
        String clientPhone = "X";
        String targetPhone = "Y";
        if (tokens.length == 3)
        {
            // [command:clientPhone:targetPhone]
            command = tokens[0];
            clientPhone = tokens[1];
            targetPhone = tokens[2];
        }
        else if (tokens.length == 2)
        {
            // [command:clientPhone]
            command = tokens[0];
            clientPhone = tokens[1];
        }
        else if (tokens.length == 1)
        {
            // [command]
            command = tokens[0];
        }
        else
        {
            System.out.println("Wrong format");
            return;
        }
        System.out.println("FMFOfficeComm::command:" + command + ", clientPhone:" + clientPhone + ", targetPhone:" + targetPhone +", lines:"+msgArray.length);

        //  The following if-else work on every possible responses sent from Office

        if (command.equals(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLATEST)) {
            System.out.println(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLATEST);
            String msg = "";
            for (int i=1;i<msgArray.length - 1;i++) {
                msg += msgArray[i];
                //if (i!=msgArray.length - 2)
                    msg += "\n";
            }
            //Tools.handleReceivedMsg(targetPhone, msg);
            if (Tools.handleReceivedLocation(targetPhone, msg))
            {
                callback.displayUserOnMap(targetPhone, msg);
            }

        }
        else if (command.equals(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETALLHIST)) {
            System.out.println(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETALLHIST);

            callback.postSimpleDialogBox(receivedMsg);  // Will be changed later
        }

        else if (command.equals(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS)) {
            System.out.println(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS);

            callback.postSimpleDialogBox(receivedMsg);  // Will be changed later

        }
        else if (command.equals(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLOG)) {
            System.out.println(FMCMessage.FMFOFFICE_CLIENTRESPONSE_GETLOG);

            callback.postSimpleDialogBox(receivedMsg); // Will be changed later

        }
        // Add more command response handling below if needed
    }


}
