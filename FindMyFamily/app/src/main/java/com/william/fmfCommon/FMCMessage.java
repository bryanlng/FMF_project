package com.william.fmfCommon;

/**
 * FMCMessage contains all the messages exchange between FMP, FMF and FMFServer
 */
public class FMCMessage {
    // *************************************************************************************
    //**** Client Commands. Client = from FindMyFamily. Client --> Server *****

    // List all Connected Target
    public static final String FMFOFFICE_CLIENTLISTALL = "ClientListAll";

    // A Generic Command (TBD)
    public static final String FMFOFFICE_CLIENTCOMMAND = "ClientCommand";
    public static final String FMFOFFICE_CLIENTCOMMAND_END = "ClientCmdEnd";

    // Get Lastest Location of A target
    public static final String FMFOFFICE_CLIENTGETLATEST = "ClientGetLatest";

    // Get all locations of A target
    public static final String FMFOFFICE_CLIENTGETALLHISTORY = "ClientGetAllHistory";

    // Get the current status of FMFServer
    public static final String FMFOFFICE_CLIENTGETSERVERSTATUS = "ClientGetServerStatus";

    // Get Log from a Target (TBD)
    public static final String FMFOFFICE_CLIENTGETLOG = "ClientGetLog";
    
    // Get history(locations) of a Target over a certain time period
    // Format: [ClientGetHistoryFromDB:#:begin:end:numLocations], 
    //		where begin and end are strings in form of millisecond values
    public static final String FMFOFFICE_CLIENTGETHISTORYFROMDB = "ClientGetHistoryFromDB";

    // Clean up DB by deleting entries older than n days old
    // Format: [CleanUpDB:#]
    public static final String FMFOFFICE_CLEANUPDB = "CleanUpDB";

    //Client Responses
    public static final String FMFOFFICE_CLIENTRESPONSE_LISTALL = "ClientListAllResponse";
    public static final String FMFOFFICE_CLIENTRESPONSE_GETLATEST = "ClientGetLatestResponse";
    public static final String FMFOFFICE_CLIENTRESPONSE_GETALLHIST = "ClientGetAllHistResponse";
    public static final String FMFOFFICE_CLIENTRESPONSE_GETSERVERSTATUS = "ClientGetServerStatusResponse";
    public static final String FMFOFFICE_CLIENTRESPONSE_GETLOG = "ClientGetLogResponse";
    public static final String FMFOFFICE_CLIENTRESPONSE_END = "ClientResponseEnd";
    public static final String FMFOFFICE_CLIENTRESPONSE_OK = "ClientRspOk";
    public static final String FMFOFFICE_CLIENTRESPONSE_ERR = "ClientRspErr";
    public static final String FMFOFFICE_CLIENTRESPONSE_NOTARGET = "ClientRspNoTarget";
    public static final String FMFOFFICE_CLIENTRESPONSE_SEPERATOR = "ClientRspSeperator";

    // *************************************************************************************
    //[TargetLogin: :9876543210]
    //[TargetKAlive: :9876543210] or [TargetTrack: :9876543210]
    //[FMPRSP:0 0]\nPH:\nDE:\nBA:80\nMD:ON\nGP:ON\nNW:ON\nTK:ON\nLOC:1 <gps 2015/01/19 15:53:19 33.0742 -96.7237 76 0 0 9>\nLOC:2 <network 2015/01/19 15:55:15 33.0687 -96.7122 1261 0 0>\nWF:ENC
    //[TargetResponseEnd]

    //[TargetResponseBegin:1234567890:9876543210]
    //[FMPRSP:0 0]\nPH:\nDE:\nBA:80\nMD:ON\nGP:ON\nNW:ON\nTK:ON\nLOC:1 <gps 2015/01/19 15:53:19 33.0742 -96.7237 76 0 0 9>\nLOC:2 <network 2015/01/19 15:55:15 33.0687 -96.7122 1261 0 0>\nWF:ENC
    //[TargetResponseEnd]

    //Target = From cellphones FindMyPhone. Target --> Server

    // Target login to Server
    public static final String FMFOFFICE_TARGETLOGIN = "TargetLogin";

    // Target reporting current location to Server
    public static final String FMFOFFICE_TARGETKEEPALIVE = "TargetKAlive";

    // Target returning log to server (TBD)
    public static final String FMFOFFICE_TARGETLOG = "TargetLog";

    // Server Sending a response back to Target
    public static final String FMFOFFICE_TARGETRESPONSE_BEGIN = "TargetResponseBegin";
    public static final String FMFOFFICE_TARGETRESPONSE_END = "TargetResponseEnd";
    public static final String FMFOFFICE_TARGETRESPONSE_LOGIN     = "TargetRspLogin";
    public static final String FMFOFFICE_TARGETRESPONSE_KEEPALIVE = "TargetRspKAlive";
    // public static final String FMFOFFICE_TARGETTRACK = "TargetTrack";

}
