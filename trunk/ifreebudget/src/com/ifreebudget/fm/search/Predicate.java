package com.ifreebudget.fm.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Predicate {
    private String selectObject = null;
    private String key;
    private ArrayList<String> values;
    private String rel;
    private String type = null;

    private Filter subClause = null;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static Predicate getPredicate(String selectObject, String key,
            ArrayList<String> values, String rel, String type) {
        return new Predicate(selectObject, key, values, rel, type);
    }

    public Predicate(String key) {
        this.key = key;
    }

    private Predicate(String selectObject, String key,
            ArrayList<String> values, String rel, String type) {
        this.selectObject = selectObject;
        this.key = key;
        this.values = values;
        if (rel.equalsIgnoreCase("Greater than"))
            this.rel = ">=";
        else if (rel.equalsIgnoreCase("Lesser than"))
            this.rel = "<=";
        else
            this.rel = rel;

        this.type = type;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void addSubClause(Filter filter) {
        this.subClause = filter;
    }

    private String getNotesString() {
        StringBuilder ret = new StringBuilder();
        int count = 0;
        for (String v : values) {
            ret.append("lower(" + key + ")");
            ret.append(" " + rel + " ");
            ret.append("?");
            if (count < values.size() - 1)
                ret.append(" OR ");
            count++;
        }

        return ret.toString();
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        if (selectObject == null || selectObject.trim().length() == 0) {
            if (key.equals("txNotes")) {
                ret.append(getNotesString());
                return ret.toString();
            }
            else
                ret.append(key);
        }
        else
            ret.append(selectObject);

        boolean extraBrace = false;

        ret.append(" ");
        ret.append(rel);
        ret.append(" ");
        if (subClause != null) {
            ret.append(" ( ");
            ret.append(subClause.printFilter(false));
            ret.append(" ) ");
        }
        else {
            if (values != null && values.size() > 0) {
                ret.append("(");
                for (int i = 0; i < values.size(); i++) {
                    ret.append("?");
                    if (i < values.size() - 1)
                        ret.append(",");
                }
                ret.append(")");
            }

            if (extraBrace)
                ret.append(")");
        }
        return ret.toString();
    }

    public String toXml() {
        StringBuffer ret = new StringBuffer("<predicate>");
        ret.append("<relationship>");
        if (rel.equals(">")) {
            ret.append("Greater than");
        }
        else if (rel.equals("<")) {
            ret.append("Lesser than");
        }
        else {
            ret.append(rel);
        }
        ret.append("</relationship>");
        ret.append("<key>" + key + "</key>");
        ret.append("<values>");
        for (int i = 0; i < values.size(); i++) {
            String s = values.get(i);
            ret.append(s);
            if (i < values.size() - 1)
                ret.append(",");
        }
        ret.append("</values>");
        ret.append("<type>" + type + "</type>");
        ret.append("</predicate>");
        return ret.toString();
    }

    public String getRel() {
        return rel;
    }

    public String getKey() {
        return key;
    }

    public String getSelectObject() {
        return selectObject;
    }

    public boolean isValid() {
        if (key == null)
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + key.hashCode();
        hash = hash * 31 + (rel == null ? 0 : rel.hashCode());
        hash = hash * 31 + (selectObject == null ? 0 : selectObject.hashCode());
        return hash;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null)
            return false;
        if (!(other instanceof Predicate)) {
            return false;
        }
        Predicate op = (Predicate) other;
        if (selectObject == null || op.getSelectObject() == null)
            return key.equals(op.getKey());
        else {
            return (selectObject.equals(op.getSelectObject()) && key.equals(op
                    .getKey()));
        }
    }

    public boolean hasValues() {
        return (values != null && values.size() > 0);
    }

    public String getType() {
        return type;
    }
}