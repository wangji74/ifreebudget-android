package com.ifreebudget.fm.activities.wizards.budget;

import static com.ifreebudget.fm.utils.Messages.tr;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.UpdateAccountActivity;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

public class BudgetDetailsActivity extends Activity {

    private EditText bNameTf;

    private Spinner bTypeSpinner;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_budget_details_step);

        bNameTf = (EditText) findViewById(R.id.budget_name_tf);

        bTypeSpinner = (Spinner) findViewById(R.id.budget_type_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.budget_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bTypeSpinner.setAdapter(adapter);
    }
    
    public void doCancelAction(View view) {
        super.finish();
    }
    
    public void doNext(View view) {
        String name = bNameTf.getText().toString();
        if (!MiscUtils.isValidString(name)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Name is required"), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        
        int pos = bTypeSpinner.getSelectedItemPosition();
        if (pos < 0) {
            pos = 0;
        }
        String typeStr = bTypeSpinner.getAdapter().getItem(pos).toString();
        int type = Budget.getTypeFromString(typeStr);    
                
        Intent intent = new Intent(this, AddBudgetActivity.class);
        intent.putExtra("BUDGETNAME", name);
        intent.putExtra("BUDGETTYPE", type);
        startActivity(intent);
    }
}

