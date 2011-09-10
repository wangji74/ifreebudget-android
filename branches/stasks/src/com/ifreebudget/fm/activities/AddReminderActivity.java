package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

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
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddReminderAction;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.BasicSchedule;
import com.ifreebudget.fm.scheduler.task.BasicTask;
import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Schedule.RepeatType;
import com.ifreebudget.fm.scheduler.task.Task;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddReminderActivity extends Activity {

    private final String TAG = "AddReminderActivity";

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
        LinearLayout ll = (LinearLayout) findViewById(R.id.repeat_info_panel);

        View vv = li.inflate(layoutId, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.repeat_type_panel);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ll.removeAllViews();

        ll.addView(vv, params);
    }

    public void saveReminder(View view) {
        try {
            Task task = createTask();
            ActionRequest req = new ActionRequest();
            req.setActionName("addReminderAction");
            req.setProperty("TASK", task);
            req.setProperty("TASKTYPE", "Reminder");
            ActionResponse resp = new AddReminderAction().execute(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                super.finish();
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - " + e.getMessage()),
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private Task createTask() throws Exception {
        String name = "Basic task";
        Task t = new BasicTask(name);

        Schedule s = getSchedule();

        t.setSchedule(s);

        return t;
    }

    private Schedule getSchedule() throws Exception {
        Calendar c = Calendar.getInstance();
        Date s = c.getTime();
        c.add(Calendar.MONDAY, 2);
        Date e = c.getTime();

        return getDailySchedule(s, e);
    }

    private Schedule getDailySchedule(Date st, Date en) throws Exception {
        BasicSchedule s = new BasicSchedule(st, en);
        Integer val = Integer.parseInt("2");
        s.setRepeatType(RepeatType.DATE, val);

        return s;
    }
}
