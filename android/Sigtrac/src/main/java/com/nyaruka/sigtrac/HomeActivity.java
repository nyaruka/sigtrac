package com.nyaruka.sigtrac;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nyaruka.sigtrac.ui.CurrentReportVew;
import com.nyaruka.sigtrac.ui.LastResultsView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends Activity {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_ON_DEMAND = "on_demand";
    public static final String PING_RESULT = "com.nyaruka.sigtrac.PingResult";

    private BroadcastReceiver m_receiver;

    private void setPhoneDetails() {
        ((TextView)findViewById(R.id.phone_model)).setText(Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL.toUpperCase());

        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        Date date = new Date();

        ((TextView)findViewById(R.id.current_date)).setText(dateFormat.format(date).toUpperCase());
    }

    private void setCarrier(String carrierName, String carrierCode) {
        Sigtrac.log("Setting carrier: " + carrierName + " (" + carrierCode + ")");
        if (carrierCode != null) {
            if (carrierCode.equals("63510")) { // MTN
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.mtn));

                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.mtn_logo));

                findViewById(R.id.start_button_icon).setBackgroundColor(getResources().getColor(R.color.mtn));
                ((TextView)findViewById(R.id.start_button_text)).setTextColor(getResources().getColor(R.color.mtn));

                findViewById(R.id.restart_button_icon).setBackgroundColor(getResources().getColor(R.color.mtn));
                ((TextView)findViewById(R.id.restart_button_text)).setTextColor(getResources().getColor(R.color.mtn));
            } else if (carrierCode.equals("63513")) { // Tigo
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.tigo));

                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.tigo_logo));

                findViewById(R.id.start_button_icon).setBackgroundColor(getResources().getColor(R.color.tigo));
                ((TextView)findViewById(R.id.start_button_text)).setTextColor(getResources().getColor(R.color.tigo));

                findViewById(R.id.restart_button_icon).setBackgroundColor(getResources().getColor(R.color.tigo));
                ((TextView)findViewById(R.id.restart_button_text)).setTextColor(getResources().getColor(R.color.tigo));

            } else if (carrierCode.equals("63514")) { // Airtel
                findViewById(R.id.main_bg).setBackgroundColor(getResources().getColor(R.color.airtel));

                ((ImageView)findViewById(R.id.carrier_logo)).setImageDrawable(getResources().getDrawable(R.drawable.airtel_logo));


                findViewById(R.id.start_button_icon).setBackgroundColor(getResources().getColor(R.color.airtel));
                ((TextView)findViewById(R.id.start_button_text)).setTextColor(getResources().getColor(R.color.airtel));


                findViewById(R.id.restart_button_icon).setBackgroundColor(getResources().getColor(R.color.airtel));
                ((TextView)findViewById(R.id.restart_button_text)).setTextColor(getResources().getColor(R.color.airtel));

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        CurrentReportVew currentReportVew = (CurrentReportVew) HomeActivity.this.findViewById(R.id.current_report_container);
        currentReportVew.showInitialStartButton();

        LastResultsView lastResultsView = (LastResultsView) HomeActivity.this.findViewById(R.id.last_results_container);
        lastResultsView.updateLastResults();


        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        setPhoneDetails();
        setCarrier(tele.getSimOperatorName(), tele.getSimOperator());
        m_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Sigtrac sigtrac = (Sigtrac)getApplication();

                CurrentReportVew currentReportVew = (CurrentReportVew) HomeActivity.this.findViewById(R.id.current_report_container);

                LastResultsView lastResultsView = (LastResultsView) HomeActivity.this.findViewById(R.id.last_results_container);

                TextView connectionType = (TextView) HomeActivity.this.findViewById(R.id.connection_type);

                ImageView bitranksLogo = (ImageView) HomeActivity.this.findViewById(R.id.connection_strenght_icon);

                if (sigtrac.getConnectionType() != null) {
                    connectionType.setVisibility(View.VISIBLE);
                    connectionType.setText(sigtrac.getConnectionType());
                } else {
                    connectionType.setText("");
                    connectionType.setVisibility(View.INVISIBLE);
                }

                if (sigtrac.get_signalStrengthLevel() == "THREE_BAR"){
                    bitranksLogo.setImageResource(R.drawable.connection_strength_three);
                } else if (sigtrac.get_signalStrengthLevel() == "TWO_BAR"){
                    bitranksLogo.setImageResource(R.drawable.connection_strength_two);
                } else {
                    bitranksLogo.setImageResource(R.drawable.connection_strength_one);
                }

                if (sigtrac.getKbps() > 0) {
                    currentReportVew.setSpeed(sigtrac.getKbps());
                } else {
                    currentReportVew.showProgressBar();
                }

                PingService.PingResults ping = sigtrac.getPingResults();
                currentReportVew.setPing(ping);

                currentReportVew.toggleRestartButton(sigtrac.isRunning());

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
        CurrentReportVew currentReportVew = (CurrentReportVew) HomeActivity.this.findViewById(R.id.current_report_container);
        currentReportVew.showProgressBar();
        LastResultsView lastResultsView = (LastResultsView) HomeActivity.this.findViewById(R.id.last_results_container);
        lastResultsView.updateLastResults();

    }


}
