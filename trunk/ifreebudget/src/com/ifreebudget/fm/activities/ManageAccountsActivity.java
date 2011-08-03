/*******************************************************************************
 * Copyright 2011 ifreebudget@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ifreebudget.fm.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.DeleteAccountAction;
import com.ifreebudget.fm.actions.DeleteCategoryAction;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.CategoryIconMap;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.utils.MiscUtils;

public class ManageAccountsActivity extends Activity {
    public static final String PARENTCATEGORYIDKEY = "PARENTCATEGORYID";
    public static final String PARENTCATEGORYIDPATH = "PARENTCATEGORYPATH";
    public static final String ACCOUNTIDKEY = "ACCOUNTID";
    public static final String CATEGORYIDKEY = "CATEGORYID";

    private static final String TAG = "ManageAccountsActivity";

    private FManEntityManager dbHelper;
    private long currentCategoryId = -1;

    private TextView categoryPathTf = null;

    private GridView grid = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);        
        super.setContentView(R.layout.manage_accts_layout);
        
        dbHelper = FManEntityManager.getInstance(this);

        categoryPathTf = (TextView) findViewById(R.id.category_path_lbl);

        grid = (GridView) findViewById(R.id.accts_grid);

        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {

                handleGridItemClick(parent, v, position, id);
            }
        });

        registerForContextMenu(grid);
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            long lastId = Long.valueOf(AccountTypes.ACCT_TYPE_ROOT);

            Intent intent = this.getIntent();

            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null && bundle.containsKey(PARENTCATEGORYIDKEY)) {
                    lastId = (Long) bundle.get(PARENTCATEGORYIDKEY);
                }
            }
            currentCategoryId = lastId;

            List<FManEntity> catgs = dbHelper.getChildren(lastId);

            FManEntity[] arr = new FManEntity[catgs.size()];
            catgs.toArray(arr);

            grid.setAdapter(new CategoryRowAdapter(this,
                    R.layout.grid_item_layout, R.id.label, arr));

            categoryPathTf.setText(getCategoryPath());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void handleGridItemClick(AdapterView<?> l, View v, int position,
            long id) {
        Object obj = grid.getAdapter().getItem(position);
        if (obj instanceof AccountCategory) {
            AccountCategory o = (AccountCategory) obj;

            Intent intent = new Intent(this, ManageAccountsActivity.class);

            intent.putExtra(PARENTCATEGORYIDKEY, o.getCategoryId());

            startActivity(intent);
        }
        else {
            Account o = (Account) obj;
            doListTxAction(o);
        }
    }

    public void addAccount(View view) {
        startAddAccountActivity();
    }

    public void addCategory(View view) {
        startAddCategoryActivity();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        if (currentCategoryId == AccountTypes.ACCT_TYPE_ROOT) {
            inflater.inflate(R.menu.rootacct_ctxt_menu, menu);
        }
        else {
            inflater.inflate(R.menu.acct_ctxt_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return true;
        }

        FManEntity obj = (FManEntity) grid.getAdapter().getItem(info.position);
        if (item.getItemId() == R.id.edit_item) {
            doEditAction(obj);
        }
        else if (item.getItemId() == R.id.list_tx_for_acct) {
            doListTxAction(obj);
        }
        else {
            doDeleteAction(obj);
        }
        return true;
    }

    private void doDeleteAction(FManEntity obj) {
        if (obj instanceof Account) {
            deleteAccount(obj);
        }
        else {
            deleteCategory(obj);
        }
    }

    private void doEditAction(FManEntity entity) {
        if (entity instanceof Account) {
            startEditAccountActivity(entity);
        }
        else {
            startEditCategoryActivity(entity);
        }
    }

    private void doListTxAction(FManEntity entity) {
        startListTxActivity(entity);
    }

    private void deleteCategory(FManEntity entity) {
        ActionRequest req = new ActionRequest();
        req.setProperty("ACCOUNTCATEGORY", entity);

        DeleteCategoryAction action = new DeleteCategoryAction();
        try {
            ActionResponse resp = action.executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                Intent intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);

                startActivity(intent);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void deleteAccount(FManEntity entity) {
        ActionRequest req = new ActionRequest();
        req.setProperty("ACCOUNTID", entity.getPK());

        DeleteAccountAction action = new DeleteAccountAction();
        try {
            ActionResponse resp = action.executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                Intent intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);

                startActivity(intent);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void startListTxActivity(FManEntity entity) {

        try {
            Intent intent = new Intent(this, ListTransactionsActivity.class);
            if (entity instanceof Account) {
                Account a = (Account) entity;
                intent.putExtra(NewFilterUtils.FILTERKEY,
                        NewFilterUtils.ACCOUNT_FILTER_TYPE);
                intent.putExtra(NewFilterUtils.FILTERVALUE, a.getAccountId());
            }
            else {
                AccountCategory a = (AccountCategory) entity;
                intent.putExtra(NewFilterUtils.FILTERKEY,
                        NewFilterUtils.CATEGORY_FILTER_TYPE);
                intent.putExtra(NewFilterUtils.FILTERVALUE, a.getCategoryId());
            }
            startActivity(intent);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void startEditAccountActivity(FManEntity entity) {
        Account a = (Account) entity;
        Intent intent = new Intent(this, UpdateAccountActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        intent.putExtra(PARENTCATEGORYIDPATH, categoryPathTf.getText());
        intent.putExtra(ACCOUNTIDKEY, a.getAccountId());
        startActivity(intent);
    }

    private void startEditCategoryActivity(FManEntity entity) {
        AccountCategory a = (AccountCategory) entity;
        Intent intent = new Intent(this, UpdateCategoryActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        intent.putExtra(PARENTCATEGORYIDPATH, categoryPathTf.getText());
        intent.putExtra(CATEGORYIDKEY, a.getCategoryId());
        startActivity(intent);
    }

    private void startAddAccountActivity() {
        Intent intent = new Intent(this, AddAccountActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        intent.putExtra(PARENTCATEGORYIDPATH, categoryPathTf.getText());
        startActivity(intent);
    }

    private void startAddCategoryActivity() {
        Intent intent = new Intent(this, AddCategoryActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        intent.putExtra(PARENTCATEGORYIDPATH, categoryPathTf.getText());
        startActivity(intent);
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void gotoHomeScreen(View view) {
        startHomeActivity();
    }

    private String getCategoryPath() {
        StringBuilder ret = new StringBuilder();

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
    class CategoryRowAdapter extends ArrayAdapter<FManEntity> {
        private FManEntity[] items;

        CategoryRowAdapter(Context context, int resource, int label,
                FManEntity[] items) {
            super(ManageAccountsActivity.this, R.layout.grid_item_layout,
                    R.id.label, items);
            this.items = items;
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
            FManEntity entity = items[position];
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
                if(ac.getParentCategoryId() == AccountTypes.ACCT_TYPE_ROOT) {
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
