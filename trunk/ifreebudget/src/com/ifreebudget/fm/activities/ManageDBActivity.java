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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ManageDBActivity extends Activity {

    private static final String TAG = "ManageDBActivity";

    private static final String BKUP_DIR_NAME = "ifb_backup.dir";

    private static final String BKUP_DATE_FORMAT = "yyyyMMdd_HHmmss";

    private static final String DATE_FORMAT = "yyyy.MM.dd 'at' hh:mm:ss a";

    private ArrayAdapter<String> listAdapter = null;

    /* Platform overrides */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_db_layout);

        ListView lv = (ListView) findViewById(R.id.db_bkup_list_view);

        listAdapter = new ArrayAdapter<String>(this, R.layout.budget_list_row);

        lv.setAdapter(listAdapter);

        super.registerForContextMenu(lv);

        TextView spacerTf = (TextView) findViewById(R.id.bkups_title_lbl);
        spacerTf.setText("Backups list");
    }

    @Override
    public void onResume() {
        super.onResume();

        loadBackups();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bkup_item_ctxt_menu, menu);
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

        String obj = (String) listAdapter.getItem(info.position);

        if (item.getItemId() == R.id.delete_bkup_item) {
            deleteBackup(obj);
        }
        else if (item.getItemId() == R.id.user_bkup_item) {
            switchDatabase(obj);
        }

        return true;
    }

    /* End platform overrides */

    private String getBackupDBName(String obj) {
        try {
            SimpleDateFormat dformat = new SimpleDateFormat(DATE_FORMAT);
            Date dt = dformat.parse(obj);

            SimpleDateFormat filenameformat = new SimpleDateFormat(
                    BKUP_DATE_FORMAT);

            String backupDBPath = BKUP_DIR_NAME + "\\"
                    + FManEntityManager.DATABASE_NAME + "_"
                    + filenameformat.format(dt);

            return backupDBPath;
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast.makeText(getApplicationContext(),
                    "Failed to get backup database from SD card",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void switchDatabase(String obj) {
        try {
            // 1. get backup db name
            String backupDBPath = getBackupDBName(obj);

            // 2. get sd storage
            File sd = validateSDStorage();
            if (sd == null) {
                return;
            }

            // 3. close the current database
            FManEntityManager.closeInstance();

            // 4. copy the db

            String currentDBPath = "\\data\\com.ifreebudget.fm\\databases\\"
                    + FManEntityManager.DATABASE_NAME;

            File data = Environment.getDataDirectory();
            
            File currentDB = new File(data, currentDBPath);

            File backupDB = new File(sd, backupDBPath);
            if (backupDB.exists()) {
                FileChannel src = new FileInputStream(backupDB).getChannel();

                FileChannel dst = new FileOutputStream(currentDB).getChannel();

                dst.truncate(0);
                
                dst.transferFrom(src, 0, src.size());

                src.close();

                dst.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        finally {
            // 5. re-start main activity, it will re-initialize db after copy
            startHomeActivity();
        }
    }

    private void deleteBackup(String obj) {
        try {
            String backupDBPath = getBackupDBName(obj);

            File sd = validateSDStorage();
            if (sd == null) {
                return;
            }

            File backupDB = new File(sd, backupDBPath);

            if (backupDB.exists()) {
                backupDB.delete();
            }

            Intent intent = new Intent(this, ManageDBActivity.class);

            startActivity(intent);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast.makeText(getApplicationContext(), "Failed to delete backup",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBackups() {
        try {
            listAdapter.clear();

            File sd = validateSDStorage();
            if (sd == null) {
                return;
            }

            String backupDBPath = BKUP_DIR_NAME;

            File backupDir = new File(sd, backupDBPath);

            if (!backupDir.exists() || !backupDir.isDirectory()) {
                return;
            }

            File[] flist = backupDir.listFiles();

            List<Date> dateList = new ArrayList<Date>();
            for (File f : flist) {
                if (!f.getName().startsWith(FManEntityManager.DATABASE_NAME)) {
                    continue;
                }
                Date dt = getDateFromFileName(f.getName());
                if (dt != null) {
                    dateList.add(dt);
                }
            }

            Collections.sort(dateList, Collections.reverseOrder());

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            for (Date d : dateList) {
                String f = sdf.format(d);
                listAdapter.add(f);
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void gotoHomeScreen(View view) {
        startHomeActivity();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void backupDatabase(View view) {
        try {
            File sd = validateSDStorage();
            if (sd == null) {
                return;
            }

            File data = Environment.getDataDirectory();

            String currentDBPath = "\\data\\com.ifreebudget.fm\\databases\\"
                    + FManEntityManager.DATABASE_NAME;

            File dirPath = new File(sd, BKUP_DIR_NAME);

            if (!dirPath.exists()) {
                if (!dirPath.mkdir()) {
                    return;
                }
            }

            String backupDBPath = BKUP_DIR_NAME + "\\"
                    + FManEntityManager.DATABASE_NAME + "_" + getFileTag();

            File currentDB = new File(data, currentDBPath);

            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {

                FileChannel src = new FileInputStream(currentDB).getChannel();

                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());

                src.close();

                dst.close();
            }
            Intent intent = new Intent(this, ManageDBActivity.class);

            startActivity(intent);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private File validateSDStorage() {
        File sd = Environment.getExternalStorageDirectory();

        if (sd == null) {
            return null;
        }

        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(getApplicationContext(), "SD card is not available",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!sd.canWrite()) {
            Toast.makeText(getApplicationContext(), "SD card is not writeable",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        return sd;
    }

    private String getFileTag() {
        SimpleDateFormat sdf = new SimpleDateFormat(BKUP_DATE_FORMAT);

        return sdf.format(new Date());
    }

    private Date getDateFromFileName(String fname) {
        SimpleDateFormat bkupFormat = new SimpleDateFormat(BKUP_DATE_FORMAT);

        String[] split = fname.split("_");
        if (split.length != 3) {
            return null;
        }

        String dt = split[1] + "_" + split[2];
        try {
            Date date = bkupFormat.parse(dt);
            return date;
        }
        catch (Exception e) {
            Log.e(TAG, "Cannot parse date format for file: " + fname);
        }
        return null;
    }
}
