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
}