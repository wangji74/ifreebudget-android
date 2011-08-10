package com.ifreebudget.fm.search.newfilter;


public class Query {
    private StringBuilder query;
    private int index = 0;

    public Query(String queryStr) {
        query = new StringBuilder(queryStr);
    }

    public void setParameter(String type, Filterable p) throws Exception {

        Object value = p.getValue();
        if (type.equals(String.class.getName())) {
            String val = "'" + value.toString() + "'";
            doSubstitute(val);
        }
        else if (type.equals(Double.class.getName())) {
            Double d = Double.parseDouble(value.toString());
            doSubstitute(d);
        }
        else if (type.equals(Long.class.getName())) {
            Long l = Long.valueOf(value.toString());
            doSubstitute(l);
        }
        index++;
    }

    private void doSubstitute(Object value) {
        int pos = query.indexOf("?");
        if (pos >= 0 && pos + 1 < query.length()) {
            query.replace(pos, pos + 1, value.toString());
        }
    }
    
    public String getQueryString() {
        return query.toString();
    }
}
