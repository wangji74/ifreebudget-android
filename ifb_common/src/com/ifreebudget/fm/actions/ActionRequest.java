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
