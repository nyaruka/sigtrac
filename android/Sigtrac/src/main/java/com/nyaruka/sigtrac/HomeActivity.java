package com.nyaruka.sigtrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class HomeActivity extends Activity {

    public static final String EXTRA_HOST = "host";
    public static final String PING_RESULT = "com.nyaruka.sigtrac.PingResult";

    private BroadcastReceiver m_receiver;


    private void setCarrier(String carrier) {
        Sigtrac.log( "Setting carrier: " + carrier);
        if (carrier != null) {
            if (carrier.indexOf("MTN") > -1) {
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.mtn));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_mtn));
            } else if (carrier.indexOf("Tigo") > -1 || carrier.equals("63513")) {
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.tigo));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_tigo));
            } else if (carrier.indexOf("Airtel") > -1) {
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.airtel));
                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.sigtrac_airtel));
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        setCarrier(tele.getSimOperatorName());
        m_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PingService.PingResults ping = ((Sigtrac)getApplication()).getPingResults();

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

                Sigtrac sigtrac = (Sigtrac)getApplication();
                TextView speed = (TextView) HomeActivity.this.findViewById(R.id.speed);

                if (sigtrac.getKbps() > 0) {
                    NumberFormat formatter = new DecimalFormat("###,###,###");
                    speed.setText(formatter.format(sigtrac.getKbps()));
                } else {
                    speed.setText("...");
                }

                if (sigtrac.isRunning()) {
                    HomeActivity.this.findViewById(R.id.posted).setVisibility(View.GONE);
                } else {
                    HomeActivity.this.findViewById(R.id.posted).setVisibility(View.VISIBLE);
                }

                // startPing();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((m_receiver), new IntentFilter(PING_RESULT));

        // startPing();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(m_receiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public void speedTapped(View view) {
        Intent intent = new Intent(this, PingService.class);
        intent.putExtra(EXTRA_HOST, "www.google.com");
        startService(intent);
    }
}
