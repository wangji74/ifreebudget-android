package com.ifreebudget.fm.activities;

import java.util.Date;
import java.util.List;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetBudgetSummaryAction;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ManageBudgetsActivity extends ListActivity {
    private static final String TAG = "ManageBudgetsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_budgets_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            List<FManEntity> budgets = em.getList(Budget.class);
            if (budgets != null) {
                FManEntity[] arr = new FManEntity[budgets.size()];
                budgets.toArray(arr);
                for (FManEntity fe : budgets) {
                    Budget b = (Budget) fe;
                }
                this.setListAdapter(new ArrayAdapter<FManEntity>(this,
                        R.layout.budget_list_row,
                        // android.R.layout.simple_list_item_1,
                        arr));
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);

        Budget b = (Budget) obj;

        Intent intent = new Intent(this, ViewBudgetActivity.class);

        intent.putExtra(ViewBudgetActivity.BUDGETIDKEY, b.getId());

        startActivity(intent);
    }

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void addBudget(View view) {
        Intent txIntent = new Intent(this, AddBudgetActivity.class);
        startActivity(txIntent);
    }
}
