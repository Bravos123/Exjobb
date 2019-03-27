package com.example.navigationtesting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class AskForPremission extends AppCompatActivity{
    private final int myRequestCode = 1;
    private Callback callback;
    private String currentPermissionName;

    public AskForPremission(AppCompatActivity activity, String permissionName){
        currentPermissionName = permissionName;
        callback = (Callback)activity;
        if(ContextCompat.checkSelfPermission(activity, permissionName) !=
                PackageManager.PERMISSION_GRANTED){
            //Permission not granted ask for it
            ActivityCompat.requestPermissions(activity, new String[]{permissionName},
                    myRequestCode);
        }else{
            //Permission already granted
            callback.callBack(currentPermissionName,true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode){
            case myRequestCode:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission granted
                    System.out.println("Permission granted");
                    callback.callBack(currentPermissionName,true);
                }else{
                    //Permission denied
                    System.out.println("Permission denied");
                    callback.callBack(currentPermissionName,false);
                }

            }
        }
    }

}
