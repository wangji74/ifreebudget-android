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

import static com.ifreebudget.fm.utils.Messages.tr;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

    private TextView subtitleLbl = null;
    
    private long catId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_catgr_layout);
        subtitleLbl = (TextView) findViewById(R.id.subtitle_lbl);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = this.getIntent();

        catId = -1;
        String categoryPath = null;
        
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(iFreeBudget.PARENTCATEGORYIDKEY)) {
                    catId = (Long) bundle.get(iFreeBudget.PARENTCATEGORYIDKEY);
                }
                if (bundle
                        .containsKey(ManageAccountsActivity.PARENTCATEGORYIDPATH)) {
                    categoryPath = (String) bundle
                            .get(ManageAccountsActivity.PARENTCATEGORYIDPATH);
                }
            }
        }
        if (categoryPath != null) {
            subtitleLbl.setText(categoryPath);
        }        
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveCategory(View view) {
        Intent intent = this.getIntent();
        if (catId != -1) {
            if (createCategory(catId)) {
                intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(iFreeBudget.PARENTCATEGORYIDKEY, catId);

                startActivity(intent);
                finish();
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
