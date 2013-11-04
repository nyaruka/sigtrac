package com.nyaruka.sigtrac.ui;

import android.content.Context;
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

    public TextView getCurrentReportKbpsLabelView() {
        if (m_currentReportKbpsLabelView == null) {
            m_currentReportKbpsLabelView= (TextView)findViewById(R.id.current_report_kbps_label);
        }
        return m_currentReportKbpsLabelView;
    }


    public TextView getCurrentReportSpeedView() {
        if (m_currentReportSpeedView == null) {
            m_currentReportSpeedView= (TextView)findViewById(R.id.current_report_speed);
        }
        return m_currentReportSpeedView;
    }

    public TextView getCurrentReportAvgTimeView() {
        if (m_currentReportAvgTimeView == null) {
            m_currentReportAvgTimeView= (TextView)findViewById(R.id.current_report_avg_time);
        }
        return m_currentReportAvgTimeView;
    }

    public TextView getCurrentReportPingLabelView() {
        if (m_currentReportPingLabelView == null) {
            m_currentReportPingLabelView= (TextView)findViewById(R.id.current_report_ping_label);
        }
        return m_currentReportPingLabelView;
    }

    public TextView getCurrentReportPacketsDroppedView() {
        if (m_currentReportPacketsDroppedView == null) {
            m_currentReportPacketsDroppedView= (TextView)findViewById(R.id.current_report_packets_lost);
        }
        return m_currentReportPacketsDroppedView;
    }

    public TextView getCurrentReportPacketsdroppedLabelView() {
        if (m_currentReportPacketsdroppedLabelView == null) {
            m_currentReportPacketsdroppedLabelView= (TextView)findViewById(R.id.current_report_lost_label);
        }
        return m_currentReportPacketsdroppedLabelView;
    }


    public ImageButton getCurrentReportStartButtonView() {
        if (m_currentReportStartButtonView == null) {
            m_currentReportStartButtonView = (ImageButton)findViewById(R.id.current_report_start_button);
        }
        return m_currentReportStartButtonView;
    }

    public ProgressBar getCurrentReportProgressView() {
        if (m_currentReportProgressView == null){
            m_currentReportProgressView = (ProgressBar) findViewById(R.id.current_report_progress);
        }
        return m_currentReportProgressView;
    }

    public ImageView getCurrentReportPingIconView() {
        if (m_currentReportPingIconView == null) {
            m_currentReportPingIconView = (ImageView) findViewById(R.id.current_report_ping_icon);
        }
        return m_currentReportPingIconView;
    }

    public void showProgressBar() {
        getCurrentReportAvgTimeView().setVisibility(GONE);
        getCurrentReportPingLabelView().setVisibility(GONE);
        getCurrentReportSpeedView().setVisibility(GONE);
        getCurrentReportKbpsLabelView().setVisibility(GONE);
        getCurrentReportPacketsdroppedLabelView().setVisibility(GONE);
        getCurrentReportPacketsDroppedView().setVisibility(GONE);
        getCurrentReportStartButtonView().setVisibility(GONE);
        getCurrentReportProgressView().setVisibility(VISIBLE);
        getCurrentReportPingIconView().setVisibility(GONE);

    }

    public void showStartButton() {
        getCurrentReportAvgTimeView().setVisibility(GONE);
        getCurrentReportPingLabelView().setVisibility(GONE);
        getCurrentReportSpeedView().setVisibility(GONE);
        getCurrentReportKbpsLabelView().setVisibility(GONE);
        getCurrentReportPacketsdroppedLabelView().setVisibility(GONE);
        getCurrentReportPacketsDroppedView().setVisibility(GONE);
        getCurrentReportStartButtonView().setVisibility(VISIBLE);
        getCurrentReportProgressView().setVisibility(GONE);
        getCurrentReportPingIconView().setVisibility(GONE);
    }

    public void showCurrentReport() {
        getCurrentReportStartButtonView().setVisibility(GONE);
        getCurrentReportProgressView().setVisibility(GONE);
        getCurrentReportAvgTimeView().setVisibility(VISIBLE);
        getCurrentReportPingLabelView().setVisibility(VISIBLE);
        getCurrentReportSpeedView().setVisibility(VISIBLE);
        getCurrentReportKbpsLabelView().setVisibility(VISIBLE);
        getCurrentReportPacketsdroppedLabelView().setVisibility(VISIBLE);
        getCurrentReportPacketsDroppedView().setVisibility(VISIBLE);
        getCurrentReportPingIconView().setVisibility(VISIBLE);
    }

    public void setSpeed(int speed) {
        NumberFormat formatter = new DecimalFormat("###,###,###");
        getCurrentReportSpeedView().setText(formatter.format(speed));
        showCurrentReport();
    }

    public void setPing(PingService.PingResults ping) {
        if (ping != null && ping.isValid()) {
            getCurrentReportAvgTimeView().setText(ping.avg.intValue() + "ms");
            getCurrentReportPacketsDroppedView().setText(ping.pctLost.intValue() + "%");
            showCurrentReport();
        } else {
            getCurrentReportPingIconView().setVisibility(GONE);
            getCurrentReportPacketsdroppedLabelView().setVisibility(GONE);
            getCurrentReportPacketsDroppedView().setVisibility(GONE);
            getCurrentReportPingLabelView().setVisibility(GONE);
            getCurrentReportAvgTimeView().setVisibility(GONE);
        }
    }

}
