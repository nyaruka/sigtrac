package com.nyaruka.sigtrac;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class Sigtrac extends Application {

    public static final String TAG = "sigtrac";

    public PingService.PingResults m_pingResults;
    private boolean m_running;
    private int m_kbps;

    @Override
    public void onCreate() {
        super.onCreate();
        setAlarm();
    }

    public void invalidateUI() {
        // notify about the update
        Intent result = new Intent(HomeActivity.PING_RESULT);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(result);
    }

    public void setPingResults(PingService.PingResults results) {
        m_pingResults = results;
        invalidateUI();
    }

    public PingService.PingResults getPingResults() {
        return m_pingResults;
    }

    private void setAlarm() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * 60 * 30, intent);
    }

    public void setRunning(boolean running) {
        m_running = running;
        invalidateUI();
    }

    public boolean isRunning() {
        return m_running;
    }

    public void setKbps(int kbps) {
        Log.d(TAG, kbps + " Kbps");
        m_kbps = kbps;
        invalidateUI();
    }

    public int getKbps() {
        return m_kbps;
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        public AlarmReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent run = new Intent(context, PingService.class);
            run.putExtra(HomeActivity.EXTRA_HOST, "www.google.com");
            context.startService(run);
        }
    }
}
