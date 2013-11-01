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

    private TextView m_lastResultsSpeedTextView;
    private TextView m_lastResultsPingTextView;
    private TextView m_lastResultsPacketsDropped;
    private TextView m_lastResultsCreated;

    public LastResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LastResultsView(Context context) {
        super(context);
    }

    public LastResultsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextView getLastResultsSpeedView() {
        if (m_lastResultsSpeedTextView == null) {
            m_lastResultsSpeedTextView = (TextView)findViewById(R.id.last_results_speed);
        }
        return m_lastResultsSpeedTextView;
    }

    public TextView getLastResultsPingView() {
        if (m_lastResultsPingTextView == null) {
            m_lastResultsPingTextView = (TextView) findViewById(R.id.last_results_avg_time);
        }
        return m_lastResultsPingTextView;
    }

    public TextView getLastResultsPacketsDroppedView() {
        if (m_lastResultsPacketsDropped == null){
            m_lastResultsPacketsDropped = (TextView) findViewById(R.id.last_results_packets_dropped);
        }
        return m_lastResultsPacketsDropped;
    }

    public TextView getLastResultsCreated() {
        if (m_lastResultsCreated == null){
            m_lastResultsCreated = (TextView) findViewById(R.id.last_results_created);
        }
        return m_lastResultsCreated;
    }


    public void updateLastResults(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String kbps = prefs.getString("lastResultsSpeed", null);
        String ping = prefs.getString("lastResultsPing", null);
        String packets_dropped = prefs.getString("lastResultsPacketsDropped", null);
        Long time = prefs.getLong("lastResultsCreated", 0);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        // TO-DO find a way to display time elapsed instead.

        if (kbps != null) {
            getLastResultsSpeedView().setText(kbps);
        }

        if (ping !=null) {
            getLastResultsPingView().setText(ping + "ms");
        }

        if (packets_dropped != null) {
            getLastResultsPacketsDroppedView().setText(packets_dropped + "%");
        }

        if (time > 0) {
            getLastResultsCreated().setText(dateFormat.format(new Date(time)));
        }

        if (kbps == null && ping == null && packets_dropped == null && time == 0) {
            setVisibility(GONE);
        }

    }


}
