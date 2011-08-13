package com.ifreebudget.fm.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.services.SessionManager;

public class AddReminderActivity extends Activity {

    private Button startDtBtn, endDtBtn, startTimeBtn, endTimeBtn;
    private RadioButton dailyBtn, weeklyBtn, monthlyBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_reminder_layout);

        startDtBtn = (Button) findViewById(R.id.start_date_btn);
        endDtBtn = (Button) findViewById(R.id.end_date_btn);

        startTimeBtn = (Button) findViewById(R.id.start_time_btn);
        endTimeBtn = (Button) findViewById(R.id.end_time_btn);

        Calendar s = Calendar.getInstance();
        Calendar e = Calendar.getInstance();
        e.add(Calendar.DATE, 1);
        e.add(Calendar.HOUR_OF_DAY, 1);

        String startDt = SessionManager.getDateFormat().format(s.getTime());
        String endDt = SessionManager.getDateFormat().format(e.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String startTime = sdf.format(s.getTime());
        String endTime = sdf.format(e.getTime());

        startDtBtn.setText(startDt);
        endDtBtn.setText(endDt);

        startTimeBtn.setText(startTime);
        endTimeBtn.setText(endTime);

        dailyBtn = (RadioButton) findViewById(R.id.daily_btn);
        dailyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AddReminderActivity", "Clicked");
                setRepeatsView(v, R.layout.daily_repeat_layout);
            }
        });

        weeklyBtn = (RadioButton) findViewById(R.id.weekly_btn);
        weeklyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AddReminderActivity", "Clicked");
                setRepeatsView(v, R.layout.weekly_repeat_layout);
            }
        });

    }

    private void setRepeatsView(View v, int layoutId) {
        Log.e("AddReminderActivity", "Clicked" + layoutId);
        Context c = v.getContext();
        LayoutInflater li = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.add_reminder_layout);

        View vv = li.inflate(layoutId, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.repeat_type_panel);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll.addView(vv, params);
    }
}
