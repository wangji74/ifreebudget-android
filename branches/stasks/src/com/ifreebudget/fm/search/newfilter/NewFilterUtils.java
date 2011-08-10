package com.ifreebudget.fm.search.newfilter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;

public class NewFilterUtils {

    public static final String FILTERKEY = "FILTER";
    public static final String FILTERVALUE = "FILTERVALUE";

    public static final String ACCOUNT_FILTER_TYPE_DATERANGED = "ACCOUNT_FILTER_DATERANGED";
    public static final String ACCOUNT_FILTER_TYPE = "ACCOUNT_FILTER";
    public static final String ACCOUNTID = "ACCOUNTID";
    public static final String CATEGORYID = "CATEGORYID";

    public static final String STARTDATE = "STARTDATE";
    public static final String ENDDATE = "ENDDATE";

    public static final String CATEGORY_FILTER_TYPE = "CATEGORY_FILTER";

    public enum DATE_RANGE {
        Today("Today"), Yesterday("Yesterday"), LastWeek("Last 7 days"), LastMonth(
                "Last 30 days"), All("All");

        private String displayName;

        private DATE_RANGE(String c) {
            displayName = c;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static Filter getBySearchQuery(String query) {
        query = query.toLowerCase();

        Filter f = new Filter("FMTRANSACTION", "txId");

        Filter subfilter = new Filter("Account", "accountId");
        subfilter.addPredicate(PredicateImpl.create("lower(accountName)", query,
                RelationType.LIKE, String.class.getName()), OperatorType.AND);

        Predicate p1 = PredicateImpl.create("fromAccountId", subfilter,
                RelationType.IN, String.class.getName());

        Predicate p2 = PredicateImpl.create("toAccountId", subfilter,
                RelationType.IN, String.class.getName());

        Predicate p3 = PredicateImpl.create("lower(txNotes)", query,
                RelationType.LIKE, String.class.getName());

        f.addPredicate(p1, OperatorType.OR);
        f.addPredicate(p2, OperatorType.OR);
        f.addPredicate(p3, OperatorType.AND);

        Filter ret = new Filter("FMTRANSACTION", null);
        Predicate p = PredicateImpl.create("txid", f, RelationType.IN,
                Long.class.getName());
        ret.addPredicate(p, OperatorType.AND);

        return ret;
    }

    public static Filter getByAccountIdFilter(long accountId) {

        Filter f = new Filter("FMTRANSACTION", null);

        String val = String.valueOf(accountId);

        Predicate p1 = PredicateImpl.create("fromAccountId", val, RelationType.IN,
                Long.class.getName());

        Predicate p2 = PredicateImpl.create("toAccountId", val, RelationType.IN,
                Long.class.getName());

        PredicateGroup group = (PredicateGroup) PredicateGroup.create(p1);
        group.addPredicate(p2, OperatorType.OR);
        
        f.addPredicate(group, OperatorType.AND);

        return f;
    }

    public static Filter getByAccountIdListFilter(List<String> accountIdList) {
        Filter f = new Filter("FMTRANSACTION", null);

        List<Filterable> val = new ArrayList<Filterable>();
        for (String s : accountIdList) {
            val.add(new PredicateValue(RelationType.IN, s));
        }

        Predicate p1 = PredicateImpl.create("fromAccountId", val, RelationType.IN,
                Long.class.getName());

        Predicate p2 = PredicateImpl.create("toAccountId", val, RelationType.IN,
                Long.class.getName());

        PredicateGroup group = (PredicateGroup) PredicateGroup.create(p1);
        group.addPredicate(p2, OperatorType.OR);
        
        f.addPredicate(group, OperatorType.AND);

        return f;
    }

    public static Filter getByDateFilter(long from, long to) {
        Filter f = new Filter("FMTRANSACTION", null);

        Predicate p1 = PredicateImpl.create("TXDATE", String.valueOf(from),
                RelationType.GREATER_THAN, Long.class.getName());

        Predicate p2 = PredicateImpl.create("TXDATE", String.valueOf(to),
                RelationType.LESSER_THAN, Long.class.getName());

        f.addPredicate(p1, OperatorType.AND);
        f.addPredicate(p2, OperatorType.AND);

        return f;
    }

    public static Filter getByDateRangeFilter(DATE_RANGE dateRangeType) {
        ArrayList<String> range = translateDateRanges(dateRangeType);

        long from = Long.parseLong(range.get(0));
        long to = Long.parseLong(range.get(1));

        return getByDateFilter(from, to);
    }

    public static void addDateRangeToFilter(Filter f, DATE_RANGE dateRangeType) {
        ArrayList<String> range = translateDateRanges(dateRangeType);
        addDateRangeToFilter(f, range.get(0), range.get(1));
    }

    public static void addDateRangeToFilter(Filter f, String startDate,
            String endDate) {
        Predicate p1 = PredicateImpl.create("TXDATE", startDate,
                RelationType.GREATER_THAN, Long.class.getName());

        Predicate p2 = PredicateImpl.create("TXDATE", endDate,
                RelationType.LESSER_THAN, Long.class.getName());

        f.addPredicate(p1, OperatorType.AND);
        f.addPredicate(p2, OperatorType.AND);
    }

    private static Calendar getToday() {
        Calendar gc = Calendar.getInstance();
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc;
    }

    private static Calendar getEndOfDay(Calendar gc) {
        if (gc == null) {
            gc = Calendar.getInstance();
        }
        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 59);
        gc.set(Calendar.SECOND, 59);
        gc.set(Calendar.MILLISECOND, 0);
        return gc;
    }

    public static ArrayList<String> translateDateRanges(DATE_RANGE range) {
        ArrayList<String> ret = new ArrayList<String>();
        if (range == DATE_RANGE.Yesterday) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(Calendar.DATE, -1);
            ret.add(String.valueOf(gc.getTimeInMillis()));
        }
        else if (range == DATE_RANGE.Today) {
            ret.add(String.valueOf(getToday().getTimeInMillis()));
            ret.add(String.valueOf(getEndOfDay(null).getTimeInMillis()));
        }
        else if (range == DATE_RANGE.LastWeek) {
            Calendar gc = getEndOfDay(null);
            ret.add(String.valueOf(gc.getTimeInMillis()));

            for (int i = 0; i < 7; i++) {
                gc.add(Calendar.DATE, -1);
            }
            ret.add(0, String.valueOf(gc.getTimeInMillis()));
        }
        else if (range == DATE_RANGE.LastMonth) {
            Calendar gc = getEndOfDay(null);
            ret.add(String.valueOf(gc.getTimeInMillis()));

            for (int i = 0; i < 30; i++) {
                gc.add(Calendar.DATE, -1);
            }
            ret.add(0, String.valueOf(gc.getTimeInMillis()));
        }
        else { // All
            ret.add(String.valueOf(0));
            Calendar eod = getEndOfDay(null);
            ret.add(String.valueOf(eod.getTimeInMillis()));
        }
        return ret;
    }
}
