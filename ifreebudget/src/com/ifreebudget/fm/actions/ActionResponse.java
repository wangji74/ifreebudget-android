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
package com.ifreebudget.fm.actions;

import java.util.HashMap;
import java.util.List;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ActionResponse {
    int errorCode = 0;
    String errorMessage;
    List resultList;
    HashMap<String, Object> objects = new HashMap<String, Object>();

    public static final int NOERROR = 0;
    public static final int GENERAL_ERROR = 99;
    public static final int INSUFFICIENT_BALANCE = 100;
    public static final int LIAB_ACCT_EXCEEDED_BALANCE = 200;
    public static final int ACCOUNT_DELETE_ERROR = 300;
    public static final int INACTIVE_ACCOUNT_OPERATION = 400;
    public static final int TX_DELETE_ERROR = 500;
    public static final int TX_CREATE_ERROR = 501;
    public static final int INVALID_TX = 600;
    public static final int INVALID_TO_ACCOUNT = 700;
    public static final int ACCOUNT_EXISTS_ADD_FAIL = 800;
    public static final int INVALID_FROM_ACCOUNT = 900;
    public static final int TX_EXISTS_ERROR = 1000;

    public static final int EMPTY_CATEGORY = 1001;
    public static final int INVALID_CATEGORY_NAME = 1002;
    public static final int INVALID_PARENT_CATEGORY = 1003;

    public ActionResponse() {

    }

    public Object getResult(String key) {
        return objects.get(key);
    }

    public void addResult(String key, Object result) {
        objects.put(key, result);
    }

    public void setResultList(List l) {
        this.resultList = l;
    }

    public List getResultList() {
        return this.resultList;
    }

    public void setErrorCode(int ec) {
        errorCode = ec;
        errorMessage = "General error";
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorMessage(String err) {
        errorMessage = err;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasErrors() {
        return errorCode != NOERROR;
    }
}
