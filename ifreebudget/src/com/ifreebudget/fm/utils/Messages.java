package com.ifreebudget.fm.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "assets.MessagesBundle"; //$NON-NLS-1$

    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
            BUNDLE_NAME, Locale.getDefault());

    private Messages() {
    }

    public static void initializeMessages() {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
                Locale.getDefault());
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e) {
            return key;
        }
    }

    public static String tr(String key) {
        return getString(key);
    }
}