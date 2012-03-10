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
package com.ifreebudget.fm.constants;

import com.ifreebudget.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AccountTypes {
    public static final int ACCT_TYPE_ROOT = -1;
    public static final int ACCT_TYPE_INCOME = 0;
    public static final int ACCT_TYPE_CASH = 1;
    public static final int ACCT_TYPE_EXPENSE = 2;
    public static final int ACCT_TYPE_LIABILITY = 3;
    public static final int ACCT_TYPE_STOCKS = 4;

    public static final String[] ACCT_TYPES = { Messages.getString("Income"),
            Messages.getString("Assets"), Messages.getString("Expense"),
            Messages.getString("Liability") };

    public static final int TX_STATUS_COMPLETED = 1;
    public static final int TX_STATUS_PENDING = 2;
    public static final int TX_STATUS_CANCELLED = 3;

    public static final int ACCOUNT_ACTIVE = 1;
    public static final int ACCOUNT_LOCKED = 2;
    public static final int ACCOUNT_CLOSED = 3;

    public static final String DEFAULT_ROOT_NAME_TR = Messages
            .getString("Accounts");
    public static final String DEFAULT_ROOT_NAME = "Accounts";

    public enum TransactionType {
        Income("Income"), Expense("Expense"), Transfer("Transfer");

        private String displayName;

        private TransactionType(String c) {
            displayName = c;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static String getAccountStatus(int status) {
        String ret = Messages.getString("unknown"); //$NON-NLS-1$

        switch (status) {
        case ACCOUNT_ACTIVE:
            ret = Messages.getString("Active"); //$NON-NLS-1$
            break;
        case ACCOUNT_LOCKED:
            ret = Messages.getString("Locked"); //$NON-NLS-1$
            break;
        case ACCOUNT_CLOSED:
            ret = Messages.getString("Closed"); //$NON-NLS-1$
            break;
        }
        return ret;
    }

    public static String getTxStatus(int status) {
        String ret = Messages.getString("unknown"); //$NON-NLS-1$

        switch (status) {
        case TX_STATUS_COMPLETED:
            ret = Messages.getString("Completed"); //$NON-NLS-1$
            break;
        case TX_STATUS_PENDING:
            ret = Messages.getString("Pending"); //$NON-NLS-1$
            break;
        case TX_STATUS_CANCELLED:
            ret = Messages.getString("Cancelled"); //$NON-NLS-1$
            break;
        }
        return ret;
    }

    public static String getAccountType(long atype) {
        int a = (int) atype;
        return getAccountType(a);
    }

    public static String getAccountType(int atype) {
        String ret = Messages.getString("unknown"); //$NON-NLS-1$
        switch (atype) {
        case ACCT_TYPE_INCOME:
            ret = Messages.getString("Income"); //$NON-NLS-1$
            break;
        case ACCT_TYPE_CASH:
            ret = Messages.getString("Cash & Investments"); //$NON-NLS-1$
            break;
        case ACCT_TYPE_EXPENSE:
            ret = Messages.getString("Expense"); //$NON-NLS-1$
            break;
        case ACCT_TYPE_LIABILITY:
            ret = Messages.getString("Liability"); //$NON-NLS-1$
            break;
        default:
            break;
        }
        return ret;
    }

    public static TransactionType getTransactionType(int fromAccountType,
            int toAccountType) {
        if (fromAccountType == AccountTypes.ACCT_TYPE_INCOME) {
            return TransactionType.Income;
        }
        else if (toAccountType == AccountTypes.ACCT_TYPE_EXPENSE) {
            return TransactionType.Expense;
        }
        else {
            return TransactionType.Transfer;
        }
    }
}
