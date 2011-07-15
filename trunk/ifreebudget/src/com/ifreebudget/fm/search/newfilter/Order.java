/*******************************************************************************
 * Copyright  
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
package com.ifreebudget.fm.search.newfilter;

public class Order {
    private String column;
    private int direction;

    public static final int ASC = 1;
    public static final int DESC = 2;

    private static final String mappings[][] = { { "", "" },
            { "Date", "TXDATE" }, { "From", "fromName" }, { "To", "toName" },
            { "Amount", "TXAMOUNT" }};

    public Order(String columnName, int direction) {
        for (int i = 0; i < mappings.length; i++) {
            String viewName = mappings[i][0];
            if (viewName.equals(columnName)) {
                columnName = mappings[i][1];
            }
        }
        this.column = columnName;
        this.direction = direction;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(" ");
        ret.append(column);
        ret.append(" ");
        ret.append(getDirectionString());
        return ret.toString();
    }

    private String getDirectionString() {
        if (direction == ASC)
            return "ASC";
        else
            return "DESC";
    }

    public static int getColumnIndex(String colName) {
        for (int i = 0; i < mappings.length; i++) {
            String[] row = mappings[i];
            if (row[1].equals(colName)) {
                return i;
            }
        }
        return -1;
    }

    public static String getColumnName(int colIdx) {
        if (colIdx >= mappings.length)
            return null;

        String[] row = mappings[colIdx];
        return row[0];
    }

    public static String getColumnDBName(int colIdx) {
        if (colIdx >= mappings.length)
            return null;

        String[] row = mappings[colIdx];
        return row[1];
    }
}
