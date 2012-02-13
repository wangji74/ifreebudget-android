package com.ifreebudget.rmapp.activities.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.services.SessionManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Calendar> groups;
    private Map<Calendar, List<ReminderListEntry>> entries;
    private View lastEditCtrlPanel = null;
    private ReminderListEntry lastSelected = null;

    private static final String TAG = "MyExpandableListAdapter";

    public MyExpandableListAdapter(Context context) {
        this.context = context;
        groups = new ArrayList<Calendar>();
        entries = new LinkedHashMap<Calendar, List<ReminderListEntry>>();
    }

    public void setData(List<ReminderListEntry> list) {
        groupItems(list);
        super.notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        Calendar cal = groups.get(groupPosition);
        List<ReminderListEntry> list = entries.get(cal);
        if (list == null || childPosition >= list.size()) {
            return null;
        }
        return list.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        ReminderListEntry entry = (ReminderListEntry) getChild(groupPosition,
                childPosition);
        if (entry == null) {
            return 0l;
        }
        return entry.getEntity().getId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ReminderListEntry item = (ReminderListEntry) getChild(
                groupPosition, childPosition);
        View v = inflater.inflate(R.layout.reminder_list_row, null);
        initializeListEntryLayout(item, v);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleLastEditCtrlPanel(v, item);
            }
        });

        return v;
    }

    private void toggleLastEditCtrlPanel(View v, ReminderListEntry lastSelected) {
        this.lastSelected = lastSelected;
        if (lastEditCtrlPanel != null) {
            TranslateAnimation slide = new TranslateAnimation(0, 0, 0, 100);
            slide.setDuration(300);
            slide.setFillAfter(true);
            slide.setInterpolator(new AccelerateInterpolator());
            lastEditCtrlPanel.startAnimation(slide);
            lastEditCtrlPanel.setVisibility(View.GONE);
        }
        lastEditCtrlPanel = (View) v
                .findViewById(R.id.edit_reminder_ctrl_panel);
        TranslateAnimation slide = new TranslateAnimation(0, 0, 100, 0);
        slide.setDuration(300);
        slide.setFillAfter(true);
        slide.setInterpolator(new AccelerateInterpolator());
        lastEditCtrlPanel.startAnimation(slide);
        lastEditCtrlPanel.setVisibility(View.VISIBLE);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<ReminderListEntry> entries = getChildObjects(groupPosition);
        if (entries == null) {
            return 0;
        }
        return entries.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return 0;
        }
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, 64);

        TextView tv = new TextView(this.context);
        tv.setLayoutParams(lp);

        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        tv.setPadding(60, 0, 5, 0);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        if (groupPosition < groups.size()) {
            Calendar cal = groups.get(groupPosition);
            SimpleDateFormat sdf = SessionManager.getDateFormat();
            tv.setText(sdf.format(cal.getTime()));
        }
        int color = context.getResources().getColor(R.color.reminder_grp_bg);
        tv.setBackgroundColor(color);
        return tv;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private List<ReminderListEntry> getChildObjects(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        Calendar cal = groups.get(groupPosition);
        return entries.get(cal);
    }

    private void initializeListEntryLayout(ReminderListEntry item, View v) {
        Spanned details = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v
                .findViewById(R.id.reminder_details_lbl);
        txDetailsView.setText(details);
    }

    private void groupItems(List<ReminderListEntry> list) {
        entries.clear();
        groups.clear();
        for (ReminderListEntry e : list) {
            Date dt = e.getNextTime();
            Calendar cal = getCalendarKey(dt);
            List<ReminderListEntry> values = entries.get(cal);
            if (values == null) {
                values = new ArrayList<ReminderListEntry>();
                entries.put(cal, values);
                groups.add(cal);
            }
            values.add(e);
        }
        // printMap(entries);
    }

    private Calendar getCalendarKey(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);

        return cal;
    }

    private void printMap(Map<Calendar, List<ReminderListEntry>> map) {
        StringBuilder sb = new StringBuilder();
        for (Entry<Calendar, List<ReminderListEntry>> e : map.entrySet()) {
            sb.append(e.getKey().getTime());
            sb.append("\n");
            for (ReminderListEntry entry : e.getValue()) {
                sb.append("\t" + entry.getEntity().getName());
                sb.append("\n");
            }
        }
        Log.i(TAG, sb.toString());
    }

    public ReminderListEntry getLastSelected() {
        return lastSelected;
    }

    public void removeEntry(ReminderListEntry entry) {
        Calendar cal = getCalendarKey(entry.getNextTime());
        if (cal == null) {
            return;
        }
        List<ReminderListEntry> list = entries.get(cal);
        if (list != null) {
            Iterator<ReminderListEntry> it = list.iterator();
            boolean mod = false;
            while (it.hasNext()) {
                if (it.next() == entry) {
                    it.remove();
                    mod = true;
                    break;
                }
            }
            if(list.size() == 0) {
                groups.remove(cal);
                entries.remove(cal);
                mod = true;
            }
            if (mod) {
                super.notifyDataSetChanged();
            }
        }
    }
}
