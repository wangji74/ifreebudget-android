package com.ifreebudget.fm.search;

import java.util.ArrayList;

import android.util.Log;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Filter implements Cloneable {
    private String name;

    private ArrayList<Predicate> filter;
    private ArrayList<String> operatorList;

    public static final String AND_OPERATOR = "AND";
    public static final String OR_OPERATOR = "OR";

    private ArrayList<Order> order;

    private ArrayList<Group> groups = null;
    private ArrayList<String> groupOperators = null;

    private ArrayList<String> selectWhat;

    private String filterObject = null;

    private static final String TAG = "Filter";

    public Filter() {
        name = System.currentTimeMillis() + ".xml";
        filter = new ArrayList<Predicate>();
        operatorList = new ArrayList<String>();

        groups = new ArrayList<Group>();
        groupOperators = new ArrayList<String>();
    }

    public void breakGroup(String operator) {
        Group curr = new Group();
        groups.add(curr);
        for (Predicate f : filter) {
            curr.getFilter().add(f);
        }
        for (String s : operatorList) {
            curr.getOperators().add(s);
        }
        groupOperators.add(operator);
        filter.clear();
        operatorList.clear();
    }

    public String toString() {
        return name;
    }

    public String getFilterName() {
        return name;
    }

    public void setFilterName(String n) {
        name = n;
    }

    public void addOrder(Order o) {
        if (order == null)
            order = new ArrayList<Order>();
        // order.clear();
        order.add(o);
    }

    public void clearOrder() {
        if (order != null)
            order.clear();
    }

    public ArrayList<Order> getOrder() {
        return order;
    }

    public boolean addPredicate(Predicate p, String operator) {
        if (!p.isValid())
            return false;

        int pos = filter.indexOf(p);
        if (pos < 0) {
            filter.add(p);
            operatorList.add(operator);
        }
        else {
            filter.set(pos, p);
            operatorList.set(pos, operator);
        }
        return true;
    }

    public void setSelectWhat(ArrayList<String> what) {
        selectWhat = what;
    }

    public String getSelectString(boolean count) {
        if (count) {
            return " count(*) ";
        }
        StringBuilder builder = new StringBuilder();
        if (selectWhat != null) {
            for (int i = 0; i < selectWhat.size(); i++) {
                builder.append(selectWhat.get(i));
                if (i < selectWhat.size() - 1) {
                    builder.append(",");
                }
            }
        }
        else {
            builder.append(" * ");
        }
        return builder.toString();
    }

    public boolean hasPredicates() {
        return filter.size() > 0;
    }

    public boolean hasPredicate(String key) {
        for (int i = 0; i < filter.size(); i++) {
            Predicate p = filter.get(i);
            if (p.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public Predicate getPredicate(String key) {
        for (int i = 0; i < filter.size(); i++) {
            Predicate p = filter.get(i);
            if (p.getKey().equals(key)) {
                return p;
            }
        }
        return null;
    }

    public void clearPredicate(String s) {
        Predicate p = new Predicate(s);
        filter.remove(p);
    }

    public String getFilterObject() {
        if (filterObject == null)
            return "FMTRANSACTION";
        else
            return filterObject;
    }

    public void setFilterObject(String obj) {
        filterObject = obj;
    }

    public String printFilter(boolean count) {
        /* If any ungrouped filters exist, add them to a group before processing */
        if (filter.size() > 0) {
            breakGroup(AND_OPERATOR);
        }

        StringBuffer query = new StringBuffer();
        query.append("select " + getSelectString(count) + " from "
                + getFilterObject());

        /* First add aroups */
        if (groups.size() > 0) {
            query.append(" where ");
            for (int i = 0; i < groups.size(); i++) {
                Group g = groups.get(i);
                query.append(printSubFilter(g.getFilter(), g.getOperators()));
                if (i < groups.size() - 1)
                    query.append(" " + groupOperators.get(i) + " ");
            }
        }
        /* Add any remaining filters */
        if (filter.size() > 0) {
            query.append(printSubFilter(filter, operatorList));
        }
        if (!count) {
            if (order != null) {
                query.append(" order by ");
                int curr = 0;
                int sz = order.size();
                for (Order o : order) {
                    query.append(o);
                    if (curr < sz - 1) {
                        query.append(",");
                    }
                    curr++;
                }
            }
        }
        return query.toString();
    }

    private String printSubFilter(ArrayList<Predicate> predicates,
            ArrayList<String> operators) {
        StringBuffer query = new StringBuffer("(");
        for (int i = 0; i < predicates.size(); i++) {
            Predicate p = predicates.get(i);
            query.append(p);
            if (i < predicates.size() - 1) {
                query.append(" " + operators.get(i) + " ");
            }
        }
        query.append(")");

        return query.toString();
    }

    private void doSubstitute(StringBuilder q, Object value) {
        int pos = q.indexOf("?");
        if (pos >= 0 && pos + 1 < q.length()) {
            q.replace(pos, pos + 1, value.toString());
        }
    }

    public String getQueryObject(boolean countQuery) throws Exception {
        try {
            int count = 0;
            StringBuilder q = new StringBuilder(printFilter(false));
            for (Group g : groups) {
                ArrayList<Predicate> subFilter = g.getFilter();
                for (int i = 0; i < subFilter.size(); i++) {
                    Predicate p = subFilter.get(i);
                    if (!p.hasValues())
                        continue;

                    String type = p.getType();
                    ArrayList<String> values = p.getValues();
                    int sz = values.size();
                    //
                    for (int j = 0; j < sz; j++) {
                        if (type.equals(Double.class.getName())) {
                            Double d = Double.parseDouble(values.get(j));
                            doSubstitute(q, d);
                            count++;
                        }
                        else if (type.equals(Long.class.getName())) {
                            Long l = Long.valueOf(values.get(j));
                            doSubstitute(q, l);
                            count++;
                        }
                        else if (type.equals(String.class.getName())) {
                            if (p.getRel().equals("like")) {
                                String val = "'%" + values.get(j) + "%'";
                                doSubstitute(q, val);
                                count++;
                            }
                            else {
                                doSubstitute(q, "'" + values.get(j) + "'");
                                count++;
                            }
                        }
                        else {
                            doSubstitute(q, "'" + values.get(j) + "'");
                            count++;
                        }
                    }
                    //
                }
            }
            // System.out.println("Query = " + q.getQueryString());
            return q.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String toXml() {
        StringBuffer query = new StringBuffer();
        query.append("<filter>");
        query.append("<name>" + name + "</name>");
        for (int i = 0; i < filter.size(); i++) {
            Predicate p = filter.get(i);
            query.append(p.toXml());
        }
        query.append("</filter>");
        return query.toString();
    }

    public boolean validateFilter() {
        boolean ret = true;
        if (filter.size() == 0)
            return false;
        for (int i = 0; i < filter.size(); i++) {
            Predicate p = filter.get(i);
            if (!p.isValid()) {
                clearPredicate(p.getKey());
                ret = false;
                break;
            }
        }
        return ret;
    }

    public final Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private static class Group {
        ArrayList<Predicate> filter;
        ArrayList<String> operators;

        public Group() {
            filter = new ArrayList<Predicate>();
            operators = new ArrayList<String>();
        }

        public ArrayList<Predicate> getFilter() {
            return filter;
        }

        public ArrayList<String> getOperators() {
            return operators;
        }
    }
}