package com.nyaruka.sigtrac;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingService extends IntentService {

    private static final String DOWNLOAD_FILE = "http://speedtest.newark.linode.com/100MB-newark.bin";

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
        sigtrac.setPingResults(null);
        sigtrac.setKbps(-1);

        sigtrac.setRunning(true);

        updateLocation();
        String host = intent.getStringExtra(HomeActivity.EXTRA_HOST);

        // do some pinging
        PingResults ping = pingHost(host, 6);
        if (ping != null) {
            sigtrac.setPingResults(ping);
        }

        downloadFile(DOWNLOAD_FILE);
        Log.d(Sigtrac.TAG, "Location: " + m_currentLocation);
        postData(ping, sigtrac.getKbps());
        sigtrac.setRunning(false);

    }

    private void downloadFile(String url) {
        try {
            long start = System.currentTimeMillis();

            URLConnection connection = new URL(url + "?" + start).openConnection();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            byte[] buf = new byte[1024];
            int bytes = 0;
            int read = 0;
            int i = 0;

            while ((read = bis.read(buf)) != -1) {
                bytes += read;

                // update our kbps
                if (i % 40 == 0) {
                    ((Sigtrac)getApplication()).setKbps(getKbps(start, bytes));
                }

                // for now just run the test for 15 seconds
                if ((System.currentTimeMillis() - start) > 15000) {
                    Log.d(Sigtrac.TAG, "Test is complete, bailing");
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

        Log.d(Sigtrac.TAG, "Pinging " + host);

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

    public void postData(PingResults ping, int kbps) {

        if (ping == null || !ping.isValid()) {
            Log.d(Sigtrac.TAG, "Invalid ping, skipping");
            return;
        }

        if (m_lastSignal == null) {
            Log.d(Sigtrac.TAG, "No signal, skipping");
            return;
        }

        TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uuid = UUID.randomUUID().toString();
        if (!prefs.contains("deviceId")) {
            prefs.edit().putString("deviceId", uuid).commit();
        }
        String deviceId = prefs.getString("deviceId", uuid);

        // TODO: write these to a file first before posting in case there's no internet

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://sigtrac.nyaruka.com/submit");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("device", deviceId));
            nameValuePairs.add(new BasicNameValuePair("carrier", manager.getSimOperatorName()));
            nameValuePairs.add(new BasicNameValuePair("device_build", Build.MANUFACTURER + ";" + Build.MODEL));
            nameValuePairs.add(new BasicNameValuePair("device_type", "AND"));
            nameValuePairs.add(new BasicNameValuePair("connection_type", cm.getActiveNetworkInfo().getSubtypeName()));
            nameValuePairs.add(new BasicNameValuePair("download_speed", "" + kbps));
            nameValuePairs.add(new BasicNameValuePair("ping", "" + ping.avg.intValue()));
            nameValuePairs.add(new BasicNameValuePair("packets_dropped", "" + ping.pctLost));
            nameValuePairs.add(new BasicNameValuePair("signal_strength_asu", "" + m_lastSignal.getGsmSignalStrength()));
            nameValuePairs.add(new BasicNameValuePair("signal_strength_dbm", "" +  ((2 * m_lastSignal.getGsmSignalStrength())-113)));

            if (m_currentLocation != null) {
                nameValuePairs.add(new BasicNameValuePair("latitude", "" + m_currentLocation.m_lat));
                nameValuePairs.add(new BasicNameValuePair("longitude", "" + m_currentLocation.m_lng));
            }

            nameValuePairs.add(new BasicNameValuePair("created_on", "" + (new Date().getTime() / 1000)));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            String entity = EntityUtils.toString(post.getEntity());
            Log.d(Sigtrac.TAG, entity);

            // Execute HTTP Post Request
            HttpResponse response = client.execute(post);
            Log.d(Sigtrac.TAG, "Response: " + EntityUtils.toString(response.getEntity()));

        } catch (Throwable t) {
            Log.e(Sigtrac.TAG, "Failed post", t);
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
                Log.d(Sigtrac.TAG, line);
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
