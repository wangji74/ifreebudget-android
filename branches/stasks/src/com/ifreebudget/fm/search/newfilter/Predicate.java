package com.ifreebudget.fm.search.newfilter;

public interface Predicate {
    String toSql();
    void setParameter(Query query) throws Exception;
}
