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
import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.ifreebudget.fm.search.FilterUtils;
import com.ifreebudget.fm.utils.MiscUtils;

public class ManageAccountsActivity extends ListActivity {
    public static final String PARENTCATEGORYIDKEY = "PARENTCATEGORYID";
    public static final String ACCOUNTIDKEY = "ACCOUNTID";
    public static final String CATEGORYIDKEY = "CATEGORYID";

    private static final String TAG = "ManageAccountsActivity";

    private FManEntityManager dbHelper;
    private long currentCategoryId = -1;

    private TextView categoryPathTf = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_accts_layout);
        super.registerForContextMenu(getListView());

        dbHelper = FManEntityManager.getInstance(this);

        categoryPathTf = (TextView) findViewById(R.id.category_path_lbl);
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
            this.setListAdapter(new CategoryRowAdapter(this,
                    R.layout.row_layout, R.id.label, arr));
            
            categoryPathTf.setText(getCategoryPath());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);
        if (obj instanceof AccountCategory) {
            AccountCategory o = (AccountCategory) obj;

            Intent intent = new Intent(this, ManageAccountsActivity.class);

            intent.putExtra(PARENTCATEGORYIDKEY, o.getCategoryId());

            startActivity(intent);
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

        FManEntity obj = (FManEntity) getListAdapter().getItem(info.position);
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
                intent.putExtra(FilterUtils.FILTERKEY,
                        FilterUtils.ACCOUNT_FILTER_TYPE);
                intent.putExtra(FilterUtils.FILTERVALUE, a.getAccountId());
            }
            else {
                AccountCategory a = (AccountCategory) entity;
                intent.putExtra(FilterUtils.FILTERKEY,
                        FilterUtils.CATEGORY_FILTER_TYPE);
                intent.putExtra(FilterUtils.FILTERVALUE, a.getCategoryId());
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
        intent.putExtra(ACCOUNTIDKEY, a.getAccountId());
        startActivity(intent);
    }

    private void startEditCategoryActivity(FManEntity entity) {
        AccountCategory a = (AccountCategory) entity;
        Intent intent = new Intent(this, UpdateCategoryActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        intent.putExtra(CATEGORYIDKEY, a.getCategoryId());
        startActivity(intent);
    }

    private void startAddAccountActivity() {
        Intent intent = new Intent(this, AddAccountActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
        startActivity(intent);
    }

    private void startAddCategoryActivity() {
        Intent intent = new Intent(this, AddCategoryActivity.class);
        intent.putExtra(PARENTCATEGORYIDKEY, currentCategoryId);
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
                    AccountCategory ac = dbHelper.getAccountCategory(lastCategoryId);
                    list.add(ac.getCategoryName());
                    if(ac.getParentCategoryId() == AccountTypes.ACCT_TYPE_ROOT) {
                        break;
                    }
                    lastCategoryId = ac.getParentCategoryId();
                }
                catch (DBException e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
            }
            int sz = list.size();
            for(i = sz - 1; i >= 0; i--) {
                ret.append(list.get(i));
                if(i > 0) {
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
            super(ManageAccountsActivity.this, R.layout.row_layout, R.id.label,
                    items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            FManEntity entity = items[position];
            if (entity instanceof AccountCategory) {
                icon.setImageResource(getCategoryIconResource((AccountCategory) entity));
            }
            else {
                icon.setImageResource(R.drawable.account);
            }
            return (row);
        }

        private int getCategoryIconResource(AccountCategory ac) {
            try {
                CategoryIconMap cim = dbHelper.getCategoryIconMap(ac
                        .getCategoryId());
                if (cim == null) {
                    return R.drawable.folder_blue;
                }
                else {
                    int iconRes = getResources()
                            .getIdentifier(cim.getIconPath(), "drawable",
                                    "com.ifreebudget.fm");
                    if (iconRes == 0) {
                        return R.drawable.folder_blue;
                    }
                    return iconRes;
                }
            }
            catch (DBException e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
                return R.drawable.folder_blue;
            }
        }
    }
}
