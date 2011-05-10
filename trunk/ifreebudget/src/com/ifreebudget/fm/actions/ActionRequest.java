package com.ifreebudget.fm.actions;

import java.util.HashMap;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ActionRequest {
    HashMap<String, Object> params = new HashMap<String, Object>();
    String actionName;

    public ActionRequest() {
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public void setProperty(String key, Object value) {
        params.put(key, value);
    }

    public Object getProperty(String key) {
        return params.get(key);
    }

    public boolean propertyExists(String key) {
        return params.get(key) != null;
    }
}