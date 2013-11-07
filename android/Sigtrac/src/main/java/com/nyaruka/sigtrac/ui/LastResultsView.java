package com.nyaruka.sigtrac.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nyaruka.sigtrac.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastResultsView extends RelativeLayout {

    private TextView m_lastResultsTimeTextView;
    private TextView m_lastResultsSpeedValueTextView;
    private TextView m_lastResultsPingTimeValueTextView;
    private TextView m_lastResultsPacketsLossValueTextView;

    public LastResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LastResultsView(Context context) {
        super(context);
    }

    public LastResultsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextView getLastResultsTimeTextView() {
        if (m_lastResultsTimeTextView == null) {
            m_lastResultsTimeTextView = (TextView)findViewById(R.id.last_results_time);
        }
        return m_lastResultsTimeTextView;
    }

    public TextView getLastResultsSpeedValueTextView() {
        if (m_lastResultsSpeedValueTextView == null) {
            m_lastResultsSpeedValueTextView = (TextView)findViewById(R.id.last_results_speed_value);
        }
        return m_lastResultsSpeedValueTextView;
    }

    public TextView getLastResultsPingTimeValueTextView() {
        if (m_lastResultsPingTimeValueTextView == null) {
            m_lastResultsPingTimeValueTextView = (TextView) findViewById(R.id.last_results_ping_time_value);
        }
        return m_lastResultsPingTimeValueTextView;
    }

    public TextView getLastResultsPacketsLossValueTextView() {
        if (m_lastResultsPacketsLossValueTextView == null){
            m_lastResultsPacketsLossValueTextView = (TextView) findViewById(R.id.last_results_packet_loss_value);
        }
        return m_lastResultsPacketsLossValueTextView;
    }


    public void updateLastResults(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String kbps = prefs.getString("lastResultsSpeed", null);
        String ping = prefs.getString("lastResultsPing", null);
        String packets_dropped = prefs.getString("lastResultsPacketsDropped", null);
        Long time = prefs.getLong("lastResultsCreated", 0);

        setVisibility(VISIBLE);

        if (kbps != null) {
            getLastResultsSpeedValueTextView().setText(kbps);
        }

        if (ping !=null) {
            getLastResultsPingTimeValueTextView().setText(ping + "");
        }

        if (packets_dropped != null) {
            getLastResultsPacketsLossValueTextView().setText(packets_dropped + "%");
        }

        if (time > 0) {
            getLastResultsTimeTextView().setText(agoTime(time));
        }

        if (kbps == null && ping == null && packets_dropped == null && time == 0) {
            setVisibility(GONE);
        }

    }

    public String agoTime(Long time){
        Long timedelta = (System.currentTimeMillis() - time)/1000;

        if (timedelta <= 60) {
            return "A MOMENT AGO";
        } else if (timedelta <= 3600) {
            Long minutes = timedelta/60;
            return minutes + " MIN AGO";
        } else if (timedelta <= 86400) {
            Long hours = timedelta/3600;
            return hours + " HOURS AGO";
        } else if (timedelta <= 604800) {
            Long days = timedelta/ 86400;
            return days + " DAYS AGO";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy - hh:mm");
            return dateFormat.format(new Date(time));
        }

    }


}
