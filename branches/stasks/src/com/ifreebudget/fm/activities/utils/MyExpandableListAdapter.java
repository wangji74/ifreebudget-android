package com.ifreebudget.fm.activities.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.TransactionListActivity;
import com.ifreebudget.fm.activities.TxHolder;
import com.ifreebudget.fm.services.SessionManager;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Key> groups;
    private Map<Key, List<TxHolder>> entries;
    private int count = 0;
    private String TAG = "MyExpandableListAdapter";
    private View lastEditCtrlPanel = null;
    private TxHolder lastSelected = null;

    public MyExpandableListAdapter(Context context) {
        this.context = context;
        groups = new ArrayList<Key>();
        entries = new LinkedHashMap<Key, List<TxHolder>>();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Object obj = getGroup(groupPosition);
        List<TxHolder> list = entries.get(obj);
        return list.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        TxHolder child = (TxHolder) getChild(groupPosition, childPosition);
        return child.getTx().getTxId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        View v = null;
        final TxHolder item = (TxHolder) getChild(groupPosition, childPosition);

        if (convertView == null) {
            // Log.i(TAG, "ConvertView: null");
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.tx_item_view, null);
        }
        else {
            // Log.i(TAG, "ConvertView: not null, reused view");
            v = convertView;
        }

        initializeTxItemLayout(item, v);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleLastEditCtrlPanel(v, item);
            }
        });

        ImageButton reminderBtn = (ImageButton) v
                .findViewById(R.id.reminder_tx_btn);
        reminderBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TransactionListActivity acty = (TransactionListActivity) context;
                acty.addReminder(item);
            }
        });

        ImageButton editBtn = (ImageButton) v.findViewById(R.id.edit_tx_btn);
        editBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TransactionListActivity acty = (TransactionListActivity) context;
                acty.editTransaction(item);
            }
        });

        ImageButton delBtn = (ImageButton) v.findViewById(R.id.del_tx_btn);
        delBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TransactionListActivity acty = (TransactionListActivity) context;
                acty.deleteTransaction(item);
            }
        });
        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Object obj = getGroup(groupPosition);
        List<TxHolder> list = entries.get(obj);
        return list.size();
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
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {

        TextView tv = null;
        if (convertView == null) {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 80);

            tv = new TextView(this.context);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            tv.setPadding(60, 0, 5, 0);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(Color.BLACK);
            int color = context.getResources().getColor(R.color.tx_grp_bg);
            tv.setBackgroundColor(color);
        }
        else {
            tv = (TextView) convertView;
        }

        if (groupPosition < groups.size()) {
            // Calendar key = (Calendar) groups.get(groupPosition);
            Object key = groups.get(groupPosition);
            int numChildren = this.getChildrenCount(groupPosition);
            // SimpleDateFormat sdf = SessionManager.getDateFormat();
            if (numChildren > 1) {
                // tv.setText(sdf.format(key.getTime()) + " (" + numChildren +
                // ")");
                tv.setText(key.toString());
            }
            else {
                // tv.setText(sdf.format(key.getTime()));
            }
        }
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

    /* Helper methods */
    private void toggleLastEditCtrlPanel(View v, TxHolder lastSelected) {
        this.lastSelected = lastSelected;
        if (lastEditCtrlPanel != null) {
            TranslateAnimation slide = new TranslateAnimation(0, 0, 0, 100);
            slide.setDuration(300);
            slide.setFillAfter(true);
            slide.setInterpolator(new AccelerateInterpolator());
            lastEditCtrlPanel.startAnimation(slide);
            lastEditCtrlPanel.setVisibility(View.GONE);
        }
        lastEditCtrlPanel = (View) v.findViewById(R.id.edit_tx_ctrl_panel);
        TranslateAnimation slide = new TranslateAnimation(0, 0, 100, 0);
        slide.setDuration(300);
        slide.setFillAfter(true);
        slide.setInterpolator(new AccelerateInterpolator());
        lastEditCtrlPanel.startAnimation(slide);
        lastEditCtrlPanel.setVisibility(View.VISIBLE);
    }

    private void initializeTxItemLayout(TxHolder item, View v) {
        if (item.getIconResource() != 0) {
            ImageView iv = (ImageView) v.findViewById(R.id.tx_icon);
            iv.setImageResource(item.getIconResource());
            iv.setAdjustViewBounds(true);
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
                .getCurrencyLocale());

        Spanned txDetails = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v.findViewById(R.id.tx_details_lbl);
        txDetailsView.setText(txDetails);

        String txAmt = nf.format(item.getTx().getTxAmount());
        TextView txAmtView = (TextView) v.findViewById(R.id.tx_amt_lbl);
        txAmtView.setText(txAmt);
    }

    public void addItem(TxHolder holder) {
        // Object key = holder.getKey();
        Key key = new Key(holder);
        List<TxHolder> list = entries.get(key);
        if (list == null) {
            list = new ArrayList<TxHolder>();
            entries.put(key, list);
            groups.add(key);
        }
        list.add(holder);
        // Log.i(TAG,
        // "Item added: " + groups.size() + ", entries: " + entries.size());
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return count;
    }

    public void clear() {
        count = 0;
        groups.clear();
        entries.clear();
    }

    public void removeEntry(TxHolder entry) {
        Object cal = entry.getKey();
        if (cal == null) {
            return;
        }
        List<TxHolder> list = entries.get(cal);
        if (list != null) {
            Iterator<TxHolder> it = list.iterator();
            boolean mod = false;
            while (it.hasNext()) {
                if (it.next() == entry) {
                    it.remove();
                    mod = true;
                    break;
                }
            }
            if (list.size() == 0) {
                groups.remove(cal);
                entries.remove(cal);
                mod = true;
            }
            if (mod) {
                super.notifyDataSetChanged();
            }
        }
    }

    public TxHolder getLastSelected() {
        return lastSelected;
    }

    class Key {
        Object id;
        String display;

        Key(TxHolder holder) {
            id = holder.getKey();
            display = holder.getKeyDisplay();
        }
        
        @Override
        public String toString() {
            return display;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            }
            else if (!id.equals(other.id))
                return false;
            return true;
        }

        private MyExpandableListAdapter getOuterType() {
            return MyExpandableListAdapter.this;
        }
    }
}