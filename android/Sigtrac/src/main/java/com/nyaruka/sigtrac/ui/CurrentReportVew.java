package com.nyaruka.sigtrac.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nyaruka.sigtrac.PingService;
import com.nyaruka.sigtrac.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class CurrentReportVew extends RelativeLayout {


    private RelativeLayout m_currentReportInitialContainer;
    private ProgressBar m_currentReportProgressBar;
    private RelativeLayout m_currentReportRunningContainer;

    private TextView m_helpText;


    private TextView m_pingTimeLabel;
    private TextView m_pingTimeUnitLabel;
    private TextView m_pingTimeValue;
    private ImageView m_pingTimeIcon;

    private TextView m_packetsLossLabel;
    private TextView m_packetsLossValue;

    private TextView m_speedLabel;
    private TextView m_speedUnitLabel;
    private TextView m_speedValue;
    private ImageView m_speedIcon;

    private RelativeLayout m_restartButton;

    private RelativeLayout m_verticalDivider;


    private TextView m_currentReportKbpsLabelView;
    private TextView m_currentReportSpeedView;
    private TextView m_currentReportAvgTimeView;
    private TextView m_currentReportPingLabelView;
    private TextView m_currentReportPacketsDroppedView;
    private TextView m_currentReportPacketsdroppedLabelView;
    private ImageButton m_currentReportStartButtonView;
    private ProgressBar m_currentReportProgressView;

    private ImageView m_currentReportPingIconView;


    public CurrentReportVew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CurrentReportVew(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CurrentReportVew(Context context) {
        super(context);
    }

    public RelativeLayout getCurrentReportInitialContainer() {
        if (m_currentReportInitialContainer == null) {
            m_currentReportInitialContainer = (RelativeLayout)findViewById(R.id.current_report_initial_container);
        }
        return m_currentReportInitialContainer;
    }

    public RelativeLayout getCurrentReportRunningContainer() {
        if (m_currentReportRunningContainer == null) {
            m_currentReportRunningContainer = (RelativeLayout)findViewById(R.id.current_report_running_container);
        }
        return m_currentReportRunningContainer;
    }

    public ProgressBar getCurrentReportProgressBar() {
        if(m_currentReportProgressBar == null) {
            m_currentReportProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        }
        return m_currentReportProgressBar;
    }

    public TextView getHelpText() {
        if(m_helpText == null) {
            m_helpText = (TextView)findViewById(R.id.help_text);
        }
        return m_helpText;
    }


    public TextView getPingTimeLabel() {
        if (m_pingTimeLabel == null) {
            m_pingTimeLabel = (TextView)findViewById(R.id.current_report_running_ping_time_label);
        }
        return m_pingTimeLabel;
    }

    public TextView getPingTimeUnitLabel() {
        if (m_pingTimeUnitLabel == null) {
            m_pingTimeUnitLabel = (TextView)findViewById(R.id.current_report_running_ping_time_unit_label);
        }
        return m_pingTimeUnitLabel;
    }

    public TextView getPingTimeValue() {
        if (m_pingTimeValue == null) {
            m_pingTimeValue = (TextView) findViewById(R.id.current_report_running_ping_time_value);
        }
        return m_pingTimeValue;
    }

    public ImageView getPingTimeIcon() {
        if (m_pingTimeIcon == null) {
            m_pingTimeIcon = (ImageView)findViewById(R.id.current_report_running_ping_time_icon);
        }
        return m_pingTimeIcon;
    }

    public TextView getPacketsLossLabel() {
        if(m_packetsLossLabel == null) {
            m_packetsLossLabel = (TextView)findViewById(R.id.current_report_running_packets_loss_label);
        }
        return m_packetsLossLabel;
    }


    public TextView getPacketsLossValue() {
        if (m_packetsLossValue == null){
            m_packetsLossValue = (TextView)findViewById(R.id.current_report_running_packets_loss_value);
        }
        return m_packetsLossValue;
    }

    public TextView getSpeedLabel() {
        if (m_speedLabel == null) {
            m_speedLabel = (TextView)findViewById(R.id.current_report_running_speed_label);
                    }
        return m_speedLabel;
    }

    public TextView getSpeedUnitLabel() {
        if (m_speedUnitLabel == null) {
            m_speedUnitLabel = (TextView)findViewById(R.id.current_report_running_speed_unit_label);
        }
        return m_speedUnitLabel;
    }

    public TextView getSpeedValue() {
        if (m_speedValue == null) {
            m_speedValue = (TextView) findViewById(R.id.current_report_running_speed_value);
        }
        return m_speedValue;
    }

    public ImageView getSpeedIcon() {
        if (m_speedIcon == null) {
            m_speedIcon = (ImageView)findViewById(R.id.current_report_running_speed_icon);
        }
        return m_speedIcon;
    }

    public RelativeLayout getRestartButton() {
        if (m_restartButton == null) {
            m_restartButton = (RelativeLayout)findViewById(R.id.current_report_running_restart_button);
        }
        return m_restartButton;
    }

    public RelativeLayout getVerticalDivider() {
        if (m_verticalDivider == null) {
            m_verticalDivider = (RelativeLayout)findViewById(R.id.vertical_divider);
        }
        return m_verticalDivider;
    }


    public void showProgressBar() {
        getCurrentReportInitialContainer().setVisibility(GONE);
        getCurrentReportRunningContainer().setVisibility(GONE);
        getCurrentReportProgressBar().setVisibility(VISIBLE);
    }

    public void showInitialStartButton() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String kbps = prefs.getString("lastResultsSpeed", null);
        String ping = prefs.getString("lastResultsPing", null);
        String packets_dropped = prefs.getString("lastResultsPacketsDropped", null);

        getCurrentReportRunningContainer().setVisibility(GONE);
        getCurrentReportProgressBar().setVisibility(GONE);
        getCurrentReportInitialContainer().setVisibility(VISIBLE);

        if (kbps == null && ping == null && packets_dropped == null) {
            getHelpText().setVisibility(VISIBLE);
        } else {
            getHelpText().setVisibility(GONE);
        }

    }

    public void showRunningContainer() {
        getCurrentReportProgressBar().setVisibility(GONE);
        getCurrentReportInitialContainer().setVisibility(GONE);
        getCurrentReportRunningContainer().setVisibility(VISIBLE);

    }


    public void toggleRestartButton(boolean isRunning) {
        if (isRunning) {
            getRestartButton().setVisibility(GONE);
        } else {
            getRestartButton().setVisibility(VISIBLE);
        }

    }

    public void setSpeed(int speed) {
        NumberFormat formatter = new DecimalFormat("###,###,###");
        getSpeedValue().setText(formatter.format(speed));
        showRunningContainer();
    }

    public void setPing(PingService.PingResults ping) {
        if (ping != null && ping.isValid()) {
            getPingTimeValue().setText(ping.avg.intValue() + "");
            getPacketsLossValue().setText(ping.pctLost.intValue() + "%");
            getPingTimeLabel().setVisibility(VISIBLE);
            getPingTimeUnitLabel().setVisibility(VISIBLE);
            getPingTimeValue().setVisibility(VISIBLE);
            getPingTimeIcon().setVisibility(VISIBLE);
            getPacketsLossLabel().setVisibility(VISIBLE);
            getPacketsLossValue().setVisibility(VISIBLE);
            getVerticalDivider().setVisibility(VISIBLE);
            showRunningContainer();
        } else {
            getPingTimeLabel().setVisibility(INVISIBLE);
            getPingTimeUnitLabel().setVisibility(INVISIBLE);
            getPingTimeValue().setVisibility(INVISIBLE);
            getPingTimeIcon().setVisibility(INVISIBLE);
            getPacketsLossLabel().setVisibility(INVISIBLE);
            getPacketsLossValue().setVisibility(INVISIBLE);
            getVerticalDivider().setVisibility(INVISIBLE);
        }
    }

}
