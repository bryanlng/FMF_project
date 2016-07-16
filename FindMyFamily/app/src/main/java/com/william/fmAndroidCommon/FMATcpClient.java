package com.william.fmAndroidCommon;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FMATcpClient {

    public static final String SERVER_IP = "localhost"; //your computer IP address
    public static final int SERVER_PORT = 4444;

    private String serverIP = SERVER_IP;
    private int serverPort = SERVER_PORT;
    private String m_loginMessage = null;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    private Socket socket = null;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public FMATcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public FMATcpClient(String logonMessage, String ip, int port, OnMessageReceived listener) {
        mMessageListener = listener;
        serverIP = ip;
        serverPort = port;
        m_loginMessage = logonMessage;

    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        System.out.println("FMPTcpClient:sendMessage:Sending.....");
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
        else
        {
            System.out.println("FMPTcpClient:sendMessage:Sending Message FAILED. mBufferOut is :"+mBufferOut);
            stopClient();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection
        System.out.println("stopClient");
        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        if (mBufferIn != null)
        {
            try {
                mBufferIn.close();
            }
            catch(Exception ee) {}
        }
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

        if (socket != null)
        {
            try{
                socket.close();
                socket = null;
            }
            catch(Exception e)
            {}
        }
    }

    public boolean isConnected()
    {
        if (socket != null && socket.isConnected())
        {
            return true;
        }
        return false;
    }


    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, serverPort);

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send login name
                if (m_loginMessage != null && m_loginMessage.length() != 0) {
                    sendMessage(m_loginMessage);
                }
                System.out.println("login message is:" + m_loginMessage);

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                if (mRun)
                    Log.e("TCP", "S: Error", e);
                else
                    Log.e("TCP", "S: Exception, but its ok");

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);
            if (socket != null)
            {
                socket = null;
            }
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}