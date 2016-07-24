package com.wl.fmfServer.mainoffice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.william.fmfCommon.FMCLocationData;

public class MainOfficeDB {

	private static MainOfficeDB myMainOfficeDB;

	//code taken from http://www.tutorialspoint.com/jdbc/jdbc-update-records.htm
	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	public static final String DB_URL = "jdbc:mysql://localhost/fmf_server";

	//  Database credentials
	public static final String USERNAME = "root";
	public static final String PASSWORD = "leung1601";

	//Connection objects that need to be accessed in MainOfficeHandler
	private Connection connection = null;

	private boolean isConnected = false;

	/* Static 'instance' method */
	public static MainOfficeDB getInstance( ) 
	{
		if (myMainOfficeDB == null)
		{
			myMainOfficeDB = new MainOfficeDB();
		}
		return myMainOfficeDB;
	}


	public boolean init(String url, String userid, String pw)
	{
		/***********************************SQL Initialization*****************************************************/
		try{
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 2: Register JDBC driver
			if (url==null) url = DB_URL;
			if (userid==null) userid = USERNAME;
			if (pw==null) pw = PASSWORD;

			
			//STEP 3: Open a connection
			//Use PreparedStatement instead of Statement to prevent SQL injections
			connection = DriverManager.getConnection(url, userid, pw);
			System.out.println("Connected to database successfully...");
			isConnected = true;
			return true;

		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}
		isConnected = false;
		return false;

	}

	public boolean isConnected()
	{
		return isConnected;    	
	}

	public String getStatus()
	{
		return isConnected?"UP":"DOWN";
	}


	/*
	 * Deletes records from table2 (locations) database
	 * Ex: if numDaysOld = 2, then we delete any records
	 * Since millisecond count increases every day, less milliseconds ==> "older" days ==> thus we would delete
	 */
	public synchronized void cleanupRecords(long numDaysOld){
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
				PreparedStatement statement = connection.prepareStatement(query);
				statement.setLong(1, nthday);

				//printout so we can see what's going on
				try{
					rowsAffected = statement.executeUpdate();
				}
				catch(SQLException se1){
					se1.printStackTrace();
					se1.getMessage();
				}

				/******2) Delete row from location_fields******/
				query = "DELETE FROM location_fields WHERE DateInMilliseconds <= ?";
				statement = connection.prepareStatement(query);
				statement.setLong(1, nthday);

				//printout so we can see what's going on
				try{
					rowsAffected = statement.executeUpdate();
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
	public synchronized FMCLocationData[] getLocationsBetweenTimes(String phoneNumber, Date begin, Date end,int numLocations){
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
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, phoneNumber);
			statement.setLong(2, beginTime);
			statement.setLong(3, endTime);

			//printout so we can see what's going on
			SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/DD HH:MM:SS");
			System.out.println("Finding # of rows from location_fields during time period: "
					+ dateFormat.format(begin) + " - " + dateFormat.format(end));

			resultSet = statement.executeQuery();

			int locationsExtracted = 0; //# of FMCLocationData objects actually found in the database
			if(resultSet.next()){   //if current row is valid, then extract
				locationsExtracted = resultSet.getInt("COUNT(PhoneNumber)");
			}
			else{

			}



			/***********Query 2 from NON_LOCATION_FIELDS: Extract data************************************************************************/
			query = "SELECT * FROM non_location_fields WHERE PhoneNumber = ? and DateInMilliseconds between ? and ? ORDER BY DateInMilliseconds desc";
			statement = connection.prepareStatement(query);
			statement.setString(1, phoneNumber);
			statement.setLong(2, beginTime);
			statement.setLong(3, endTime);

			//printout so we can see what's going on
			System.out.println("Extracting rows from non_location_fields during time period: "
					+ dateFormat.format(begin) + " - " + dateFormat.format(end));

			//execute query1 and extract data elements. Should only return 1 row
			//Create variables here so that they can be used at the very end when we put stuff into FMCLocationData objects ==> then into the array
			resultSet = statement.executeQuery();
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
			statement = connection.prepareStatement(query);
			statement.setString(1, phoneNumber);
			statement.setLong(2, beginTime);
			statement.setLong(3, endTime);

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
			resultSet = statement.executeQuery();
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


	public synchronized boolean 	insertKeepAliveRecord(FMCLocationData locationData)
	{

		/**************************************************SQL Database integration HERE************************************
		 * SQL Database integration HERE
		 * Putting raw string --> FMCLocationData object is helpful, b/c for SQL integration, now all we have 
		 * to do is call getter methods
		 * 
		 *  How the 2 database tables are related to each other:
		 *  1) Table 1: stores non-location fields 
		 *  2) Table 2: stores location fields
		 *  Each entry in table 1 is connected with another unique entry in table 2 by
		 *  	PhoneNumber and DateInMilliseconds ( you can see it as like a "timestamp")
		 *  
		 *  Only 2 options: Add(insert) or delete
		 *  Since each entry has a corresponding part in the other table, 
		 *  	-when one is added ==> identical one is added in other table
		 *  	-when one is deleted ==> identical one is deleted in other table
		 */
		//STEP 4: Execute a query
		/* First test to see if the phoneNumber is in the database using a SELECT statement
		 * If it's not ==> add it and it's fields into the database using INSERT statement
		 * http://stackoverflow.com/questions/16099382/java-mysql-check-if-value-exists-in-database
		 * http://stackoverflow.com/questions/30860186/check-if-value-exists-in-mysql-db-in-java
		 * Use PreparedStatement instead of Statement to prevent SQL injections
		 */
		//                    query = "SELECT ChargingMode from non_location_fields where PhoneNumber = ?";	//test to see if the phoneNumber is in the database using a SELECT statement
		//                    MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
		//                    MainOfficeServer.statement.setString(1, locationData.getPhoneNumber());

		try{
			//                       	ResultSet resultSet = MainOfficeServer.statement.executeQuery();
			//                       	System.out.println("Executing test query for phoneNumber: " + locationData.getPhoneNumber());

			//statements written here so that we can reuse code
			int rowsAffected;
			String mobileData, gps, network;
			mobileData = (locationData.isMobileDataON()) ? "ON" : "OFF";
			gps = (locationData.isGPSON()) ? "ON" : "OFF";
			network = (locationData.isNetworkON()) ? "ON" : "OFF";

			//if the current row is not valid ==> phoneNumber isn't in there ==> insert into database
			//                   		if(!resultSet.next()){
			/******1) Insert row into non_location_fields******/
			String query = "INSERT INTO non_location_fields values(?,?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, locationData.getPhoneNumber());
			statement.setLong(2, locationData.getTimeReceivedInMillis());
			statement.setString(3, locationData.getChargingMethod());
			statement.setString(4, mobileData);
			statement.setString(5, gps);
			statement.setString(6, network);
			statement.setString(7, locationData.getRawWifiMessage());

			//printout so we can see what's going on
			/*
			getOutBufferedWriter().write("phoneNumber: " + locationData.getPhoneNumber() + 
					", timeStamp: " + locationData.getTimeReceived() + 
					", chargingMethod: " + locationData.getChargingMethod() + 
					", mobileData: " + mobileData + ", gps: " + gps + 
					", network: " + network + ", wifi: " + locationData.getRawWifiMessage());
			getOutBufferedWriter().newLine();
			 */
			try{
				rowsAffected = statement.executeUpdate();
			}
			catch(SQLException se1){
				se1.printStackTrace();
			}


			/******2) Insert row into location_fields******/
			try{
				System.out.println("Executing insert query for location_fields phoneNumber: " + locationData.getPhoneNumber());

				//statements written here so that we can reuse code
				query = "INSERT INTO location_fields values(?,?,?,?,?,?,?)";
				statement = connection.prepareStatement(query);
				statement.setString(1, locationData.getPhoneNumber());			//phone number
				statement.setString(2, locationData.getTimeReceived());		//time received in Date format
				statement.setLong(3, locationData.getTimeReceivedInMillis());	//time received in Millis
				statement.setString(4, locationData.getBestLocation().getOriginalLocationString());		//best location
				statement.setString(5, locationData.getFMPLocation(0).getOriginalLocationString());	//loc 1 aka gps
				statement.setString(6, locationData.getFMPLocation(1).getOriginalLocationString());	//loc 2 aka network
				statement.setInt(7, locationData.getBatteryLevel());	//battery level
				//printout so we can see what's going on
				/*
				getOutBufferedWriter().write("phoneNumber: " + locationData.getPhoneNumber() + 
						", TimeRecieved: " + locationData.getTimeReceived() + 
						", TimeRecieved (millis): " + locationData.getTimeReceivedInMillis() + 
						", BestLocation: " + locationData.getBestLocation().getOriginalLocationString() + 
						", Location_1: " + locationData.getFMPLocation(0).getOriginalLocationString() +
						", Location_2: " + locationData.getFMPLocation(1).getOriginalLocationString() +
						", BatteryLevel: " + locationData.getBatteryLevel());
				getOutBufferedWriter().newLine();
				 */
				try{
					rowsAffected = statement.executeUpdate();
					return true;
				}
				catch(SQLException se1){
					se1.printStackTrace();
				}



			}
			catch(SQLException se){
				se.printStackTrace();
			}

			//                   		}
			//                   		//Old update statement that was deprecated when table structure changed
			//                   		//if the current row is valid ==> phoneNumber exists in there ==> simply update
			//                   		else{
			//                   			query = "UPDATE non_location_fields SET ChargingMode = ?, MobileData = ?, GPS = ?, MobileNetwork = ?, Wifi = ? WHERE PhoneNumber = ?";
			//                      		MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
			//                      		MainOfficeServer.statement.setString(6, locationData.getPhoneNumber());
			//                            MainOfficeServer.statement.setString(1, locationData.getChargingMethod());
			//                            MainOfficeServer.statement.setString(2, mobileData);
			//                            MainOfficeServer.statement.setString(3, gps);
			//                            MainOfficeServer.statement.setString(4, network);
			//                            MainOfficeServer.statement.setString(5, locationData.getRawWifiMessage());
			//                            //printout so we can see what's going on
			//                            getOutBufferedWriter().write("phoneNumber: " + locationData.getPhoneNumber() + 
			//                       				", chargingMethod: " + locationData.getChargingMethod() + 
			//                       				", mobileData: " + mobileData + ", gps: " + gps + 
			//                       				", network: " + network + ", wifi: " + locationData.getRawWifiMessage());
			//                            getOutBufferedWriter().newLine();
			//                            try{
			//                            	rowsAffected = MainOfficeServer.statement.executeUpdate();
			//                            }
			//                            catch(SQLException se1){
			//                            	se1.printStackTrace();
			//                            }
			//                   		}

		}
		catch(SQLException se){
			se.printStackTrace();
		}
		//                   	resultSet.close();

		//                   	//code below was copied and moved above 
		//                   	try{
		//                       	System.out.println("Executing insert query for location_fields phoneNumber: " + locationData.getPhoneNumber());
		//                       	
		//                   		//statements written here so that we can reuse code
		//                       	int rowsAffected;
		//                  		query = "INSERT INTO location_fields values(?,?,?,?,?,?,?)";
		//                  		MainOfficeServer.statement = MainOfficeServer.connection.prepareStatement(query);
		//                        MainOfficeServer.statement.setString(1, locationData.getPhoneNumber());			//phone number
		//                        MainOfficeServer.statement.setString(2, locationData.getTimeReceived());		//time received in Date format
		//                        MainOfficeServer.statement.setLong(3, locationData.getTimeReceivedInMillis());	//time received in Millis
		//                        MainOfficeServer.statement.setString(4, locationData.getBestLocation().getOriginalLocationString());		//best location
		//                        MainOfficeServer.statement.setString(5, locationData.getFMPLocation(0).getOriginalLocationString());	//loc 1 aka gps
		//                        MainOfficeServer.statement.setString(6, locationData.getFMPLocation(1).getOriginalLocationString());	//loc 2 aka network
		//                        MainOfficeServer.statement.setInt(7, locationData.getBatteryLevel());	//battery level
		//                        //printout so we can see what's going on
		//                   		getOutBufferedWriter().write("phoneNumber: " + locationData.getPhoneNumber() + 
		//                   				", TimeRecieved: " + locationData.getTimeReceived() + 
		//                   				", TimeRecieved (millis): " + locationData.getTimeReceivedInMillis() + 
		//                   				", BestLocation: " + locationData.getBestLocation().getOriginalLocationString() + 
		//                   				", Location_1: " + locationData.getFMPLocation(0).getOriginalLocationString() +
		//                   				", Location_2: " + locationData.getFMPLocation(0).getOriginalLocationString() +
		//                   				", BatteryLevel: " + locationData.getBatteryLevel());
		//                        getOutBufferedWriter().newLine();
		//                         try{
		//                          	rowsAffected = MainOfficeServer.statement.executeUpdate();
		//                         }
		//                         catch(SQLException se1){
		//                          	se1.printStackTrace();
		//                         }
		//                          
		//                   		
		//                   		
		//                   	}
		//                   	catch(SQLException se){
		//                   		se.printStackTrace();
		//                   	}

		//                    MainOfficeServer.statement.executeQuery(query);
		return false;

	}



}
