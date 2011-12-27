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
package com.ifreebudget.fm.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class SessionManager {
    private static Locale currentLocale;
    private static Locale currencyLocale;
    private static SessionManager instance;
    private static final String TAG = "SessionManager";
    private static final String dateFormat = "dd MMM yyyy";
    private static final String dateTimeFormat = "dd MMM yyyy hh:mm a";

    private SessionManager() {
    }

    public synchronized static void startSession() {
        clearSession();
        if (instance == null) {
            instance = new SessionManager();
            currentLocale = Locale.getDefault();
            currencyLocale = currentLocale;
        }
    }

    private static void clearSession() {
        currentLocale = null;
        currencyLocale = null;
        instance = null;
    }

    public static Locale getCurrentLocale() {
        if (currentLocale == null) {
            currentLocale = new Locale("en", "US");
        }
        return currentLocale;
    }

    public static Locale getCurrencyLocale() {
        if (currencyLocale == null) {
            currencyLocale = new Locale("en", "US");
        }
        return currencyLocale;
    }

    public static boolean isSessionAlive() {
        return instance == null;
    }

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(dateFormat);
    }

    public static SimpleDateFormat getDateTimeFormat() {
        return new SimpleDateFormat(dateTimeFormat);
    }
}
