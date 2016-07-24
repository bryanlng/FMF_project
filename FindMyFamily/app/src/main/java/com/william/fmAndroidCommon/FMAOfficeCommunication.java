package com.william.fmAndroidCommon;

import com.william.fmfCommon.FMCMessage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

/*
 * Commands:
 * 1. STATUS
 * 2. GETTRACK PHONENO TIMEBEGIN TIMEEND
 * 3. DELETERECORD PHONENO
 * 4. DELETE DATE BEGIN END
 *
 *
 */

public abstract class FMAOfficeCommunication {

    // Has to implement this method
    public abstract void  handleReceivedMessage(String receivedMsg);
    private FMATcpClient mTcpClient = null;
    private String mHost="wleungtx.no-ip.biz";//"192.168.1.8";

    private int mPort=8081;//23456;
    private String loginMessage=null;
    private boolean messageReceived=false;



    public FMAOfficeCommunication() {
    }

    public FMAOfficeCommunication(String ip, int port) {
        mHost = ip;
        mPort = port;
    }

    public void setConnectingHostIP(String ip, int port) {
        mHost = ip;
        mPort = port;
    }

    public boolean isConnectedToOffice()
    {
        if (mTcpClient != null && mTcpClient.isConnected()) return true;
        return false;
    }

    public void stopCommunication()
    {
        if (mTcpClient != null)
        {
            mTcpClient.stopClient();
        }
        mTcpClient = null;
    }

    public void connectToOffice(String msg) {
        System.out.println("FMFOfficeCommunication:connectToOffice:"+msg);
        loginMessage = msg;
        if (mTcpClient!= null && mTcpClient.isConnected())
        {
            mTcpClient.stopClient();
        }
        messageReceived = false;
        new ConnectToOfficeTask().execute("");
    }

    public boolean isMessageReceived()
    {
        return messageReceived;
    }
/*
    public void handleReceivedMessage(String receivedMsg)
    {
        String testMsg = "[FMPRSP:0 0]\nBA:77\nCH:USB\nWF:DIS<<>>\n";
        String pn = "9728977218";
        Tools.handleReceivedMsg(pn, testMsg);
    }*/

    public void sendMessageToOffice(String message) {
        sendMessageToOffice(message, false);
    }

    public void sendMessageToOffice(String message, boolean retry) {
        if (mTcpClient != null)
        {
            messageReceived = false;
            mTcpClient.sendMessage(message);

            if (retry)
                new CheckMsgReceiveTask().execute(message);
        }
        else {
            System.out.println("mTcpClient is still NULL");
        }
    }

    public class CheckMsgReceiveTask extends AsyncTask <String, String, Boolean>{

        private String receivedMessage = "";

        protected Boolean doInBackground(String... args) {

            //we create a TCPClient object and
            System.out.println(" **** CheckMsgReceiveTask:");

            try{
                Thread.sleep(5000);
                if (!messageReceived)
                {
                    System.out.println("NO RESPONSE after 5 sec. Restart connection and resend one more time");
                    connectToOffice(args[0]);
                }
                else
                {
                    System.out.println("RESPONSE Received. We are good.");
                }
            }
            catch (Exception e) {
                System.out.println("RESPONSE Received. Exception"+e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        protected void onPostExecute(String result) {

        }

    }


    public class ConnectToOfficeTask extends AsyncTask <String, String, FMATcpClient>{

        private String receivedMessage = "";

        protected FMATcpClient doInBackground(String... args) {

            //we create a TCPClient object and
            System.out.println(" **** ConnectToOfficeTask:"+mHost+":"+mPort+", Message:"+loginMessage);

            mTcpClient = new FMATcpClient(loginMessage, mHost, mPort,
                    new FMATcpClient.OnMessageReceived() {
                        @Override

                        //here the messageReceived method is implemented
                        public void messageReceived(String message) {
                            //this method calls the onProgressUpdate
                            publishProgress(message);
                        }
                    });
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            System.out.println("onProgressUpdate:" + values[0]);

            receivedMessage += values[0];
            if (values[0] != null) {
                if (values[0].contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_END) ||
                        values[0].contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_OK) ||
                        values[0].contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_ERR) ||
                        values[0].contains(FMCMessage.FMFOFFICE_CLIENTRESPONSE_NOTARGET)||
                        values[0].contains(FMCMessage.FMFOFFICE_TARGETRESPONSE_END)) //||
                        //values[0].contains(FMCMessage.FMFOFFICE_TARGETRESPONSE_LOGIN)||
                        //values[0].contains(FMCMessage.FMFOFFICE_TARGETRESPONSE_KEEPALIVE))
                {
                    messageReceived = true;
                    handleReceivedMessage(receivedMessage);
                    receivedMessage = "";
                }
                else {
                    receivedMessage+= "\n";
                }
            }
        }


        protected void onPostExecute(String result) {
            System.out.println("FMFOfficeCommunication: onPostExecute result:" + result);
        }

    }
}