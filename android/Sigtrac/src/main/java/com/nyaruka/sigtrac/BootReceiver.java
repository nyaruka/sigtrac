package com.nyaruka.sigtrac;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
         checkService(context);
    }


    public static boolean checkService(Context context){
        if(!isServiceRunning(context)){
            startService(context);
            return false;
        } else {
            return true;
        }
    }


    private static void startService(Context context){
        Intent intent = new Intent(context, PingService.class);
        intent.putExtra(HomeActivity.EXTRA_HOST, PingService.PING);
        context.startService(intent);
    }


    private static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
