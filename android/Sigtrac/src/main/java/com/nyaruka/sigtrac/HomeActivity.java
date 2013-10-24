package com.nyaruka.sigtrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class HomeActivity extends Activity {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_ON_DEMAND = "on_demand";
    public static final String PING_RESULT = "com.nyaruka.sigtrac.PingResult";

    private BroadcastReceiver m_receiver;


    private void setCarrier(String carrierName, String carrierCode) {
        Sigtrac.log("Setting carrier: " + carrierName + " (" + carrierCode + ")");
        if (carrierCode != null) {
            if (carrierCode.equals("63510")) { // MTN
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.mtn));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_mtn));
            } else if (carrierCode.equals("63513")) { // Tigo
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.tigo));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_tigo));
            } else if (carrierCode.equals("63514")) { // Airtel
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.airtel));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_airtel));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        showProgressStart();


        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        setCarrier(tele.getSimOperatorName(), tele.getSimOperator());
        m_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Sigtrac sigtrac = (Sigtrac)getApplication();
                TextView speed = (TextView) HomeActivity.this.findViewById(R.id.speed);

                ImageButton onDemandStart = (ImageButton) HomeActivity.this.findViewById(R.id.onDemandStart);
                onDemandStart.setVisibility(View.GONE);

                TextView connectionType = (TextView) HomeActivity.this.findViewById(R.id.connection_type);

                ImageView bitranksLogo = (ImageView) HomeActivity.this.findViewById(R.id.bitranks_logo);

                if (sigtrac.getConnectionType() != null) {
                    connectionType.setVisibility(View.VISIBLE);
                    connectionType.setText(sigtrac.getConnectionType());
                } else {
                    connectionType.setText("");
                    connectionType.setVisibility(View.INVISIBLE);
                }

                if (sigtrac.get_signalStrengthLevel() == "THREE_BAR"){
                    bitranksLogo.setImageResource(R.drawable.bitranks_logo_3_bar);
                } else if (sigtrac.get_signalStrengthLevel() == "TWO_BAR"){
                    bitranksLogo.setImageResource(R.drawable.bitranks_logo_2_bar);
                } else {
                    bitranksLogo.setImageResource(R.drawable.bitranks_logo_1_bar);
                }


                if (sigtrac.getKbps() > 0) {
                    NumberFormat formatter = new DecimalFormat("###,###,###");
                    speed.setVisibility(View.VISIBLE);
                    HomeActivity.this.findViewById(R.id.kbps_label).setVisibility(View.VISIBLE);
                    speed.setText(formatter.format(sigtrac.getKbps()));
                    HomeActivity.this.findViewById(R.id.onStartProgress).setVisibility(View.GONE);
                }

                PingService.PingResults ping = sigtrac.getPingResults();

                if (ping != null && ping.isValid()) {
                    TextView avg = (TextView) HomeActivity.this.findViewById(R.id.avg_time);
                    avg.setText(ping.avg.intValue() + "ms");
                    avg.setVisibility(View.VISIBLE);

                    TextView packets = (TextView) HomeActivity.this.findViewById(R.id.packets_lost);
                    packets.setText(ping.pctLost.intValue() + "%");
                    packets.setVisibility(View.VISIBLE);

                    HomeActivity.this.findViewById(R.id.ping_icon).setVisibility(View.VISIBLE);
                } else {
                    HomeActivity.this.findViewById(R.id.avg_time).setVisibility(View.GONE);
                    HomeActivity.this.findViewById(R.id.packets_lost).setVisibility(View.GONE);
                    HomeActivity.this.findViewById(R.id.ping_icon).setVisibility(View.GONE);
                }

                if (sigtrac.isRunning()) {
                    HomeActivity.this.findViewById(R.id.posted).setVisibility(View.GONE);
                } else {
                    HomeActivity.this.findViewById(R.id.posted).setVisibility(View.VISIBLE);
                    onDemandStart.setVisibility(View.VISIBLE);
                }

                if (sigtrac.isWifi()) {
                    Toast.makeText(HomeActivity.this, "Sorry, can't run mobile network test while connected to wifi", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((m_receiver), new IntentFilter(PING_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(m_receiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void onDemandStart(View view) {
        Intent intent = new Intent(this, PingService.class);
        intent.putExtra(EXTRA_ON_DEMAND, true);
        intent.putExtra(EXTRA_HOST, "www.google.com");
        startService(intent);
        showProgressStart();
    }

    public void showProgressStart() {
        HomeActivity.this.findViewById(R.id.ping_icon).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.packets_lost).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.avg_time).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.speed).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.kbps_label).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.onDemandStart).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.posted).setVisibility(View.GONE);
        HomeActivity.this.findViewById(R.id.onStartProgress).setVisibility(View.VISIBLE);
    }
}
