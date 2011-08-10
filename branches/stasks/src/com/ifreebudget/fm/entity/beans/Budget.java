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
package com.ifreebudget.fm.entity.beans;

public class Budget implements FManEntity {
    private static final long serialVersionUID = 1L;
    public final static int WEEKLY = 1;
    public final static int MONTHLY = 3;
    public final static int BIWEEKLY = 2;

    private Long id;
    private String name;
    private int type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static int getTypeFromString(String type) {
        if (type.equals("Weekly")) {
            return WEEKLY;
        }
        else if (type.equals("Bi-weekly")) {
            return BIWEEKLY;
        }
        return MONTHLY;
    }

    public static String getTypeAsString(int type) {
        if (type == WEEKLY) {
            return "Weekly";
        }
        else if (type == BIWEEKLY) {
            return "Bi-weekly";
        }
        return "Monthly";
    }

    public String toString() {
        return name;
    }

    @Override
    public String getPKColumnName() {
        return "id";
    }

    @Override
    public Object getPK() {
        return id;
    }

    @Override
    public void setPK(Object pk) {
        setId((Long) pk);
    }

    @Override
    public TableMapper getTableMapper() {
        return new BudgetMapper();
    }
}
