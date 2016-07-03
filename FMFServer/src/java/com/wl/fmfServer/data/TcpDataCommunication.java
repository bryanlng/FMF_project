package com.wl.fmfServer.data;

import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TcpDataCommunication
{

    private Socket mySocket=null;
    private String remoteHostName;
    private int remoteIPPort;
    private PrintWriter outPrintWriter;
    private BufferedReader inReader;
    private BufferedWriter outBufferedWriter;

    public TcpDataCommunication() {
    }

    public TcpDataCommunication(Socket mSocket) {
        mySocket = mSocket;
        setBuilders();
    }


    public TcpDataCommunication(String hname, int ipport) {
        remoteHostName=hname;
        remoteIPPort=ipport;
    }

    public void makeConnection(boolean secured) throws Exception
    {
        System.out.println("makeConnection " + secured);
        try {
            if (secured) {
                SSLSocketFactory factory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();

                SSLSocket sclientSocket = (SSLSocket)factory.createSocket(remoteHostName, remoteIPPort);
                sclientSocket.startHandshake();
                mySocket = (Socket)sclientSocket;
            }
            else {
                mySocket = new Socket(remoteHostName,remoteIPPort+1);
            }
            if (!setBuilders() ) {
                throw new Exception ("Cannot Set builder");
            }

        }
        catch (Exception e) {
            // "Cannot Establish Communication to server";
            throw new Exception (e);
        }
    }

    public void makeConnection() throws Exception
    {
        try {
            makeConnection(true);
        }
        catch (Exception e) {
            /* Try unsecured communication */
            makeConnection(false);
        }
    }

    public void makeConnection(String hname, int ipport) throws Exception
    {
        remoteHostName=hname;
        remoteIPPort=ipport;
        try {
            makeConnection(true);
        }
        catch (Exception e) {
            /* Try unsecured communication */
            makeConnection(false);
        }
    }

    public boolean getConnectionStatus()
    {
        return mySocket == null ? false:!mySocket.isClosed();
    }

    public void disconnectSocket()
    {
        try { mySocket.close(); } catch (Exception eb){}
        mySocket=null;
    }

    private boolean setBuilders() {
        try {

            outPrintWriter = new PrintWriter(
                  new BufferedWriter(
                  new OutputStreamWriter(
                      mySocket.getOutputStream())));

            outBufferedWriter = new BufferedWriter(
                  new OutputStreamWriter(
                      mySocket.getOutputStream()));

            /* read response */
            inReader = new BufferedReader(
                    new InputStreamReader(
                    mySocket.getInputStream()));
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
        System.out.println("--- TcpDataCommunication Built all reader writers ---");
        return true;
    }
    public void setRemoteHostName(String hname){
        remoteHostName=hname;
    }

    public String getRemoteHostName(){
        return remoteHostName;
    }

    public void setRemoteIPPort(int iport ){
        remoteIPPort=iport;
    }

    public int getRemoteIPPort(){
        return remoteIPPort;
    }

    public void setOutPrintWriter(PrintWriter op ){
        outPrintWriter=op;
    }

    public PrintWriter getOutPrintWriter(){
        return outPrintWriter;
    }

    public void setInReader(BufferedReader br ){
        inReader=br;
    }

    public BufferedReader getInReader(){
        return inReader;
    }

    public void setOutBufferedWriter(BufferedWriter bw ){
        outBufferedWriter=bw;
    }

    public BufferedWriter getOutBufferedWriter(){
        return outBufferedWriter;
    }

    public OutputStream getOutputStream()
    {
        OutputStream os = null;
        try {
            os = mySocket.getOutputStream();
        }
        catch (IOException e){
            System.out.println("getOutputStream exception:"+e);
        }
        return os;
    }


    public InputStream getInputStream()
    {
        InputStream iStream = null;
        try {
            iStream = mySocket.getInputStream();
        }
        catch (IOException e){
        }
        return iStream;
    }
}
