package com.nyaruka.sigtrac;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SignalStrength;
import android.util.Log;


public class Sigtrac extends Application {

    public static final String TAG = "sigtrac";

    // Require 3MB per run
    public static final int MIN_BYTES_PER_RUN = 3145728;

    public PingService.PingResults m_pingResults;
    private boolean m_running;
    private int m_kbps;
    private boolean m_wifi;
    private int m_bytesUsed;
    private String m_connectionType;
    private String m_signalStrenghtLevel = "ONE_BAR";

    public void setBytesUsed(int bytes) {
        m_bytesUsed = bytes;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateAlarm();
    }

    public static void log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
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

    public void updateAlarm() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);

        // remove any previous alarm
        mgr.cancel(intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Sigtrac.log("Updating alarm. Run in background: " + prefs.getBoolean("run_in_background", false) + ". Capped @" + prefs.getString("data_cap", "-1"));

        if (prefs.getBoolean("run_in_background", false)) {
            int cap = Integer.parseInt(prefs.getString("data_cap", "-1"));

            long day = 1000 * 60 * 60 * 24;

            if (cap > -1) {
                int runs = cap / MIN_BYTES_PER_RUN;

                // max of 24 runs per day
                runs = Math.min(runs, 24);
                Sigtrac.log("Sleep: " + (day / runs) + "ms");

                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt("runs_per_day", runs);
                edit.putInt("bytes_per_run", cap / runs);
                edit.commit();

                Sigtrac.log("Runs per day: " + runs);
                Sigtrac.log("Bytes per run: " + (cap / runs));
                mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (day / runs), intent);
            } else {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("runs_per_day");
                edit.remove("bytes_per_run");
                edit.commit();
                mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (day / 24), intent);
            }
        } else {
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("runs_per_day");
            edit.remove("bytes_per_run");
            edit.commit();
        }

    }

    public void setRunning(boolean running) {
        m_running = running;
        invalidateUI();
    }

    public boolean isRunning() {
        return m_running;
    }

    public void setKbps(int kbps) {
        Sigtrac.log(kbps + " Kbps");
        m_kbps = kbps;
        invalidateUI();
    }

    public int getKbps() {
        return m_kbps;
    }

    public void setConnectionType(String connectionType) {
        m_connectionType = connectionType;
    }

    public String getConnectionType() {
        return m_connectionType;
    }

    public void setSignalStrengthLevel(SignalStrength signalStrength) {
        if (signalStrength.getGsmSignalStrength() >= 18){
            m_signalStrenghtLevel = "THREE_BAR";
        } else if (signalStrength.getGsmSignalStrength() >= 8) {
            m_signalStrenghtLevel = "TWO_BAR";
        } else {
            m_signalStrenghtLevel = "ONE_BAR";
        }
    }

    public String get_signalStrengthLevel() {
        return m_signalStrenghtLevel;
    }

    public void setWifi(boolean wifi) {
        m_wifi = wifi;
        invalidateUI();
    }

    public boolean isWifi() {
        return m_wifi;
    }

    public static void get() {
        return ;
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
