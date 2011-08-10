package com.ifreebudget.fm.search.newfilter;

import java.util.ArrayList;
import java.util.List;

public class PredicateGroup implements Predicate {
    private List<Predicate> predicates;
    private List<OperatorType> operators;

    public PredicateGroup() {
        predicates = new ArrayList<Predicate>();
        operators = new ArrayList<OperatorType>();
    }

    public static Predicate create(Predicate p) {
        PredicateGroup group = new PredicateGroup();
        group.predicates.add(p);
        return group;
    }

    public void addPredicate(Predicate p, OperatorType oper) {
        predicates.add(p);
        operators.add(oper);
    }

    @Override
    public String toSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("(");
        int sz = predicates.size();
        for (int i = 0; i < sz; i++) {
            ret.append(predicates.get(i).toSql());
            if (i < operators.size()) {
                ret.append(PredicateImpl.SPACE);
                ret.append(operators.get(i));
                ret.append(PredicateImpl.SPACE);
            }
        }
        ret.append(")");
        return ret.toString();
    }

    @Override
    public void setParameter(Query query) throws Exception {
        int sz = predicates.size();
        for (int i = 0; i < sz; i++) {
            Predicate p = predicates.get(i);
            p.setParameter(query);
        }
    }
}
