package com.william.findMyfamily;

import com.william.fmfCommon.FMCMessage;
import com.william.fmfCommon.FMCRawLocation;
import com.william.fmfCommon.FMCLocationData;
import com.william.fmAndroidCommon.FMAOfficeCommunication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/*
 FMFMainScreen: Main Screen
 Note:  It implements FMFCallBackInterface such that FMFOfficeComm can call it's own method to
 pass received data back to FMFMainScreen
 */
public class FMFMainScreen extends FragmentActivity  implements FMFCallBackInterface{

    static Context mainContext;
    static String snippetText;
    static int mapDisplayMode = GoogleMap.MAP_TYPE_NORMAL;
    static boolean mapTrafficMode = false;
    private Vibrator vibrator;

    private GoogleMap googleMap;

    private AlertDialog listDialog;
    private AlertDialog alertDialog;

    private FMFOfficeComm officeConnection = new FMFOfficeComm(this);
    /*
     * Initializations
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.FMFMainScreen = this;
        Tools.myOfficeConnection = officeConnection;

        setContentView(R.layout.activity_fmfmain_screen);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Tools.mapUserList = Tools.readFromPropertiesfile(this.getApplicationContext());
        if (!Tools.readingUserFromFileOk) {
            Toast.makeText(this, "Cannot read from config file", Toast.LENGTH_LONG).show();
        }
        System.out.println("FMFMainScreen onCreate");
        System.out.println("FMFMainScreen initialize, size:" + Tools.mapUserList.size());

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Tools.map = fm.getMap();

        //fm.getMapAsync( this);

        Tools.map.setOnMarkerClickListener(
                new OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
//                      Toast.makeText(mainContext, "OnMarkerClickListener", Toast.LENGTH_LONG).show();
                        //  open up dialog
                        String thePhoneNumber = marker.getTitle();
                        FMFUserData searchData = new FMFUserData(thePhoneNumber);
                        int foundIndex = Tools.mapUserList.indexOf(searchData);
                        if (foundIndex != -1) {
                            performMarkerOpen(Tools.mapUserList.get(foundIndex));
                            return true;
                        }
                        return false;
                    }
                }
        );

        Button findButton = (Button) findViewById(R.id.findButton);

        findButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                vibrator.vibrate(50);
                findButtonFunction();
            }
        });

        Button usersButton = (Button) findViewById(R.id.usersButton);

        usersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                vibrator.vibrate(50);
                usersButtonFunction();

            }
        });
        Button mapModeButton = (Button) findViewById(R.id.mapModeButton);

        mapModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                vibrator.vibrate(50);
                mapModeButtonFunction();
            }
        });

        Button setupButton = (Button) findViewById(R.id.setupButton);

        setupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                vibrator.vibrate(50);
                toolsButtonFunction();
            }
        });

        mainContext = this.getBaseContext();

    }

    /***********************************
     * Call backs of the Main Screen
     **********************************/

    protected void onStart() {
        super.onStart();
        System.out.println("On onStart");
    }

    protected void onRestart() {
        super.onRestart();
        System.out.println("On onRestart");

        if (Tools.checkForUpdates()) {
            Tools.map.clear();
            processAllUsers(true);
        }
    }

    protected void onResume() {
        super.onResume();
        System.out.println("On onResume.");

        if (Tools.checkForUpdates()) {
            processAllUsers(false);
        }
    }

    protected void onPause() {
        super.onPause();
        System.out.println("On onPause");
    }

    protected void onStop() {
        super.onStop();
        System.out.println("On onStop");
    }

    protected void onDestroy() {
        super.onDestroy();
        System.out.println("On onDestroy");
        // Save users to file
        Tools.saveCurrentMapUsersToPropertiesfile(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fmfmain_screen, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String ares = data.getStringExtra("a");
        System.out.println("On onActivityResult a=" + ares);
        /*
        switch(requestCode) {
        case (0) : {
            System.out.println("onActivityResult:" + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                String fileName = data.getStringExtra("a");
                String filePath = data.getStringExtra("filePath");
                System.out.println("got file name:" + filePath+","+fileName);
                */
    }


    /***********************************
     * Map Display Functions
     ************************************/
    public void drawUserOnMap(FMFUserData mapData, boolean animationFlag) {
        drawUserOnMap(mapData, -1, animationFlag);
    }

    public void drawUserOnMap(FMFUserData mapData, int locationChoice, boolean animationFlag) {
        LatLng currentLocation = new LatLng(32.9401, -97.0094);
        String snippetText = "";
        String markerTitle = "";
        int accuracy = 0;

        Bitmap photo;
        FMCLocationData userData = mapData.getFmpLocationData();
        int cirleColor = mapData.getColor();
        if (locationChoice >= 0) {
            currentLocation = new LatLng(userData.getFMPLocation(locationChoice).getLatitude(),
                    userData.getFMPLocation(locationChoice).getLongitude());
            accuracy = (int) userData.getFMPLocation(locationChoice).getAccuracy();
            System.out.println("Using SMS info provide is :" + userData.getFMPLocation(locationChoice).getProvider() +
                    ", Lat: " + userData.getFMPLocation(locationChoice).getLatitude() + ", Long: " + userData.getFMPLocation(locationChoice).getLongitude());
        } else {
            if (userData.getBestLocation() != null) {
                currentLocation = new LatLng(userData.getBestLocation().getLatitude(), userData.getBestLocation().getLongitude());
                accuracy = (int) userData.getBestLocation().getAccuracy();
                System.out.println("Using SMS info provide is :" + userData.getBestLocation().getProvider() +
                        ", Lat: " + userData.getBestLocation().getLatitude() + ", Long: " + userData.getBestLocation().getLongitude());
            } else {
                Toast.makeText(this, "No Location available for " + userData.getPhoneNumber(), Toast.LENGTH_LONG).show();
            }
        }
        markerTitle = userData.getPhoneNumber();


        snippetText = "Battery Level:" + userData.getBatteryLevel() + "% ";
        if (userData.getChargingMethod() != null)
            snippetText += "(" + userData.getChargingMethod() + ")";

        Bitmap bmap = Tools.getContactPhoto(this, userData.getPhoneNumber());
        Bitmap smallerBitmap = Bitmap.createScaledBitmap(bmap, 100, 100, true);
        photo = Tools.getCircularBitmap(smallerBitmap);

        if (photo == null) {
            photo = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
        }

        Marker currentMarker = Tools.map.addMarker(new MarkerOptions().position(currentLocation)
                .title(markerTitle)
                .snippet(snippetText)
                .icon(BitmapDescriptorFactory.fromBitmap(photo)));

        mapData.setMarker(currentMarker);

        if (animationFlag) {
            Tools.map.setMapType(mapDisplayMode);
            Tools.map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

            // Zoom in, animating the camera.
            Tools.map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
        }

        Circle circle = Tools.map.addCircle(new CircleOptions()
                .center(currentLocation)
                .radius(accuracy)
                .strokeWidth(0)
                .strokeColor(cirleColor)
                .fillColor(cirleColor));

        mapData.setCircle(circle);
        mapData.setMapState(FMFUserData.MAP_STATE_DISPLAYED_IN_MAP);

        System.out.println("Finished drawUserOnMap " + userData.getPhoneNumber());
    }

    public void processAllUsers(boolean forceRedraw) {
        FMFUserData fLatestMapData = null;

        System.out.println("processAllUsers  Draw existing locations:" + Tools.mapUserList.size());


        for (int i = 0; i < Tools.mapUserList.size(); i++) {
            FMFUserData fMapData = Tools.mapUserList.get(i);
            if (forceRedraw || fMapData.getMapState() == FMFUserData.MAP_STATE_PENDING_DISPLAY) {
                fMapData.setColor(Tools.circleColor[i % 6]);
                if (Tools.latestActivityPH.length() != 0 &&
                        fMapData.getFmpLocationData().getPhoneNumber().equals(Tools.latestActivityPH)) {
                    fLatestMapData = fMapData;
                } else {
//                    drawUserOnMap(fMapData, (fMapData.getMapState() == FMFUserData.MAP_STATE_PENDING_DISPLAY || (i == Tools.mapUserList.size() - 1)) ? true : false);
                    drawUserOnMap(fMapData, false);
                }
            }
        }
        if (fLatestMapData != null) {
            drawUserOnMap(fLatestMapData, true);
            Tools.latestActivityPH = "";
        }
    }

    /*****************************************************************
     * Button Functions below
     */


    // Quick Find Button and Function
    public void findButtonFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FMFMainScreen.this);
        LayoutInflater inflater = this.getLayoutInflater();

        View diagView = inflater.inflate(R.layout.find_maindialog, null);

        ListView userListView = (ListView) diagView.findViewById(R.id.listViewFind);
        UserItemListAdapter adapter = new UserItemListAdapter(diagView.getContext());
        userListView.setAdapter(adapter);
        //       userListView.setBackgroundColor(Color.rgb(0,0,0));

//        builder.setView(diagView);
        builder.setView(diagView)
                .setNegativeButton(R.string.C_DISMISS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);

                    }
                })
                .setPositiveButton(R.string.C_FINDALLUSERS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);

                        // WL need to change this
                        // disabled it for now  because I screwed up so many times

                        //findUsersOperation(true,0);

                    }
                });


        final AlertDialog alert = builder.create();
        alert.show();

        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("findButtonFunction short click " + position);
                vibrator.vibrate(50);
                alert.dismiss();
                //findUsersOperation(false, position);
                findUsersOperationWithLAN(position);
            }
        });

        userListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final int fposition = position;

                System.out.println("long click" + position);
//                  commandOperation(position);
                final FMFUserData fMapData = Tools.mapUserList.get(fposition);

                //findUsersOperationWithLAN(position);

                // Confirm
                AlertDialog.Builder builder2 = new AlertDialog.Builder(FMFMainScreen.this);

                builder2.setMessage("Confirm sending SMS to user " + fMapData.getUserName() + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                findUsersOperation(false, fposition);
                                return;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialog = builder2.create();
                alertDialog.show();
                alert.dismiss();
                return true;
            }
        });
    }

    /***********************************
     * User Button Functions
     **********************************/
    public void usersButtonFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FMFMainScreen.this);
        LayoutInflater inflater = this.getLayoutInflater();

        View diagView = inflater.inflate(R.layout.user_maindialog, null);


        builder.setView(diagView)
                .setNegativeButton(R.string.C_Add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        addUserFunction();
                        vibrator.vibrate(50);

                    }
                })
                .setNeutralButton(R.string.C_DISPLAYALL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);

                        processAllUsers(true);
                    }
                })
                .setPositiveButton(R.string.C_CLEARMAP, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);
                        // clearAllUsers();
                        for (int i = 0; i < Tools.mapUserList.size(); i++) {
                            FMFUserData FMFUserData = Tools.mapUserList.get(i);
                            if (FMFUserData.getMarker() != null)
                                FMFUserData.getMarker().remove();
                            if (FMFUserData.getCircle() != null)
                                FMFUserData.getCircle().remove();

                        }
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();

        // User List
        ListView userListView = (ListView) diagView.findViewById(R.id.listViewUser);
        UserItemListAdapter adapter = new UserItemListAdapter(diagView.getContext());
        userListView.setAdapter(adapter);

        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("short click " + position);
                vibrator.vibrate(50);
                alert.dismiss();
                usersSelectionFunction(position);
            }
        });

    }


    /***********************************
     * Map Option Functions
     **********************************/
    public void mapModeButtonFunction() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        View mapDialogView = inflater.inflate(R.layout.mapoption_dialog, null);
        final RadioGroup mapOptionId = (RadioGroup) mapDialogView.findViewById(R.id.mapOptions);
        final CheckBox trafficCB = (CheckBox) mapDialogView.findViewById(R.id.trafficcb);

        mapOptionId.check(mapDisplayMode == GoogleMap.MAP_TYPE_NORMAL ? R.id.mapRadio : R.id.satelliteRadio);
        trafficCB.setChecked(mapTrafficMode);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout


        builder.setView(mapDialogView)
                // Add action buttons
                .setPositiveButton(R.string.C_OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send command(s)
                        int selectedMapMode = mapOptionId.getCheckedRadioButtonId();
                        mapTrafficMode = trafficCB.isChecked();

                        if (selectedMapMode == R.id.mapRadio)
                            mapDisplayMode = GoogleMap.MAP_TYPE_NORMAL;
                        else
                            mapDisplayMode = GoogleMap.MAP_TYPE_SATELLITE;

                        Tools.map.setMapType(mapDisplayMode);

                        Tools.map.setTrafficEnabled(mapTrafficMode);

                    }
                })
                .setNegativeButton(R.string.C_Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    /***********************************
     * Tools Functions
     **********************************/
    public void toolsButtonFunction() {

        CharSequence[] items = {"Server Status and Summary", "Server Setup",
                "Target List", "Display Log", "Settings"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tools Menu");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                listDialog.dismiss();
                switch (item) {
                    case 0: // Check Server Status
                        performCheckServerStatus();

                        break;

                    case 1: //
                        performServerSetup();
                        break;

                    case 2: //
                        performListTargets();
                        break;

                    case 3: //
                        //performServerCleanup();
                        performDisplayLog();
                        break;

                    case 4: //
                        //performTrackDataSummary();
                        performSettings();
                        break;
                }

            }
        });

        listDialog = builder.create();
        listDialog.show();

    }

    // Config Function
    public void configFunction() {
//        Intent ed = new Intent(ControlScreen.this, SnapShotScreen.class);
//        startActivityForResult(ed, 0);
    }


    /*************************************
     * Supporting function after user click on an image
     *
     * @param
     **************************************/

    // Click on Marker on the Map
    public void performMarkerOpen(FMFUserData FMFUserData) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        final View markerDialogView = inflater.inflate(R.layout.marker_mainsdialog, null);

        final int itemNumber = Tools.mapUserList.indexOf(FMFUserData);
        builder.setView(markerDialogView)
                // Add action buttons
                .setNegativeButton(R.string.C_DISMISS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                })
                .setPositiveButton(R.string.C_FINDUSER, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Find User FMFUserData
                        vibrator.vibrate(50);
                        //findUsersOperation(false, itemNumber);
                        findUsersOperationWithLAN(itemNumber);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        final AlertDialog fAlert = alert;


        TextView userNamePhoneTV = (TextView) markerDialogView.findViewById(R.id.userNamePhoneNum);
        TextView dateStringTV = (TextView) markerDialogView.findViewById(R.id.dateString);
        ProgressBar pBar = (ProgressBar) markerDialogView.findViewById(R.id.pBar);
        TextView pBarTV = (TextView) markerDialogView.findViewById(R.id.pBarTV);

        TextView gpsWifiStatesTV = (TextView) markerDialogView.findViewById(R.id.gpsWifiStates);
        Button commandButton = (Button) markerDialogView.findViewById(R.id.commandButton);
        final FMFUserData fFMFUserData = FMFUserData;
        commandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                vibrator.vibrate(50);
                commandOperation(Tools.mapUserList.indexOf(fFMFUserData));
            }
        });

        TextView wifiConnectionTV = (TextView) markerDialogView.findViewById(R.id.wifiConnection);
        Button location1Button = null;
        Button location2Button = null;

        FMCLocationData fmpLocationData = FMFUserData.getFmpLocationData();
        userNamePhoneTV.setText(FMFUserData.getUserName() + "  " + FMFUserData.getFmpLocationData().getPhoneNumber());
        String uDate = FMFUserData.getFmpLocationData().getUpdateDateString();
        if (uDate == null || uDate.length() == 0)
            dateStringTV.setText("No Date Information.");
        else
            dateStringTV.setText("Data received on:" + uDate);
        int batteryLevel = FMFUserData.getFmpLocationData().getBatteryLevel();

        if (batteryLevel > 75) {
            pBar.setProgressDrawable(mainContext.getResources().getDrawable(R.drawable.green_progressbar));
        } else if ((batteryLevel >= 40)) {
            pBar.setProgressDrawable(mainContext.getResources().getDrawable(R.drawable.yellow_progressbar));
        } else
            pBar.setProgressDrawable(mainContext.getResources().getDrawable(R.drawable.red_progressbar));

        pBar.setProgress(batteryLevel);

        String batteryString = "";
        if (fmpLocationData.getChargingMethod() != null && fmpLocationData.getChargingMethod().length() > 0) {
            batteryString += "Battery Charging (" + fmpLocationData.getChargingMethod() + ")  ";
        }
        batteryString += new String(fmpLocationData.getBatteryLevel() + "%");
        pBarTV.setText(batteryString);

        gpsWifiStatesTV.setText("GPS: " + (fmpLocationData.isGPSON() ? "ON" : "OFF") +
                "   Mobile Data:" + (fmpLocationData.isMobileDataON() ? "ON" : "OFF") +
                "\nScreen On?:" + (fmpLocationData.isInteractiveON() ? "YES" : "NO") +
                "\nWifi:" + fmpLocationData.getWifiDetailStatus());


        // commandButton

        String wifiString = "";
        if (fmpLocationData.getConnectedWifiSSID() != null && fmpLocationData.getConnectedWifiSSID().length() > 0) {
            wifiString = "SSID: " + fmpLocationData.getConnectedWifiSSID();
            if (!fmpLocationData.getWifiStatus().equals("ENC")) {
                wifiString = wifiString + " (Old)";
            }
        } else {
            if (fmpLocationData.getWifiAvailableSSIDs() != null &&
                    fmpLocationData.getWifiAvailableSSIDs().length > 0) {
                wifiString = "Available SSID: ";
                for (int i = 0; i < fmpLocationData.getWifiAvailableSSIDs().length; i++) {
                    wifiString += fmpLocationData.getWifiAvailableSSIDs()[i];
                    wifiString += (i == fmpLocationData.getWifiAvailableSSIDs().length - 1) ? "" : ",";
                }
            }
        }
        wifiConnectionTV.setText(wifiString);

        final FMFUserData fmpFUserData = FMFUserData;
        if (fmpLocationData.getFMPLocation(FMCLocationData.LOCATION_GPS) != null) {
            location1Button = (Button) markerDialogView.findViewById(R.id.location1Button);
            FMCRawLocation myLocation = fmpLocationData.getFMPLocation(FMCLocationData.LOCATION_GPS);
            String buttonText = myLocation.getProvider();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            cal.setTimeInMillis(myLocation.getTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            buttonText += "   " + dateFormat.format(cal.getTime());
            buttonText += "\nLocation: " + myLocation.getLatitude() + ", " + myLocation.getLongitude();
            buttonText += "\nAccuracy: " + myLocation.getAccuracy();
            if (myLocation.getSpeed() != 0.0) {
                buttonText += "\nDirection: " + bearingToDirection(myLocation.getBearing());
                buttonText += "  Speed: " + myLocation.getSpeed();
            } else {
                buttonText += ",  No Motion.";
            }

            location1Button.setText(buttonText);

            location1Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    vibrator.vibrate(50);
                    //Display this location on map
                    System.out.println("LOCATION_GPS button pressed");
                    fmpFUserData.removeFromMap();
                    drawUserOnMap(fmpFUserData, FMCLocationData.LOCATION_GPS, true);
                    fAlert.dismiss();
                }
            });
        }

        if (fmpLocationData.getFMPLocation(FMCLocationData.LOCATION_NETWORK) != null) {
            Button networkButton = null;
            if (location1Button == null) {
                // no gps, use location1button
                location1Button = (Button) markerDialogView.findViewById(R.id.location1Button);
                networkButton = location1Button;
            } else  // location1 used
            {
                location2Button = (Button) markerDialogView.findViewById(R.id.location2Button);
                networkButton = location2Button;
            }
            FMCRawLocation myLocation = fmpLocationData.getFMPLocation(FMCLocationData.LOCATION_NETWORK);
            String buttonText = myLocation.getProvider();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            cal.setTimeInMillis(myLocation.getTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            buttonText += "   " + dateFormat.format(cal.getTime());
            buttonText += "\nLocation: " + myLocation.getLatitude() + ", " + myLocation.getLongitude();
            buttonText += "\nAccuracy: " + myLocation.getAccuracy();
            networkButton.setText(buttonText);


            networkButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    System.out.println("LOCATION_NETWORK button pressed");
                    vibrator.vibrate(50);
                    fmpFUserData.removeFromMap();
                    drawUserOnMap(fmpFUserData, FMCLocationData.LOCATION_NETWORK, true);
                    fAlert.dismiss();
                }
            });
        }

        // Remove any unused button
        if (location1Button == null) {
            location1Button = (Button) markerDialogView.findViewById(R.id.location1Button);
            ViewGroup layout = (ViewGroup) location1Button.getParent();
            if (null != layout)
                layout.removeView(location1Button);
        }
        if (location2Button == null) {
            location2Button = (Button) markerDialogView.findViewById(R.id.location2Button);
            ViewGroup layout = (ViewGroup) location2Button.getParent();
            if (null != layout)
                layout.removeView(location2Button);
        }



        /*
        if(null!=layout) //for safety only  as you are doing onClick
          layout.removeView(location2Button);
          */

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        /*
        final int itemNumber = Tools.mapUserList.indexOf(FMFUserData);

        builder.setView(markerDialogView)
        // Add action buttons
        .setNegativeButton(R.string.C_DISMISS, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        })
        .setPositiveButton(R.string.C_FINDUSER, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Find User FMFUserData
                vibrator.vibrate(50);
                //findUsersOperation(false, itemNumber);
                findUsersOperationWithLAN(itemNumber);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
*/
    }

    // Add User Button

    public void addUserFunction() {
        // Bring up a dialog to add phone number and user name
        final String[] records = Tools.getPhoneContacts(this);
        if (records == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

//      builder.setTitle(fMapData.getUserName());

        builder.setItems(records, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                listDialog.dismiss();
                String[] tokens = records[item].split("\t");
                if (tokens != null && tokens[1] != null) {
                    Tools.addNewFMFUserData(tokens[0], tokens[1]);
                    Tools.saveCurrentMapUsersToPropertiesfile(mainContext);
                }
            }
        });

        listDialog = builder.create();
        listDialog.show();


    }

    public void usersSelectionFunction(int itemIndex) {
//      CharSequence[] items = {"Display my location on Map",
//              "Latest Location Information", "Display Track Result",  "Find me", "Send Phone Commands",
//              "Remove me from Map", "Delete", "Move Up", "Move Down"};
        CharSequence[] items = {"Display my location on Map",
                "Latest Location Information", "Find me (LAN)", "Display Last 10 locations", "FMP log",
                "Remove me from Map", "Delete", "Move Up", "Move Down",
                "Find me (SMS)", "Send Phone Commands", "Display Track Result"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final FMFUserData fMapData = Tools.mapUserList.get(itemIndex);
        final int itemFIndex = itemIndex;
        builder.setTitle(fMapData.getUserName());

        items[0] = "Display " + fMapData.getUserName() + " on Map";
        items[1] = fMapData.getUserName() + "'s latest location information";
        items[2] = "Find " + fMapData.getUserName() + " (LAN)";
        items[5] = "Remove " + fMapData.getUserName() + " from Map";
        items[9] = "Find " + fMapData.getUserName() + " (SMS)";


        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                listDialog.dismiss();
                switch (item) {
                    case 0:  // Display my location on Map
                        fMapData.removeFromMap();
                        fMapData.setColor(Tools.circleColor[itemFIndex % 6]);
                        drawUserOnMap(fMapData, true);
                        break;

                    case 1:  // Latest Location Information
                        performMarkerOpen(fMapData);
                        break;


                    case 2:  // Find me (LAN)
                        findUsersOperationWithLAN(itemFIndex);
                        //findUsersOperation(itemFIndex);
                        break;

                    case 3:  // Display Last 10 locations
                        // displayLocationHistoryOperation(itemFIndex);
                        break;

                    case 4:  // FMP log
                        // fMPLogOperation(itemFIndex);
                        break;


                    case 5:  // Remove From Map
                        fMapData.removeFromMap();
                        break;

                    case 6:  // delete
                        performItemDelete(fMapData);
                        break;

                    case 7:  // Move Up
                        if (item != 0) {
                            Tools.mapUserList.add(itemFIndex - 1, fMapData);
                            Tools.mapUserList.remove(itemFIndex + 1); // old position
                            Tools.saveCurrentMapUsersToPropertiesfile(mainContext);
                        }
                        break;
                    case 8:  // Move Down

                        if (item != Tools.mapUserList.size() - 1) {
                            Tools.mapUserList.remove(itemFIndex);
                            Tools.mapUserList.add(itemFIndex + 1, fMapData); // new pos
                            Tools.saveCurrentMapUsersToPropertiesfile(mainContext);
                        }
                        break;

                    case 9:  // Find me (SMS)
                        findUsersOperation(itemFIndex);
                        break;
//
                    case 10:  // Commands
                        commandOperation(itemFIndex);
                        break;

                    case 11:  //  Display track result.  Not used anymore
                        displayTrackResult(itemFIndex);
                        break;

                }
            }
        });

        listDialog = builder.create();
        listDialog.show();


    }


    public void findUsersOperation(boolean toAll, int itemNumber) {
        if (toAll) {
            for (int i = 0; i < Tools.mapUserList.size(); i++) {
                Tools.sendFindCommand(i);
            }
            Toast.makeText(this, "Sent Request to all users. Result will be available soon..", Toast.LENGTH_SHORT).show();
        } else {
            Tools.sendFindCommand(itemNumber);
            Toast.makeText(this, "Request sent to " + Tools.mapUserList.get(itemNumber).getUserName() + ". Result will be available soon..", Toast.LENGTH_SHORT).show();

        }
    }

    // This function sends SMS text as commands to target phone.
    // Although still functional, it is not used anymore
    public void commandOperation(int itemNumber) {
        // Commands
        // GPS on / off
        // Wifi On / Off
        // Mobile Data on / off

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        View commandDialogView = inflater.inflate(R.layout.command_dialog, null);
        final RadioGroup wifiGroupId = (RadioGroup) commandDialogView.findViewById(R.id.wifi);
        final RadioGroup gpsGroupId = (RadioGroup) commandDialogView.findViewById(R.id.gps);
        final RadioGroup mdataGroupId = (RadioGroup) commandDialogView.findViewById(R.id.mdata);
        final RadioGroup trackGroupId = (RadioGroup) commandDialogView.findViewById(R.id.track);

        final boolean currentWifiStatus = Tools.mapUserList.get(itemNumber).getFmpLocationData().getWifiStatus().equalsIgnoreCase("ENC");
        final boolean currentGPSStatus = Tools.mapUserList.get(itemNumber).getFmpLocationData().isGPSON();
        final boolean currentMDataStatus = Tools.mapUserList.get(itemNumber).getFmpLocationData().isMobileDataON();
        final boolean currentTrackStatus = Tools.mapUserList.get(itemNumber).getFmpLocationData().isTrackON();

        wifiGroupId.check(currentWifiStatus ? R.id.radioWifiON : R.id.radioWifiOFF);
        gpsGroupId.check(currentGPSStatus ? R.id.radioGPSON : R.id.radioGPSOFF);
        mdataGroupId.check(currentMDataStatus ? R.id.radioMDATAON : R.id.radioMDATAOFF);
        trackGroupId.check(currentTrackStatus ? R.id.radioTRACKON : R.id.radioTRACKOFF);

        TextView userNameTV = (TextView) commandDialogView.findViewById(R.id.userName);
        userNameTV.setText(Tools.mapUserList.get(itemNumber).getUserName());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final int itemFNumber = itemNumber;

        builder.setView(commandDialogView)
                // Add action buttons
                .setPositiveButton(R.string.C_SendCommand, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send command(s)
                        int selectedWifiButton = wifiGroupId.getCheckedRadioButtonId();
                        int selectedGPSButton = gpsGroupId.getCheckedRadioButtonId();
                        int selectedMDATAButton = mdataGroupId.getCheckedRadioButtonId();
                        int selectedTrackButton = trackGroupId.getCheckedRadioButtonId();

                        if (selectedWifiButton == R.id.radioWifiON) {
                            if (!currentWifiStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_WIFI_COMMAND, Tools.FMP_ON);
                        } else {
                            if (currentWifiStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_WIFI_COMMAND, Tools.FMP_OFF);
                        }

                        if (selectedGPSButton == R.id.radioGPSON) {
                            if (!currentGPSStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_GPS_COMMAND, Tools.FMP_ON);
                        } else {
                            if (currentGPSStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_GPS_COMMAND, Tools.FMP_OFF);
                        }

                        if (selectedMDATAButton == R.id.radioMDATAON) {
                            if (!currentMDataStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_MDATA_COMMAND, Tools.FMP_ON);
                        } else {
                            if (currentMDataStatus)
                                Tools.sendFMPCommand(itemFNumber, Tools.FMP_MDATA_COMMAND, Tools.FMP_OFF);
                        }

                        if (selectedTrackButton == R.id.radioTRACKON) {
                            if (!currentTrackStatus) {
                                Tools.sendFMPTrackCommand(itemFNumber, Tools.FMP_ON);
                                Tools.mapUserList.get(itemFNumber).getFmpLocationData().setTrackON(true);
                            }
                        } else {
                            if (currentTrackStatus) {
                                Tools.sendFMPTrackCommand(itemFNumber, Tools.FMP_OFF);
                                Tools.mapUserList.get(itemFNumber).getFmpLocationData().setTrackON(false);
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.C_Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //                         LoginDialogFragment.this.getDialog().cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    // Find User
    public void findUsersOperation(int itemNumber) {
        if (itemNumber == 0) {
            for (int i = 0; i < Tools.mapUserList.size(); i++) {
                Tools.sendFindCommand(i);
            }
            Toast.makeText(this, "Sent Find Request to users. Result will be available soon..", Toast.LENGTH_SHORT).show();
        } else {
            Tools.sendFindCommand(itemNumber - 1);
            Toast.makeText(this, "Find Request sent. Result will be available soon..", Toast.LENGTH_SHORT).show();

        }
    }

    public void findUsersOperationWithLAN(int itemNumber) {
        Tools.sendFindCommandwithLAN(itemNumber);
        Toast.makeText(this, "Find Request sent through Internet. Result will be available soon..", Toast.LENGTH_SHORT).show();
    }

    // displayTrackResult
    public void displayTrackResult(int itemNumber) {
//      Tools.sendTrackCommand(itemNumber);
        Toast.makeText(this, "Track Result Request sent to " + Tools.mapUserList.get(itemNumber).getUserName() + ". Result will be available soon..", Toast.LENGTH_SHORT).show();
    }

    // Function From Tools Button
    public void performCheckServerStatus() {
        // Send a CheckServerStatus message to Server
        String message = "[" + FMCMessage.FMFOFFICE_CLIENTGETSERVERSTATUS + ":" + Tools.getMyPhoneNumber() + ":]";
        Tools.sendMessageThroughLAN(message);
    }

    // Method not used anymore  Can be deleted in the next major release
    public void performTrackUser() {

        //Toast.makeText(this, "Find Request sent through Internet. Result will be available soon..", Toast.LENGTH_SHORT).show();

        /* Old Implementation
        // To send a http command and expect a status,
        // then display a Toast
        new FMFTrackDataCommunication().execute(Tools.FMFCOMMANDSERVERSTATUS);
        Tools.setServerResponse(false);
        int timeout = 0;
        try
        {
            while (timeout < 8)
            {
                if (Tools.getServerResponse()){
                    if (Tools.getServerResult() == null)
                    {
                        Toast.makeText(this, "Server is down", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        if (Tools.getServerResult()[0].equals("999"))
                        {
                            Toast.makeText(this, "Server is down.  Error: " + Tools.getServerResult()[1], Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            displayTrackUserSummary(Tools.getServerResult()[1]);

                        }
                    }
                    break;
                }
                timeout++;
                Thread.sleep(1000);
            } // while

        }

        catch (Exception e)
        {}
        */
    }

    // Function From Tools Button
    public void performServerSetup() {
        // Have a dialog box showing existing server connection values

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        View commandDialogView = inflater.inflate(R.layout.serversetup_dialog, null);
        final EditText serverURL = (EditText) commandDialogView.findViewById(R.id.serverURLET);
        final EditText serverPort = (EditText) commandDialogView.findViewById(R.id.serverPortET);
        final EditText serverRoute = (EditText) commandDialogView.findViewById(R.id.serverRouteET);
        final EditText trackFreq = (EditText) commandDialogView.findViewById(R.id.trackFreqET);
        final FMFMainScreen myFMFMainScreen = this;

        serverURL.setText(Tools.fmfServerAddr);
        serverPort.setText(new String("" + Tools.fmfServerPort));
        serverRoute.setText(Tools.fmfTrackRoute);
        trackFreq.setText(new String("" + Tools.fmfTrackTimer));

        builder.setView(commandDialogView)
                // Add action buttons
                .setPositiveButton(R.string.C_Save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Save
                        Tools.fmfServerAddr = serverURL.getText().toString();
                        Tools.fmfServerPort = Integer.parseInt(serverPort.getText().toString());
                        Tools.fmfTrackRoute = serverRoute.getText().toString();
                        Tools.fmfTrackTimer = Integer.parseInt(trackFreq.getText().toString());
                        Tools.saveCurrentMapUsersToPropertiesfile(FMFMainScreen.this);
                        // Now restart the FMFOfficeComm
                        Tools.myOfficeConnection.stopCommunication();

                        Tools.myOfficeConnection = new FMFOfficeComm(myFMFMainScreen);

                    }
                })
                .setNegativeButton(R.string.C_Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Cancel
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    public void performListTargets() {
        Tools.sendMessageThroughLAN("[" + FMCMessage.FMFOFFICE_CLIENTLISTALL + "]");
    }

    public void performDisplayLog() {
        // Display Log
        Toast.makeText(this, "Display Log will be available soon..", Toast.LENGTH_SHORT).show();
    }

    public void performSettings() {
        // Perform settings like notification sound, etc.
        Toast.makeText(this, "Settings will be available soon..", Toast.LENGTH_SHORT).show();
    }

    // Method not used anymore.  Can be deleted in the next major release
    public void performTrackDataSummary() {
        // Display each phone number , no.of record and range

    }

    // Method not used anymore  Can be deleted in the next major release
    // Can be used as example
    public void displayTrackUserSummary(String json) {

        JSONStringer parser;
        Toast.makeText(this, "displayTrackUserSummary: Server is up. Response Message is:" + Tools.getServerResult()[0], Toast.LENGTH_LONG).show();


        JSONObject object;
        int iii = 1;
        System.out.println("json " + json);
        try {
            Tools.trackUserList.clear();
            while (true) {
                System.out.println("displayTrackUserSummary: item " + iii);
                int startIndex = json.indexOf("{");
                int endIndex = json.indexOf("}");
                if (endIndex == 0) {
                    System.out.println("json is now->" + json + "<-");
                }

                System.out.println("starti:" + startIndex + ", endindex:" + endIndex + ", total length" + json.length());

                if (startIndex < 0) break;

                String aRecord = json.substring(startIndex, endIndex + 1);
                System.out.println("aRecord:" + aRecord);


                JSONTokener jTokener = new JSONTokener(aRecord);
                object = (JSONObject) jTokener.nextValue();

                if (object == null) break;
                String phoneNumber = object.getString("phoneNumber");
                String locationDetail = object.getString("locationDetail");
                String count = object.getString("count");

                int mappos = Tools.getPositionOfMapUserList(phoneNumber);
                FMFUserData aUser = new FMFUserData(phoneNumber);
                System.out.println("Phone:" + phoneNumber);
                System.out.println("User Name:" + aUser.getUserName());
                System.out.println("Location:" + locationDetail);
                System.out.println("count:" + count);

                //locationDetail = "UN:"+Tools.mapUserList.get(mappos).getUserName()+"\n"+locationDetail;
                FMCLocationData locationData = new FMCLocationData();
                locationData.composeObjectFromMessage(locationDetail, phoneNumber);
                aUser.setFmpLocationData(locationData);
                aUser.setUserName(Tools.mapUserList.get(mappos).getUserName());
                aUser.setTrackCount(Integer.parseInt(count));
                aUser.setTrackBeginTime(object.getString("createdAt"));
                aUser.setTrackEndTime(object.getString("updatedAt"));

                Tools.trackUserList.add(aUser);

                iii++;

                json = json.substring(endIndex + 1);

            }    // while
        } catch (Exception e1) {

        }


        AlertDialog.Builder builder = new AlertDialog.Builder(FMFMainScreen.this);
        LayoutInflater inflater = this.getLayoutInflater();

        View diagView = inflater.inflate(R.layout.trackusersummary_maindialog, null);

        ListView userListView = (ListView) diagView.findViewById(R.id.listViewTrackUserSummary);
        TrackUserItemListAdapter adapter = new TrackUserItemListAdapter(diagView.getContext());
        userListView.setAdapter(adapter);
        ;
        builder.setView(diagView)
                .setNegativeButton(R.string.C_DISMISS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);

                    }
                })
                .setPositiveButton(R.string.C_TRACKSUMMARY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrator.vibrate(50);
                        //findUsersOperation(true,0);
                    }
                });


        final AlertDialog alert = builder.create();
        alert.show();

        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("displayTrackUserSummary short click " + position);
                vibrator.vibrate(50);
                alert.dismiss();
                // findUsersOperation(false,position);
            }
        });
/*
        userListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                System.out.println("long click"+ position);
                // commandOperation(position);
                alert.dismiss();
                return true;
            }

        });    */
    }


    // Delete a target user
    private void performItemDelete(FMFUserData fmData) {
        final String userName = fmData.getUserName();
        final FMFUserData ffmData = fmData;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Confirm delete user " + userName + " ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Tools.mapUserList.remove(ffmData);
                        Toast.makeText(FMFMainScreen.this, "User " + userName + " removed", Toast.LENGTH_LONG).show();
                        Tools.saveCurrentMapUsersToPropertiesfile(FMFMainScreen.this);
                        return;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //listdialog.dismiss();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    // The following are methods that have to be implemented for FMFCallBackInterface
    // *************************************************************

    public void postSimpleDialogBox(String asyncresult)
    {
        System.out.println("**** at postResult *****");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(asyncresult).setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void handleClientListAllResponse(String asyncresult)
    {
        System.out.println("**** at handleClientListAllResponse *****");

        // List Target, provide button to user menu
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(asyncresult).setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void handleGetServerStatusResponse(String asyncresult)
    {
        System.out.println("**** at handleGetServerStatusResponse *****");
        // Display Server up day, SQL onOff, n. of targets
        // Provide button to clean DB record for N days

        // Read first line. Split it


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(asyncresult).setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void  displayUserOnMap(String targetPhone, String locationInfo)
    {
        System.out.println("FMFMainScreen:displayUserOnMap:"+targetPhone);
        processAllUsers(false);
        System.out.println("FMFMainScreen:displayUserOnMap END:"+targetPhone);
    }
    // Helper functions below
    // *************************************************************

    private String bearingToDirection(float bearing) {
        String retString = "N";

        if (bearing < 0.0 || bearing > 360.0)
            retString = "UNK";
        else if (bearing < 22.5)
            retString = "N";
        else if (bearing < 67.5)
            retString = "NE";
        else if (bearing < 112.5)
            retString = "E";
        else if (bearing < 157.5)
            retString = "SE";
        else if (bearing < 202.5)
            retString = "S";
        else if (bearing < 247.5)
            retString = "SW";
        else if (bearing < 292.5)
            retString = "W";
        else if (bearing < 337.5)
            retString = "NW";

        retString = retString + " (" + bearing + ")";
        return retString;
    }


}
