package com.ifreebudget.fm.activities.utils;

public interface DialogCallback {
    final int SUCCESS = 1;
    final int FAILURE = 2;
    
    void onReturn(int code, Object result);
    
    void onDismiss(int code, Object context);
}
