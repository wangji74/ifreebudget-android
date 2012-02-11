package com.ifreebudget.rmapp.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.BasicSchedule;
import com.ifreebudget.fm.scheduler.task.BasicTask;
import com.ifreebudget.fm.scheduler.task.MonthSchedule;
import com.ifreebudget.fm.scheduler.task.MonthScheduleDayBased;
import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Schedule.DayOfWeek;
import com.ifreebudget.fm.scheduler.task.Schedule.RepeatType;
import com.ifreebudget.fm.scheduler.task.Schedule.WeekOfMonth;
import com.ifreebudget.fm.scheduler.task.Task;
import com.ifreebudget.fm.scheduler.task.WeekSchedule;
import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraintDayBased;
import com.ifreebudget.fm.scheduler.task.constraints.WeekConstraint;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.ReminderAppActivity;
import com.ifreebudget.rmapp.actions.AddReminderAction;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;
import com.ifreebudget.rmapp.task.STaskAlarmReceiver;

public class AddReminderActivity extends Activity {

    private static final String TAG = "RMApp.AddReminderActivity";
    public static final String TASK_ALARM_ID = "rmapp-st-id";
    public static final String TASKID = "TASKID";

    private Button startDtBtn, endDtBtn, startTimeBtn, endTimeBtn,
            repeatUnitBtn;
    private EditText rem_title_tf;
    private TextView repeatsLbl;

    private Spinner repeatTypeSpinner;

    private static final int ST_TIME_DIALOG = 0;
    private static final int EN_TIME_DIALOG = 1;
    private static final int ST_DATE_DIALOG = 2;
    private static final int EN_DATE_DIALOG = 3;
    private static final int REP_UNIT_DIALOG = 4;

    private static final String TIME_FORMAT = "hh:mm a";

    private boolean editMode = false;

    private Long taskToEdit = null;

    private enum TASK_TYPE_ENUM {
        once("once"), hourly("hours"), daily("days"), weekly("weeks"), monthly(
                "months"), monthly_by_week("months");

        String val;

        private TASK_TYPE_ENUM(String val) {
            this.val = val;
        }
    };

    private TASK_TYPE_ENUM taskType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_reminder_layout);

        Intent intent = this.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(TASKID)) {
                taskToEdit = (Long) bundle.get(TASKID);
                editMode = true;
            }
        }
        initializeFields();
    }

    @Override
    public void onResume() {
        super.onResume();
        repeatTypeSpinner.setSelection(2);
    }

    private void initializeFields() {
        repeatsLbl = (TextView) AddReminderActivity.this
                .findViewById(R.id.repeats_unit_lbl);

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

        repeatUnitBtn = (Button) findViewById(R.id.repeat_unit_btn);
        repeatUnitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(REP_UNIT_DIALOG);
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

        repeatTypeSpinner = (Spinner) findViewById(R.id.sch_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.repeats_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatTypeSpinner.setAdapter(adapter);
        repeatTypeSpinner.setSelection(2);
        repeatTypeSpinner
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view, int pos, long id) {

                        if (view == null) {
                            return;
                        }
                        if (pos == 0) {
                            repeatsLbl.setVisibility(View.GONE);
                            repeatUnitBtn.setVisibility(View.GONE);
                        }
                        else {
                            repeatsLbl.setVisibility(View.VISIBLE);
                            repeatUnitBtn.setVisibility(View.VISIBLE);
                        }
                        switch (pos) {
                        case 0:
                            taskType = TASK_TYPE_ENUM.once;
                            setRepeatsView(view, -1);
                            break;
                        case 1:
                            taskType = TASK_TYPE_ENUM.hourly;
                            setRepeatsView(view, R.layout.hourly_repeat_layout);
                            break;
                        case 2:
                            taskType = TASK_TYPE_ENUM.daily;
                            setRepeatsView(view, R.layout.daily_repeat_layout);
                            break;
                        case 3:
                            taskType = TASK_TYPE_ENUM.weekly;
                            setRepeatsView(view, R.layout.weekly_repeat_layout);
                            break;
                        case 4:
                            taskType = TASK_TYPE_ENUM.monthly;
                            setRepeatsView(view, R.layout.monthly_repeat_layout);
                            break;
                        case 5:
                            taskType = TASK_TYPE_ENUM.monthly_by_week;
                            setRepeatsView(view,
                                    R.layout.monthly_by_week_repat_layout);
                            break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
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
        case REP_UNIT_DIALOG:
            return getRepeatUnitDialog();
        }
        return null;
    }

    private Dialog getRepeatUnitDialog() {
        Log.i(TAG, "Task type: " + taskType);
        if (taskType == null) {
            return null;
        }
        final SpinnerDialog d = new SpinnerDialog(this, 1, 60, taskType.val);
        d.setTitle("Repeats");
        return d;
    }

    private void setRepeatDialogResult(int value) {
        repeatUnitBtn.setText(String.valueOf(value));
    }

    private DatePickerDialog getDatePickerDialog(
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

    private TimePickerDialog getTimePickerDialog(
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

        if (layoutId != -1) {
            View vv = li.inflate(layoutId, null);
            initRepeatsLayout(vv, layoutId);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.repeat_type_panel);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);

            ll.removeAllViews();

            ll.addView(vv, params);
        }
        else {
            ll.removeAllViews();
        }
    }

    private void initRepeatsLayout(View v, int layoutId) {
        if (layoutId == R.layout.monthly_by_week_repat_layout) {
            Calendar today = Calendar.getInstance();

            Spinner weekNumSpinner = (Spinner) v
                    .findViewById(R.id.week_num_spinner);

            ArrayAdapter<CharSequence> weekNumAdapter = ArrayAdapter
                    .createFromResource(this, R.array.week_num,
                            android.R.layout.simple_spinner_item);

            weekNumAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            weekNumSpinner.setAdapter(weekNumAdapter);

            int weekNum = today.get(Calendar.WEEK_OF_MONTH) - 1;
            if (weekNum < weekNumAdapter.getCount()) {
                weekNumSpinner.setSelection(weekNum);
            }

            Spinner dowSpinner = (Spinner) v.findViewById(R.id.dow_spinner);

            ArrayAdapter<CharSequence> dowAdapter = ArrayAdapter
                    .createFromResource(this, R.array.days_of_week,
                            android.R.layout.simple_spinner_item);
            dowAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dowSpinner.setAdapter(dowAdapter);

            int dow = today.get(Calendar.DAY_OF_WEEK) - 1;
            if (dow < dowAdapter.getCount()) {
                dowSpinner.setSelection(dow);
            }
        }
        else if (layoutId == R.layout.monthly_repeat_layout) {
            Calendar today = Calendar.getInstance();

            int dayInMonth = today.get(Calendar.DAY_OF_MONTH);
            EditText dayOfMonthField = (EditText) v
                    .findViewById(R.id.monthly_repeat_dom_tf);
            dayOfMonthField.setText(String.valueOf(dayInMonth));
        }
        else if (layoutId == R.layout.weekly_repeat_layout) {
            Calendar today = Calendar.getInstance();
            int dow = today.get(Calendar.DAY_OF_WEEK);
            CheckBox cb = null;
            switch (dow) {
            case Calendar.SUNDAY:
                cb = (CheckBox) v.findViewById(R.id.cbSun);
                break;
            case Calendar.MONDAY:
                cb = (CheckBox) v.findViewById(R.id.cbMon);
                break;
            case Calendar.TUESDAY:
                cb = (CheckBox) v.findViewById(R.id.cbTue);
                break;
            case Calendar.WEDNESDAY:
                cb = (CheckBox) v.findViewById(R.id.cbWed);
                break;
            case Calendar.THURSDAY:
                cb = (CheckBox) v.findViewById(R.id.cbThu);
                break;
            case Calendar.FRIDAY:
                cb = (CheckBox) v.findViewById(R.id.cbFri);
                break;
            case Calendar.SATURDAY:
                cb = (CheckBox) v.findViewById(R.id.cbSat);
                break;
            default:
                cb = (CheckBox) v.findViewById(R.id.cbSun);
                break;
            }
            cb.setChecked(true);
        }
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
                    "Cannot create task - " + e.getMessage(),
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

    private String getRepeatUnit() throws Exception {
        String txt = repeatUnitBtn.getText().toString();
        return txt;
    }

    private Task createTask() throws Exception {
        if (!validate()) {
            return null;
        }
        String name = rem_title_tf.getText().toString();

        Task t = new BasicTask(name);

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
                    ("Cannot create task - Name is required"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        String s = e.toString().trim();
        if (s.length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    ("Cannot create task - Name is required"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if (s.length() > 30) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    ("Name too long, max allowed 30 characters"),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    private boolean validateDates(Date s, Date e) {
        if (e.before(s)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Cannot create task - End date is before start date",
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
        if (taskType == TASK_TYPE_ENUM.once) {
            return getOnceSchedule(s);
        }

        String repeatUnit = getRepeatUnit();

        if (taskType == TASK_TYPE_ENUM.hourly) {
            return getHourlySchedule(s, e, repeatUnit);
        }
        else if (taskType == TASK_TYPE_ENUM.daily) {
            return getDailySchedule(s, e, repeatUnit);
        }
        else if (taskType == TASK_TYPE_ENUM.weekly) {
            return getWeeklySchedule(s, e, repeatUnit);
        }
        else if (taskType == TASK_TYPE_ENUM.monthly) {
            return getDatebasedMonthlySchedule(s, e, repeatUnit);
        }
        else if (taskType == TASK_TYPE_ENUM.monthly_by_week) {
            return getDaybasedMonthlySchedule(s, e, repeatUnit);
        }
        return null;
    }

    private Schedule getOnceSchedule(Date st) throws Exception {
        BasicSchedule s = new BasicSchedule(st, st);

        s.setRepeatType(RepeatType.HOUR, 1);

        return s;
    }

    private Schedule getHourlySchedule(Date st, Date en, String repeatUnit)
            throws Exception {
        BasicSchedule s = new BasicSchedule(st, en);

        int step = validateStepValue(repeatUnit);
        if (step < 1) {
            return null;
        }
        s.setRepeatType(RepeatType.HOUR, step);

        return s;
    }

    private Schedule getDailySchedule(Date st, Date en, String repeatUnit)
            throws Exception {

        BasicSchedule s = new BasicSchedule(st, en);

        int step = validateStepValue(repeatUnit);
        if (step < 1) {
            return null;
        }
        s.setRepeatType(RepeatType.DATE, step);

        return s;
    }

    private Schedule getWeeklySchedule(Date st, Date en, String repeatUnit)
            throws Exception {
        int step = validateStepValue(repeatUnit);
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

    private Schedule getDaybasedMonthlySchedule(Date st, Date en,
            String repeatUnit) throws Exception {
        int[] repeatRange = { 1, 12 };
        int repeatVal = -1;
        try {
            repeatVal = validateIntValue(repeatUnit, repeatRange);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Invalid value for repeat interval", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        Spinner weekNumSpinner = (Spinner) findViewById(R.id.week_num_spinner);
        Spinner dowSpinner = (Spinner) findViewById(R.id.dow_spinner);

        String weekNum = (String) weekNumSpinner.getSelectedItem();
        String dowVal = (String) dowSpinner.getSelectedItem();

        MonthScheduleDayBased s = new MonthScheduleDayBased(st, en);

        WeekOfMonth wom = WeekOfMonth.valueOf(weekNum);

        DayOfWeek dow = DayOfWeek.valueOf(dowVal);

        Constraint co = new MonthConstraintDayBased(wom, dow);

        s.setRepeatType(RepeatType.MONTH, repeatVal);

        s.setConstraint(co);

        return s;
    }

    private Schedule getDatebasedMonthlySchedule(Date st, Date en,
            String repeatUnit) throws Exception {
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
                    "Invalid value for repeat interval", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        int[] repeatRange = { 1, 12 };
        int repeatVal = -1;
        try {
            repeatVal = validateIntValue(repeatUnit, repeatRange);
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
                Toast toast = Toast
                        .makeText(getApplicationContext(),
                                "Invalid value for repeat interval",
                                Toast.LENGTH_SHORT);
                toast.show();
                return -1;
            }
        }
        if (step < 1) {
            Toast toast = Toast
                    .makeText(
                            getApplicationContext(),
                            "Invalid value for repeat interval, can not be less than 1",
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

        RMAppEntityManager em = RMAppEntityManager.getInstance(context);

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

        Log.d(debugTag,
                "Scheduled event to fire at: " + timeToSchedule.toString());
        manager.set(AlarmManager.RTC_WAKEUP, timeToSchedule.getTime(), sender);
    }

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, ReminderAppActivity.class);
        startActivity(intent);
    }

    class SpinnerDialog extends Dialog {
        int low;
        int high;
        int curr;
        String postTitle;

        Button ok;
        ImageButton incr, decr;
        EditText txtField;
        TextView postTitleTf;

        public SpinnerDialog(Context context, int low, int high,
                String postTitle) {
            super(context);
            this.high = Math.max(low, high);
            this.low = Math.min(low, high);
            curr = low;
            this.postTitle = postTitle;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.i(TAG, "Dialog onCreate");
            setContentView(R.layout.spinner_layout);
            this.txtField = (EditText) this.findViewById(R.id.spinner_value_tf);
            this.postTitleTf = (TextView) this.findViewById(R.id.post_lbl);

            this.incr = (ImageButton) findViewById(R.id.increment_btn);
            incr.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSpinnerValue(++curr);
                }
            });
            this.decr = (ImageButton) this.findViewById(R.id.decrement_btn);
            decr.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSpinnerValue(--curr);
                }
            });

            this.ok = (Button) this.findViewById(R.id.ok_btn);
            ok.setOnClickListener(new android.view.View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        int tfValue = Integer.parseInt(txtField.getText()
                                .toString());
                        if (tfValue != curr) {
                            setSpinnerValue(tfValue);
                        }
                    }
                    catch (NumberFormatException e) {
                        // ignore;
                    }
                    AddReminderActivity.this.setRepeatDialogResult(curr);
                    SpinnerDialog.this.dismiss();
                }
            });

            setSpinnerValue(curr);
            this.postTitleTf.setText(postTitle);
        }

        @Override
        public void show() {
            super.show();
            this.postTitleTf.setText(taskType.val);
            this.curr = low;
            setSpinnerValue(curr);
        }

        private void setSpinnerValue(int value) {
            if (value < low) {
                value = low;
            }
            else if (value > high) {
                value = high;
            }
            txtField.setText(String.valueOf(value));
            curr = value;
        }
    }
}
