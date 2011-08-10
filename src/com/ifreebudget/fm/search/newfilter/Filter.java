package com.ifreebudget.fm.search.newfilter;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Filter implements Filterable {
    private String name;

    private List<Predicate> predicates;

    private List<OperatorType> operators;

    private List<String> selectWhat;

    private String filterObject;

    private List<Order> order;

    private Filter(String filterObject) {
        name = System.currentTimeMillis() + ".xml";
        predicates = new ArrayList<Predicate>();
        operators = new ArrayList<OperatorType>();

        this.filterObject = filterObject;
    }

    public Filter(String filterObject, String select) {
        this(filterObject);
        if (select != null) {
            this.selectWhat = new ArrayList<String>();
            this.selectWhat.add(select);
        }
    }

    // public Filter(String filterObject, List<String> select) {
    // this(filterObject);
    // this.selectWhat = select;
    // }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSelectWhat() {
        return selectWhat;
    }

    public void setSelectWhat(List<String> selectWhat) {
        this.selectWhat = selectWhat;
    }

    public String getFilterObject() {
        return filterObject;
    }

    public void setFilterObject(String filterObject) {
        this.filterObject = filterObject;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    private String getSelectString(boolean count) {
        if (count) {
            return " count(R) ";
        }
        StringBuilder builder = new StringBuilder(" R");
        if (selectWhat != null) {
            for (int i = 0; i < selectWhat.size(); i++) {
                builder.append("." + selectWhat.get(i));
                if (i < selectWhat.size() - 1) {
                    builder.append(",");
                }
            }
        }
        else {
            builder.append(".*");
        }
        return builder.toString();
    }

    private String printFilter(boolean count) {
        StringBuilder query = new StringBuilder();

        query.append("select " + getSelectString(count) + " from "
                + getFilterObject() + " R ");

        int sz = predicates.size();
        int opSz = sz - 1;

        if (sz > 0) {
            query.append(" where ");
            for (int i = 0; i < sz; i++) {
                Predicate p = predicates.get(i);
                query.append(p.toSql());
                query.append(PredicateImpl.SPACE);
                if (i < opSz) {
                    query.append(operators.get(i));
                    query.append(PredicateImpl.SPACE);
                }
                query.append("\n");
            }
        }

        if (!count) {
            if (order != null) {
                query.append(" order by ");
                int curr = 0;
                sz = order.size();
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

    public void addPredicate(Predicate p, OperatorType oper) {
        predicates.add(p);
        operators.add(oper);
    }

    @Override
    public String getSubstitutionString() {
        return printFilter(false);
    }

    @Override
    public String getValue() {
        return printFilter(false);
    }

    public String getQueryObject(boolean countQuery) throws Exception {
        StringBuilder q = new StringBuilder(printFilter(countQuery));
        Query query = new Query(q.toString());

        int sz = predicates.size();
        for (int i = 0; i < sz; i++) {
            Predicate p = predicates.get(i);
            p.setParameter(query);
        }
        String queryStr = query.getQueryString();
        //Log.i("Filter", queryStr);
        return queryStr;
    }

    public void addOrder(Order o) {
        if (order == null)
            order = new ArrayList<Order>();
        order.add(o);
    }

    public void clearOrder() {
        if (order != null)
            order.clear();
    }

    public List<Order> getOrder() {
        return order;
    }
}
