package com.ifreebudget.fm.activities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.ListTransactionsActivity.TxHolder;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.CategoryIconMap;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.search.newfilter.Filter;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils.DATE_RANGE;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class AccountsActivity extends Activity {
    private GridView gallery;
    private GalleryAdapter adapter;
    private final NumberFormat nf = NumberFormat
            .getCurrencyInstance(SessionManager.getCurrencyLocale());

    private ListView listView;
    private ArrayAdapter<TxHolder> txListAdapter;
    private final Handler txListUpdateHandler = new Handler();

    private FilterBuilderUtils filterUtil;

    private Button filterButton;
    private ImageButton upBtn;

    private TextView categoryPathTf = null;

    private long lastCategoryId = Long.valueOf(AccountTypes.ACCT_TYPE_ROOT);

    private static final int PAGE_SIZE = 25;
    private static final String TAG = "AccountsActivity";

    /* State variables */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.accounts_layout);

        filterUtil = new FilterBuilderUtils(this);

        adapter = new GalleryAdapter();
        gallery = (GridView) findViewById(R.id.acct_gallery);
        gallery.setAdapter(adapter);
        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                Object obj = parent.getAdapter().getItem(position);
                if (obj != null) {
                    if (obj instanceof AccountCategory) {
                        AccountCategory ac = (AccountCategory) obj;
                        lastCategoryId = ac.getParentCategoryId();
                        loadCategory(ac.getCategoryId());
                    }
                }
            }
        });

        txListAdapter = new MyArrayAdapter(this, R.layout.tx_list_layout);
        listView = (ListView) findViewById(R.id.list_panel);
        listView.setAdapter(txListAdapter);

        upBtn = (ImageButton) findViewById(R.id.up_btn);
        upBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCategory(lastCategoryId);
            }
        });

        categoryPathTf = (TextView) findViewById(R.id.category_path_lbl);
        filterButton = (Button) findViewById(R.id.filter_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategory(Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
    }

    private void loadCategory(long parentId) {
        try {
            List<FManEntity> catgs = FManEntityManager.getInstance()
                    .getChildren(parentId);
            Log.i(TAG, "..." + parentId + ", " + catgs.size());
            adapter.clear();
            for (FManEntity e : catgs) {
                adapter.add(e);
            }
            String categoryPath = getCategoryPath(parentId);
            if (categoryPath != null && categoryPath.length() > 0) {
                upBtn.setVisibility(View.VISIBLE);
            }
            else {
                upBtn.setVisibility(View.GONE);
            }
            categoryPathTf.setText(getCategoryPath(parentId));
            
            getIntent().putExtra(NewFilterUtils.FILTERKEY,
                    NewFilterUtils.CATEGORY_FILTER_TYPE);
            getIntent().putExtra(NewFilterUtils.FILTERVALUE, parentId);
            
            resetState();
            retrieveTxList(filterUtil.buildFilter(getIntent()));
        }
        catch (DBException e) {
            Log.i(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void resetState() {
        /* clear out the display */
        txListAdapter.clear();
        off.set(0);
        end.set(false);
    }

    public void showFilterSelector(View view) {
        Resources res = getResources();
        final CharSequence[] items = { res.getString(R.string.today_filter),
                res.getString(R.string.last_week_filter),
                res.getString(R.string.last_month_filter),
                res.getString(R.string.all_fltr) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select filter");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        filterUtil.setDateRange(item);
                        filterButton.setText(items[item]);
                        resetState();
                        retrieveTxList(filterUtil.buildFilter(getIntent()));
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void setFilterButtonText() {
        Resources res = getResources();

        String text = "Select";
        DATE_RANGE dateRange = filterUtil.getDateRange();
        if (dateRange != null) {
            switch (dateRange) {
            case Today:
                text = res.getString(R.string.today_filter);
                break;
            case LastWeek:
                text = res.getString(R.string.last_week_filter);
                break;
            case LastMonth:
                text = res.getString(R.string.last_month_filter);
                break;
            case All:
                text = res.getString(R.string.all_fltr);
            }
        }
        filterButton.setText(text);
    }

    private boolean retrieveInProgress = false;
    private final AtomicInteger off = new AtomicInteger(0);
    private final AtomicBoolean end = new AtomicBoolean(false);

    private void retrieveTxList(final Filter query) {
        if (end.get()) {
            return;
        }

        final FManEntityManager dbHelper = FManEntityManager.getInstance();
        Runnable r = new Runnable() {
            public void run() {
                List<FManEntity> catgs;
                retrieveInProgress = true;
                try {
                    try {
                        Log.i(TAG,
                                "Retr tx list: " + off.get() + "," + end.get());
                        String q = query.getQueryObject(false);
                        catgs = dbHelper.executeFilterQuery(q,
                                Transaction.class, off.get(), PAGE_SIZE);
                        Log.i(TAG, "Retrieved " + catgs.size());
                        off.addAndGet(catgs.size());
                        if (catgs.size() < PAGE_SIZE) {
                            end.set(true);
                        }
                    }
                    catch (Exception e1) {
                        Log.e(TAG, MiscUtils.stackTrace2String(e1));
                        catgs = dbHelper.getTransactions(0, 0);
                    }
                    for (FManEntity e : catgs) {
                        Transaction t = (Transaction) e;
                        final TxHolder tx = makeTxHolder(t);
                        txListUpdateHandler.post(new Runnable() {
                            public void run() {
                                addToUI(tx);
                            }
                        });
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
                finally {
                    retrieveInProgress = false;
                }
            }
        };

        new Thread(r).start();
    }

    private void addToUI(TxHolder tx) {
        txListAdapter.add(tx);
    }

    private TxHolder makeTxHolder(Transaction t) throws Exception {
        TxHolder tx = new TxHolder(t);
        FManEntityManager dbHelper = FManEntityManager.getInstance();
        Account from = dbHelper.getAccount(t.getFromAccountId());
        Account to = dbHelper.getAccount(t.getToAccountId());

        int iconRes = 0;
        if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
            iconRes = R.drawable.pending;
        }
        else {
            CategoryIconMap iconMap = dbHelper.getCategoryIconMap(from
                    .getCategoryId());
            if (iconMap == null) {
                iconMap = dbHelper.getCategoryIconMap(to.getCategoryId());
            }

            if (iconMap != null) {
                iconRes = getResources().getIdentifier(iconMap.getIconPath(),
                        "drawable", "com.ifreebudget.fm");
            }
            else {
                iconRes = R.drawable.blank;
            }
        }

        tx.fromAcct = from;
        tx.toAcct = to;
        tx.iconResource = iconRes;

        return tx;
    }

    class MyArrayAdapter extends ArrayAdapter<TxHolder> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TxHolder item = super.getItem(position);
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.tx_item_view, null);
            }
            initializeTxItemLayout(item, v);
            return v;
        }
    }

    private void initializeTxItemLayout(TxHolder item, View v) {
        if (item.iconResource != 0) {
            ImageView iv = (ImageView) v.findViewById(R.id.tx_icon);
            iv.setImageResource(item.iconResource);
            iv.setAdjustViewBounds(true);
        }
        Spanned txDetails = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v.findViewById(R.id.tx_details_lbl);
        txDetailsView.setText(txDetails);

        String txAmt = nf.format(item.t.getTxAmount());
        TextView txAmtView = (TextView) v.findViewById(R.id.tx_amt_lbl);
        txAmtView.setText(txAmt);
    }

    private String getCategoryPath(long currentCategoryId) {
        StringBuilder ret = new StringBuilder();

        FManEntityManager dbHelper = FManEntityManager.getInstance();
        int lim = 100;
        ArrayList<String> list = new ArrayList<String>();
        if (currentCategoryId != AccountTypes.ACCT_TYPE_ROOT) {
            int i = 0;
            long lastCategoryId = currentCategoryId;
            while (i++ < lim) {
                try {
                    AccountCategory ac = dbHelper
                            .getAccountCategory(lastCategoryId);
                    list.add(ac.getCategoryName());
                    if (ac.getParentCategoryId() == AccountTypes.ACCT_TYPE_ROOT) {
                        break;
                    }
                    lastCategoryId = ac.getParentCategoryId();
                }
                catch (DBException e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
            }
            int sz = list.size();
            for (i = sz - 1; i >= 0; i--) {
                ret.append(list.get(i));
                if (i > 0) {
                    ret.append(" > ");
                }
            }
        }
        return ret.toString();
    }

    /* Row adapter for displaying accounts and categories */
    class GalleryAdapter extends ArrayAdapter<FManEntity> {
        private FManEntityManager dbHelper = FManEntityManager.getInstance();

        GalleryAdapter() {
            super(AccountsActivity.this, R.layout.grid_item_layout, R.id.label);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                LayoutInflater li = getLayoutInflater();
                v = li.inflate(R.layout.grid_item_layout, null);
            }
            else {
                v = convertView;
            }
            ImageView icon = (ImageView) v.findViewById(R.id.icon_image);
            FManEntity entity = super.getItem(position);
            if (entity instanceof AccountCategory) {
                icon.setImageResource(getCategoryIconResource((AccountCategory) entity));
            }
            else {
                icon.setImageResource(R.drawable.account);
            }

            TextView tv = (TextView) v.findViewById(R.id.icon_text);
            tv.setText(entity.toString());
            return v;
        }

        private int getCategoryIconResource(AccountCategory ac) {
            try {
                /* If one of the root categories, get default icons */
                if (ac.getParentCategoryId() == AccountTypes.ACCT_TYPE_ROOT) {
                    return getRootCategoryIcon(ac);
                }
                CategoryIconMap cim = dbHelper.getCategoryIconMap(ac
                        .getCategoryId());
                if (cim == null) {
                    return R.drawable.default_category;
                }
                else {
                    int iconRes = getResources()
                            .getIdentifier(cim.getIconPath(), "drawable",
                                    "com.ifreebudget.fm");
                    if (iconRes == 0) {
                        return R.drawable.default_category;
                    }
                    return iconRes;
                }
            }
            catch (DBException e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
                return R.drawable.default_category;
            }
        }

        private int getRootCategoryIcon(AccountCategory ac) {
            if (ac.getCategoryId() == AccountTypes.ACCT_TYPE_INCOME) {
                return R.drawable.income;
            }
            else if (ac.getCategoryId() == AccountTypes.ACCT_TYPE_CASH) {
                return R.drawable.assets;
            }
            else if (ac.getCategoryId() == AccountTypes.ACCT_TYPE_EXPENSE) {
                return R.drawable.expense;
            }
            else if (ac.getCategoryId() == AccountTypes.ACCT_TYPE_LIABILITY) {
                return R.drawable.liab;
            }
            else {
                return R.drawable.default_category;
            }
        }
    }
}
