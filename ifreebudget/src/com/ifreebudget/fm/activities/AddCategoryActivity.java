package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddCategoryAction;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddCategoryActivity extends Activity {
    private static final String TAG = "AddCategoryActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_catgr_layout);
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveCategory(View view) {
        Intent intent = this.getIntent();

        long catId = -1;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(iFreeBudget.PARENTCATEGORYIDKEY)) {
                    catId = (Long) bundle.get(iFreeBudget.PARENTCATEGORYIDKEY);
                }
            }
        }

        if (catId != -1) {
            if (createCategory(catId)) {
                super.finish();
                intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(iFreeBudget.PARENTCATEGORYIDKEY, catId);

                startActivity(intent);
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Please select a main category.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private boolean createCategory(long categoryId) {
        try {
            EditText nameTf = (EditText) findViewById(R.id.catgr_name_tf);
            String name = nameTf.getText().toString();

            if (name == null || name.trim().length() == 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("Category name is required"), Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }

            FManEntityManager em = FManEntityManager.getInstance();

            AccountCategory parent = em.getAccountCategory(categoryId);

            ActionRequest req = new ActionRequest();
            req.setActionName("addAccountCategory");
            req.setProperty("CATEGORYNAME", name);
            req.setProperty("PARENTCATEGORY", parent);

            ActionResponse resp = new AddCategoryAction().execute(req);

            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                return true;
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }
        catch (Exception e) {
            Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            return false;
        }
    }
}
