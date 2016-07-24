package com.william.findMyfamily;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

//  This function is not implemented anymore

/*
 * Commands:
 * 1. STATUS
 * 2. GETTRACK PHONENO TIMEBEGIN TIMEEND
 * 3. DELETERECORD PHONENO
 * 4. DELETE DATE BEGIN END
 * 
 * 
 */
public class FMFTrackDataCommunication extends AsyncTask <String, Void, String>{	
	protected String doInBackground(String... args) {
		int argLength = args.length;

		String url = "http://"+Tools.fmfServerAddr;
		int serverPort = Tools.fmfServerPort;
		String urlString = new String (url+":"+serverPort+"/"+Tools.fmfTrackRoute);

		if (argLength==0)
		{
			System.out.println("FMFTrackDataCommunication No arg Error ");
			return null;
		}

		if (args[0].equals(Tools.FMFCOMMANDSERVERSTATUS))
		{
			urlString = urlString + "/"+"list";
			String[] retString = sendToServer(urlString, null, "GET"); 
			Tools.setServerResult(retString);			
		}
		else if (args[0].equals(Tools.FMFCOMMANDSERVERSTATUS)){


		}
		return null;
	}

	protected void onPostExecute(String result) {
		System.out.println("FMFTrackDataCommunication: onPostExecute result:"+result);	 	
//		Toast.makeText(Tools.FMFMainContext, "No Location available for", Toast.LENGTH_LONG).show();   

	}

	private String[] sendToServer(String urlString, String postContent, String method)
	{
		String responseString[] = new String[2];
		
		String USER_AGENT = "Mozilla/5.0";	    	 
		try {
			URL obj = new URL(urlString);
			StringBuffer response = new StringBuffer();
			System.out.println("FMFTrackDataCommunication sendAndReceive URL:" + urlString+", method:"+method+", postContent:"+postContent );

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod(method);
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// Send post request
			if (method.equals("POST"))
			{
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(postContent);
				wr.flush();
				wr.close();
			}

			int responseCode = con.getResponseCode();
			responseString[0] = ""+responseCode;
			String responseContent = con.getResponseMessage();
			System.out.println("\nSending 'POST' request to URL : " + urlString);
			System.out.println("Post parameters : " + postContent);
			System.out.println("Response Code : " + responseCode);
			System.out.println("Response Content : " + responseContent);			

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			

			while ((inputLine = in.readLine()) != null) {				
				response.append(inputLine);
			}
			in.close();
			//print result
			System.out.println(response.toString());	 	
			responseString[1] = response.toString();
		}

		catch (Exception e)
		{
			System.out.println("sendToServer: Throws exception "+e);
			responseString[0] = "999";
			responseString[1] = e.getMessage();			
		}
		
		return responseString;

	}

}