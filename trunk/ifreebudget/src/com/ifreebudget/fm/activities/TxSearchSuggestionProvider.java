package com.ifreebudget.fm.activities;

import android.content.SearchRecentSuggestionsProvider;

public class TxSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.ifreebudget.fm.TxSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public TxSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
