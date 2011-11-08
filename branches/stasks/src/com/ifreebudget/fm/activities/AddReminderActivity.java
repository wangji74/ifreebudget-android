package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddReminderAction;
import com.ifreebudget.fm.actions.GetBudgetSummaryAction;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.BasicSchedule;
import com.ifreebudget.fm.scheduler.task.MonthSchedule;
import com.ifreebudget.fm.scheduler.task.STaskAlarmReceiver;
import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Schedule.DayOfWeek;
import com.ifreebudget.fm.scheduler.task.Schedule.RepeatType;
import com.ifreebudget.fm.scheduler.task.ScheduledTx;
import com.ifreebudget.fm.scheduler.task.Task;
import com.ifreebudget.fm.scheduler.task.WeekSchedule;
import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraint;
import com.ifreebudget.fm.scheduler.task.constraints.WeekConstraint;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddReminderActivity extends Activity {

    private static final String TAG = "AddReminderActivity";
    public static final String TASK_ALARM_ID = "ifb-st-id";

    private Button startDtBtn, endDtBtn, startTimeBtn, endTimeBtn;
    private RadioButton dailyBtn, weeklyBtn, monthlyBtn;
    private EditText rem_title_tf;

    private static final int ST_TIME_DIALOG = 0;
    private static final int EN_TIME_DIALOG = 1;
    private static final int ST_DATE_DIALOG = 2;
    private static final int EN_DATE_DIALOG = 3;

    private static final String TIME_FORMAT = "hh:mm a";

    private enum TASK_TYPE_ENUM {
        daily, weekly, monthly
    };

    private TASK_TYPE_ENUM taskType;

    private Long txId = 0l;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_reminder_layout);

        Intent intent = this.getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null
                    && bundle.containsKey(UpdateTransactionActivity.TXID)) {
                txId = (Long) bundle.get(UpdateTransactionActivity.TXID);
            }
        }

        startDtBtn = (Button) findViewById(R.id.start_date_btn);
        startDtBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ST_DATE_DIALOG);
            }
        });

        endDtBtn = (Button) findViewById(R.id.end_date_btn);
        endDtBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(EN_DATE_DIALOG);
            }
        });

        startTimeBtn = (Button) findViewById(R.id.start_time_btn);
        startTimeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ST_TIME_DIALOG);
            }
        });

        endTimeBtn = (Button) findViewById(R.id.end_time_btn);
        endTimeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(EN_TIME_DIALOG);
            }
        });

        rem_title_tf = (EditText) findViewById(R.id.rem_title_tf);

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
                taskType = TASK_TYPE_ENUM.daily;
                setRepeatsView(v, R.layout.daily_repeat_layout);
            }
        });

        weeklyBtn = (RadioButton) findViewById(R.id.weekly_btn);
        weeklyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskType = TASK_TYPE_ENUM.weekly;
                setRepeatsView(v, R.layout.weekly_repeat_layout);
            }
        });

        monthlyBtn = (RadioButton) findViewById(R.id.monthly_btn);
        monthlyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskType = TASK_TYPE_ENUM.monthly;
                setRepeatsView(v, R.layout.monthly_repeat_layout);
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        String txt = null;
        switch (id) {
        case ST_DATE_DIALOG:
            txt = startDtBtn.getText().toString();
            return getDatePickerDialog(stDatePickerListener, txt);
        case EN_DATE_DIALOG:
            txt = endDtBtn.getText().toString();
            return getDatePickerDialog(enDatePickerListener, txt);
        case ST_TIME_DIALOG:
            txt = startTimeBtn.getText().toString();
            return getTimePickerDialog(stTimePickerListener, txt);
        case EN_TIME_DIALOG:
            txt = endTimeBtn.getText().toString();
            return getTimePickerDialog(enTimePickerListener, txt);
        }
        return null;
    }

    public DatePickerDialog getDatePickerDialog(
            DatePickerDialog.OnDateSetListener listener, String txt) {
        SimpleDateFormat sdf = SessionManager.getDateFormat();
        int yr = 2011;
        int mo = 10;
        int dt = 27;
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(txt));
            yr = c.get(Calendar.YEAR);
            mo = c.get(Calendar.MONTH);
            dt = c.get(Calendar.DATE);
        }
        catch (Exception e) {
            Log.e(TAG, "Error parsing time for start time dialog: " + txt);
        }
        return new DatePickerDialog(this, listener, yr, mo, dt);
    }

    public TimePickerDialog getTimePickerDialog(
            TimePickerDialog.OnTimeSetListener listener, String txt) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        int hr = 12;
        int min = 0;
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(txt));
            hr = c.get(Calendar.HOUR_OF_DAY);
            min = c.get(Calendar.MINUTE);
        }
        catch (Exception e) {
            Log.e(TAG, "Error parsing time for start time dialog: " + txt);
        }
        return new TimePickerDialog(this, listener, hr, min, false);
    }

    private void updateTimeDisplay(Button view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        view.setText(sdf.format(c.getTime()));
    }

    private void updateDateDisplay(Button view, int year, int month, int date) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DATE, date);

        SimpleDateFormat sdf = SessionManager.getDateFormat();
        view.setText(sdf.format(c.getTime()));
    }

    private TimePickerDialog.OnTimeSetListener stTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            updateTimeDisplay(startTimeBtn, hourOfDay, minute);
        }
    };

    private TimePickerDialog.OnTimeSetListener enTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            updateTimeDisplay(endTimeBtn, hourOfDay, minute);
        }
    };

    private DatePickerDialog.OnDateSetListener stDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            updateDateDisplay(startDtBtn, year, monthOfYear, dayOfMonth);
        }
    };

    private DatePickerDialog.OnDateSetListener enDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            updateDateDisplay(endDtBtn, year, monthOfYear, dayOfMonth);
        }
    };

    private void setRepeatsView(View v, int layoutId) {
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
            if (task == null) {
                return;
            }
            ActionRequest req = new ActionRequest();
            req.setActionName("addReminderAction");
            req.setProperty("TASK", task);
            req.setProperty("TASKTYPE", "Reminder");
            ActionResponse resp = new AddReminderAction().execute(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                reRegisterAlarm(TAG, am, getApplicationContext());
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
        Date dt = fmt.parse(dateSt + " " + timeSt);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private Date getEndTime() throws Exception {
        String dateSt = endDtBtn.getText().toString();
        String timeSt = endTimeBtn.getText().toString();

        SimpleDateFormat fmt = SessionManager.getDateTimeFormat();
        Date dt = fmt.parse(dateSt + " " + timeSt);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private Task createTask() throws Exception {
        if (!validate()) {
            return null;
        }
        String name = rem_title_tf.getText().toString();

        Task t = new ScheduledTx(name, txId);

        Schedule s = getSchedule();

        if (s == null) {
            return null;
        }

        t.setSchedule(s);

        return t;
    }

    private boolean validate() {
        Editable e = rem_title_tf.getText();
        if (e == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - Name is required"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        String s = e.toString().trim();
        if (s.length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - Name is required"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if (s.length() > 30) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Name too long, max allowed 30 characters"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    private boolean validateDates(Date s, Date e) {
        Date now = new Date();
        if (s.before(now)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Cannot create task - Start time is past"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

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
        if (taskType == TASK_TYPE_ENUM.daily) {
            return getDailySchedule(s, e);
        }
        else if (taskType == TASK_TYPE_ENUM.weekly) {
            return getWeeklySchedule(s, e);
        }
        else if (taskType == TASK_TYPE_ENUM.monthly) {
            return getMonthlySchedule(s, e);
        }
        return null;
    }

    private Schedule getDailySchedule(Date st, Date en) throws Exception {
        EditText repInfo = (EditText) findViewById(R.id.daily_repeat_unit_tf);
        String val = repInfo.getText().toString();

        BasicSchedule s = new BasicSchedule(st, en);

        int step = validateStepValue(val);
        if (step < 1) {
            return null;
        }
        s.setRepeatType(RepeatType.DATE, step);

        return s;
    }

    private Schedule getWeeklySchedule(Date st, Date en) throws Exception {
        EditText repInfo = (EditText) findViewById(R.id.weekly_repeat_unit_tf);
        String val = repInfo.getText().toString();

        int step = validateStepValue(val);
        if (step < 1) {
            return null;
        }

        WeekSchedule s = new WeekSchedule(st, en);

        WeekConstraint co = new WeekConstraint();

        boolean daySelected = false;

        CheckBox cbox = (CheckBox) findViewById(R.id.cbSun);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Sunday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbMon);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Monday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbTue);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Tuesday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbWed);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Wednesday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbThu);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Thursday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbFri);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Friday);
            daySelected = true;
        }
        cbox = (CheckBox) findViewById(R.id.cbSat);
        if (cbox.isChecked()) {
            co.addDay(DayOfWeek.Saturday);
            daySelected = true;
        }

        if (!daySelected) {
            Toast toast = Toast
                    .makeText(
                            getApplicationContext(),
                            "Can not create task - One or more day of week must be selected.",
                            Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
        s.setRepeatType(RepeatType.WEEK, step);
        s.setConstraint(co);

        return s;
    }

    private Schedule getMonthlySchedule(Date st, Date en) throws Exception {
        EditText domInfo = (EditText) findViewById(R.id.monthly_repeat_dom_tf);

        int[] domRange = { 1, 31 };
        int dayOfMonth = -1;
        try {
            dayOfMonth = validateIntValue(domInfo.getText().toString(),
                    domRange);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        EditText repInfo = (EditText) findViewById(R.id.monthly_repeat_unit);

        int[] repeatRange = { 1, 12 };
        int repeatVal = -1;
        try {
            repeatVal = validateIntValue(repInfo.getText().toString(),
                    repeatRange);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        MonthSchedule s = new MonthSchedule(st, en);
        Constraint co = new MonthConstraint(dayOfMonth);

        s.setRepeatType(RepeatType.MONTH, repeatVal);
        s.setConstraint(co);
        return s;
    }

    private int validateStepValue(String val) {
        int step = 1;
        if (val != null) {
            try {
                step = Integer.parseInt(val);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "Unparseable step value for daily schedule: " + val);
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("Invalid value for repeat interval"),
                        Toast.LENGTH_SHORT);
                toast.show();
                return -1;
            }
        }
        if (step < 1) {
            Toast toast = Toast
                    .makeText(
                            getApplicationContext(),
                            tr("Invalid value for repeat interval, can not be less than 1"),
                            Toast.LENGTH_SHORT);
            toast.show();
            return -1;
        }

        return step;
    }

    private int validateIntValue(String val, int... range) throws Exception {
        int ret = -1;
        ret = Integer.parseInt(val);
        if (range != null) {
            if (range.length == 2) {
                int min = Math.min(range[0], range[1]);
                int max = Math.max(range[0], range[1]);

                if (ret >= min && ret <= max) {
                    return ret;
                }
                String s = String.format(
                        "Value %d outside valid range %d : %d", ret, range[0],
                        range[1]);
                throw new RuntimeException(s);
            }
            throw new RuntimeException("Invalid range");
        }
        return ret;
    }

    public static void reRegisterAlarm(String debugTag, AlarmManager manager,
            Context context) throws Exception {

        FManEntityManager em = FManEntityManager.getInstance(context);

        List<FManEntity> schList = em.getList(ScheduleEntity.class,
                " order by nextrt ");

        Date now = new Date();
        Date timeToSchedule = null;
        for (FManEntity e : schList) {
            ScheduleEntity se = (ScheduleEntity) e;
            TaskEntity te = em.getTask(se.getScheduledTaskId());
            Date end = new Date(te.getEndTime());
            Date next = new Date(se.getNextRunTime());

            if (end.before(now)) {
                continue;
            }
            else if (next.before(now)) {
                continue;
            }
            timeToSchedule = next;
            break;
        }

        if (timeToSchedule == null) {
            Log.e(debugTag, "Unable to get timeToSchedule, num tasks:"
                    + schList.size());
            return;
        }

        Intent intent = new Intent(context, STaskAlarmReceiver.class);
        intent.putExtra(TASK_ALARM_ID, timeToSchedule.getTime());

        PendingIntent sender = PendingIntent.getBroadcast(context,
                Short.MAX_VALUE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i(debugTag,
                "Scheduled event to fire at: " + timeToSchedule.toString());
        manager.set(AlarmManager.RTC_WAKEUP, timeToSchedule.getTime(), sender);
    }
}
