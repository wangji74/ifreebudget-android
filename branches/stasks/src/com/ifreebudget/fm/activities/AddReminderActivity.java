package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.text.ParseException;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddReminderAction;
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

    private enum TASK_TYPE_ENUM {
        daily, weekly
    };

    private TASK_TYPE_ENUM taskType;

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
                taskType = TASK_TYPE_ENUM.daily;
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

    private Date getStartTime() throws Exception {
        String dateSt = startDtBtn.getText().toString();
        String timeSt = startTimeBtn.getText().toString();

        SimpleDateFormat fmt = SessionManager.getDateTimeFormat();
        return fmt.parse(dateSt + " " + timeSt);
    }

    private Date getEndTime() throws Exception {
        String dateSt = endDtBtn.getText().toString();
        String timeSt = endTimeBtn.getText().toString();

        SimpleDateFormat fmt = SessionManager.getDateTimeFormat();
        return fmt.parse(dateSt + " " + timeSt);
    }

    private Task createTask() throws Exception {
        String name = "Basic task";
        Task t = new BasicTask(name);

        Schedule s = getSchedule();

        if (s == null) {
            return null;
        }

        t.setSchedule(s);

        return t;
    }

    private boolean validateDates(Date s, Date e) {
        if (e.before(s)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - End date is before start date"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else if (e.equals(s)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - Start and end dates are same"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    private Schedule getSchedule() throws Exception {
        Date s = getStartTime();
        Date e = getEndTime();

        if (!validateDates(s, e)) {
            return null;
        }
        return getDailySchedule(s, e);
    }

    private Schedule getDailySchedule(Date st, Date en) throws Exception {
        LinearLayout ll = (LinearLayout) findViewById(R.id.repeat_info_panel);
        View v = ll.getChildAt(0);

        EditText repInfo = (EditText) v.findViewById(R.id.daily_repeat_unit_tf);
        String val = repInfo.getText().toString();

        BasicSchedule s = new BasicSchedule(st, en);

        int step = 1;
        if (val == null) {
            try {
                step = Integer.parseInt(val);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "Unparseable step value for daily schedule: " + val);
            }
        }
        s.setRepeatType(RepeatType.DATE, step);

        return s;
    }
}
