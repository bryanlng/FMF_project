package com.wl.fmfServer.data;

public class Communication
{
	
    public static String COMMAND_END = "[End]";
    public static String COMMAND_LOGIN = "[Login]";
    /*
    public static String COMMAND_UPDATEPASSWORD = "[CommandUpdatePassword]";
    public static String COMMAND_UPDATEUSERNAME = "[CommandUpdateUserName]";
    public static String COMMAND_CONFIGUPDATE = "[CommandCUpdate]";
    public static String COMMAND_SERVERRESTART = "[CommandSRestart]";
    public static String COMMAND_CONFIGDATAREQUEST = "[CommandCRequest]";
    public static String COMMAND_DATAREQUEST = "[CommandDRequest]";
    public static String COMMAND_PROGRAMLISTREQUEST = "[CommandProgramRequest]";
    public static String COMMAND_USERLISTREQUEST = "[CommandUserListRequest]";
    public static String COMMAND_KILLPROGRAMREQUEST = "[CommandKillProgramRequest]";
    public static String COMMAND_SLOWDOWNREQUEST = "[CommandSlowDownRequest]";
    public static String COMMAND_SNAPSHOTREQUEST = "[CommandSnapShotRequest]";


    public static String COMMAND_DOS = "[CommandDOS]";
    public static String COMMAND_IM = "[IM]";
    public static String COMMAND_SUSPEND = "[Suspend]";
    public static String COMMAND_RESUME = "[Resume]";
    public static String COMMAND_LOGOFFWINUSER = "[LogoffWinUser]";
    public static String COMMAND_PLAYAUDIO = "[PlayAudio]";
    public static String COMMAND_SHUTDOWN = "[Shutdown]";
*/
    public static String RESPONSE_LOGINOK = "[LoginOk]";
    public static String RESPONSE_LOGINFAIL = "[LoginFailed]";
    public static String RESPONSE_UPDATEPASSWORDOK = "[ResponseUpdatePasswordOk]";
    public static String RESPONSE_UPDATEPASSWORDFAIL = "[ResponseUpdatePasswordFailed]";
    public static String RESPONSE_CONFIGRUPDATE = "[ResponseCUpdate]";
    public static String RESPONSE_CONFIGDATA = "[ResponseCRequest]";
    public static String RESPONSE_DATAREQUEST = "[ResponseDRequest]";
    public static String RESPONSE_PROGRAMLISTREQUEST = "[ResponseProgramRequest]";
    public static String RESPONSE_USERLISTREQUEST = "[ResponseUserListRequest]";
    public static String RESPONSE_SNAPSHOTREQUEST = "[ResponseSnapShotRequest]";
    public static String RESPONSE_BINARY = "[ResponseBinary]";

 //   public static String RESPONSE_DOS = "[ResponseDOS]";
    public static String RESPONSE_END = "[End]";
//    public static String RESPONSE_IM = "[IM]";

    public static String COMMAND_LOGINFROMTARGET = "[CommandLoginFromTarget]";
    public static String COMMAND_DIAGDUMPINFO = "ww";

    public static String RESPONSE_LOGINFROMTARGETOK = "[LoginFromTargetOk]";
    public static String RESPONSE_LOGINFROMTARGETFAIL = "[LoginFromTargetFailed]";

    public static int DEFAULT_SERVER_PORT=12345;
    public static int DEFAULT_MAINOFFICE_PORT=12355;

    public Communication()
    {

    }


}
