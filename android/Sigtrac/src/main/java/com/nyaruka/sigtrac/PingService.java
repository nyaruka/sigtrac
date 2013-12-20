package com.nyaruka.sigtrac;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingService extends IntentService {

    // private static final String DOWNLOAD_FILE = "http://speedtest.atlanta.linode.com/100MB-atlanta.bin";
    // private static final String DOWNLOAD_FILE = "http://speedtest.newark.linode.com/100MB-newark.bin";
    // public static final String DOWNLOAD_FILE = "http://speedtest.london.linode.com/100MB-london.bin";
    // public static final String DOWNLOAD_FILE = "http://bitranks.com/media/100MB.bin";
    public static final String DOWNLOAD_FILE = "https://s3.amazonaws.com/bitranks/100MB.bin";

    public static final String PING = "8.8.8.8";

    private PhoneStateListener m_signalListener;
    private SignalStrength m_lastSignal;
    private GeoPoint m_currentLocation;

    public PingService() {
        super("PingService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_signalListener = new PhoneStateListener(){
            public void onSignalStrengthsChanged (SignalStrength signal) {
                // Log.d(HomeActivity.TAG, signal.toString());
                m_lastSignal = signal;
                ((Sigtrac)getApplication()).invalidateUI();
            }
        };

        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tele.listen(m_signalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        updateLocation();
    }

    private void updateLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = getBestProvider();
        if (bestProvider != null) {
            // Get the current location in start-up
            Location location = (locationManager.getLastKnownLocation(bestProvider));
            if (location != null) {
                m_currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        }
    }

    public String getBestProvider(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        return locationManager.getBestProvider(criteria, true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tele.listen(m_signalListener, PhoneStateListener.LISTEN_NONE);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Sigtrac sigtrac = (Sigtrac)getApplication();


        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);


        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);


        boolean background = prefs.getBoolean("run_in_background", false);
        int bytesPerRun = prefs.getInt("bytes_per_run", -1);

        boolean onDemand = intent.getBooleanExtra(HomeActivity.EXTRA_ON_DEMAND, false);

        if (!onDemand && !background) {
            Sigtrac.log("Automatic runs are currently disabled");
            return;
        }

        // don't cap on demand runs
        if (onDemand) {
            bytesPerRun = -1;
        }

        sigtrac.setPingResults(null);
        sigtrac.setConnectionType(null);
        sigtrac.setKbps(-1);
        sigtrac.setWifi(false);
        sigtrac.setRunning(true);

        if (wifi.isConnected()) {
            Sigtrac.log("Wifi connected, not running test");
            sigtrac.setWifi(true);
            sigtrac.setRunning(false);
            return;
        }

        updateLocation();

        if (m_lastSignal != null){
            sigtrac.setSignalStrengthLevel(m_lastSignal);
        }

        sigtrac.setConnectionType(getConnectedDataNetwork(telephonyManager.getNetworkType()));


        String host = intent.getStringExtra(HomeActivity.EXTRA_HOST);


        // do some pinging
        PingResults ping = pingHost(host, 6);

        downloadFile(DOWNLOAD_FILE, bytesPerRun);


        if (ping != null) {
            sigtrac.setPingResults(ping);
        }

        Sigtrac.log("Location: " + m_currentLocation);
        saveData(ping, sigtrac.getKbps(), telephonyManager);

        sigtrac.setRunning(false);

    }

    private void downloadFile(String url, int maxBytes) {
        try {

            Sigtrac.log("Capping download at " + maxBytes + " bytes");

            long start = System.currentTimeMillis();

            URLConnection connection = new URL(url + "?" + start).openConnection();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            byte[] buf = new byte[1024];
            int bytes = 0;
            int read = 0;
            int i = 0;

            Sigtrac sigtrac = ((Sigtrac)getApplication());
            while ((read = bis.read(buf)) != -1) {
                bytes += read;

                // update our kbps
                if (i % 40 == 0) {
                    sigtrac.setKbps(getKbps(start, bytes));
                    sigtrac.setBytesUsed(bytes);
                }

                if (maxBytes > -1 && bytes > maxBytes) {
                    Sigtrac.log("Downloaded " + (bytes / 1024) + "KB, bailing");
                    break;
                }

                // for now just run the test for 15 seconds
                if ((System.currentTimeMillis() - start) > 15000) {
                    Sigtrac.log("Ran 15s, bailing");
                    Sigtrac.log("Downloaded " + (bytes / 1024) + "KB");
                    break;
                }
                i++;
            }
        } catch (Throwable t) {
            Log.e(Sigtrac.TAG, "Failed to download file", t);
        }
    }

    private int getKbps(long start, int bytes) {
        int elapsed = (int)(System.currentTimeMillis() - start) / 1000;
        return (((bytes * 8) / 1024) /  Math.max(elapsed, 1));
    }

    private PingResults pingHost(String host, int count) {

        Sigtrac.log("Pinging " + host);

        try {
            Process process = new ProcessBuilder()
                    .command("/system/bin/ping", "-c", count + "", host)
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0) {
                output.append(buffer, 0, i);
            }
            reader.close();

            String results =  output.toString();
            if (results != null) {
                PingResults pingResults = new PingResults(results);
                if (pingResults.isValid()) {
                    return pingResults;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }



    public void saveData(PingResults ping, int kbps, TelephonyManager telephonyManager) {
        if (ping == null || !ping.isValid()) {
            Sigtrac.log("Invalid ping, skipping");
            return;
        }

        if (m_lastSignal == null) {
            Sigtrac.log("No signal, skipping");
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uuid = UUID.randomUUID().toString();
        if (!prefs.contains("deviceId")) {
            prefs.edit().putString("deviceId", uuid).commit();
        }
        String deviceId = prefs.getString("deviceId", uuid);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("device", deviceId));
            nameValuePairs.add(new BasicNameValuePair("carrier", telephonyManager.getSimOperatorName()));
            nameValuePairs.add(new BasicNameValuePair("country", telephonyManager.getSimCountryIso()));
            nameValuePairs.add(new BasicNameValuePair("device_build", Build.MANUFACTURER + ";" + Build.MODEL));
            nameValuePairs.add(new BasicNameValuePair("device_type", "AND"));
            nameValuePairs.add(new BasicNameValuePair("connection_type", getConnectedDataNetwork(telephonyManager.getNetworkType())));
            nameValuePairs.add(new BasicNameValuePair("download_speed", "" + kbps));
            nameValuePairs.add(new BasicNameValuePair("ping", "" + ping.avg.intValue()));
            nameValuePairs.add(new BasicNameValuePair("packets_dropped", "" + ping.pctLost));
            nameValuePairs.add(new BasicNameValuePair("signal_strength_asu", "" + m_lastSignal.getGsmSignalStrength()));
            nameValuePairs.add(new BasicNameValuePair("signal_strength_dbm", "" +  ((2 * m_lastSignal.getGsmSignalStrength())-113)));
            nameValuePairs.add(new BasicNameValuePair("version", "" + info.versionCode));

            if (m_currentLocation != null) {
                nameValuePairs.add(new BasicNameValuePair("latitude", "" + m_currentLocation.m_lat));
                nameValuePairs.add(new BasicNameValuePair("longitude", "" + m_currentLocation.m_lng));
            }

            long now = System.currentTimeMillis();

            long created = now / 1000;
            nameValuePairs.add(new BasicNameValuePair("created_on", "" + created));


            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
            String entityString = EntityUtils.toString(entity);

            String filename = "" + created;

            FileOutputStream fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(entityString.getBytes());
            fileOutputStream.close();


            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("lastResultsSpeed", "" + kbps);
            edit.putString("lastResultsPing", "" + ping.avg.intValue());
            edit.putString("lastResultsPacketsDropped", "" + ping.pctLost);
            edit.putLong("lastResultsCreated", now);
            edit.commit();

            Intent intent = new Intent(this, PostDataService.class);
            startService(intent);


        } catch (Throwable t){
            Log.e(Sigtrac.TAG, "Failed Saving data to file", t);
        }


    }

    public String getConnectedDataNetwork(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";

            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";

            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";

            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";

            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev.0";

            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev.A";

            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";

            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSPDA";

            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";

            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";

            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";

            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev.B";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";

            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";

            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";

            default:
                return "UNKNOWN";

        }
    }


    public static String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "UNKNOWN";
        }
    }


    /**
     * Takes raw text from command line ping and parses it into something useful
     */
    public static final class PingResults {

        public BigDecimal pctLost;
        public BigDecimal min = BigDecimal.ZERO;
        public BigDecimal avg = BigDecimal.ZERO;
        public BigDecimal max = BigDecimal.ZERO;
        public BigDecimal dev = BigDecimal.ZERO;

        public PingResults(String results) {

            Pattern packetPattern = Pattern.compile(".* ([0-9]+)% packet loss.*");
            Pattern timePattern = Pattern.compile("rtt min\\/avg\\/max\\/mdev = (.*)\\/(.*)\\/(.*)\\/(.*) ms");

            String[] lines = results.split("\n");
            for (String line : lines) {
                Sigtrac.log(line);
                Matcher matcher = timePattern.matcher(line);
                if (matcher.find()) {
                    min = new BigDecimal(matcher.group(1));
                    avg = new BigDecimal(matcher.group(2));
                    max = new BigDecimal(matcher.group(3));
                    dev = new BigDecimal(matcher.group(4));
                }

                matcher = packetPattern.matcher(line);
                if (matcher.find()) {
                    pctLost = new BigDecimal(matcher.group(1));
                }
            }
        }

        public boolean isValid() {
            return pctLost != null;
        }
    }

    /**
     * Simple struct to hold our location coordinates
     */
    public class GeoPoint {
        private double m_lat;
        private double m_lng;
        public GeoPoint(double lat, double lng) {
            m_lat = lat;
            m_lng = lng;
        }

        public String toString() {
            return m_lat + ";" + m_lng;
        }
    }

}
