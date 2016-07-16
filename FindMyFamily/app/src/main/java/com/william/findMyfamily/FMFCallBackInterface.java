package com.william.findMyfamily;

/**
 * Created by William on 7/13/2016.
 * This interface is used as a way for FMFOfficeComm to call a function in FMFMainScreen
 */
public interface FMFCallBackInterface {

     // These functions should be implemented in FMFMainScreen,
     // These functions are called from FMFOfficeComm

    void postSimpleDialogBox(String asyncresult);
    void displayUserOnMap(String targetPhone, String locationInfo);
}
