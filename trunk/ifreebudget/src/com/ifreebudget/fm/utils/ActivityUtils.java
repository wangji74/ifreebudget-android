package com.ifreebudget.fm.utils;

import java.util.ArrayList;

import android.util.Log;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;

public class ActivityUtils {

    private static final String TAG = "ActivityUtils";

    public static String getCategoryPath(Long categoryId) {
        StringBuilder ret = new StringBuilder();

        int lim = 100;
        ArrayList<String> list = new ArrayList<String>();
        if (categoryId != AccountTypes.ACCT_TYPE_ROOT) {
            int i = 0;
            long lastCategoryId = categoryId;
            while (i++ < lim) {
                try {
                    AccountCategory ac = FManEntityManager.getInstance()
                            .getAccountCategory(lastCategoryId);
                    list.add(ac.getCategoryName());
                    if (ac.getParentCategoryId() == AccountTypes.ACCT_TYPE_ROOT) {
                        break;
                    }
                    lastCategoryId = ac.getParentCategoryId();
                }
                catch (DBException e) {
                    Log.e(TAG + ".getCategoryPath",
                            MiscUtils.stackTrace2String(e));
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

}
