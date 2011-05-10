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
package com.ifreebudget.fm.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R;

public class FilterUtils {
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

    public static Filter getByAccountIdFilter(Long accountId) {
        Filter topLvlFilter = new Filter();

        ArrayList<String> l2 = new ArrayList<String>();
        l2.add(String.valueOf(accountId));

        Predicate p3 = Predicate.getPredicate("FROMACCOUNTID", "FROMACCOUNTID",
                l2, "in", Long.class.getName());

        Predicate p4 = Predicate.getPredicate("TOACCOUNTID", "TOACCOUNTID", l2,
                "in", Long.class.getName());

        topLvlFilter.addPredicate(p3, Filter.OR_OPERATOR);
        topLvlFilter.addPredicate(p4, Filter.OR_OPERATOR);

        topLvlFilter.setFilterName(accountId + "");

        return topLvlFilter;
    }

    public static Filter getByAccountIdListFilter(List<String> accountIdList) {
        Filter topLvlFilter = new Filter();

        Predicate p3 = Predicate.getPredicate("FROMACCOUNTID", "FROMACCOUNTID",
                (ArrayList<String>) accountIdList, "in", Long.class.getName());

        Predicate p4 = Predicate.getPredicate("TOACCOUNTID", "TOACCOUNTID",
                (ArrayList<String>) accountIdList, "in", Long.class.getName());

        topLvlFilter.addPredicate(p3, Filter.OR_OPERATOR);
        topLvlFilter.addPredicate(p4, Filter.OR_OPERATOR);

        topLvlFilter.setFilterName("byIdList");

        return topLvlFilter;
    }

    public static Filter getByDateFilter(long from, long to) {
        Filter topLvlFilter = new Filter();

        ArrayList<String> start = new ArrayList<String>(1);
        start.add(String.valueOf(from));

        ArrayList<String> end = new ArrayList<String>(1);
        end.add(String.valueOf(to));

        Predicate p3 = Predicate.getPredicate("TXDATE", "TXDATE", start,
                "Greater than", Long.class.getName());

        Predicate p4 = Predicate.getPredicate("TXDATE", "TXDATE", end,
                "Lesser than", Long.class.getName());

        topLvlFilter.addPredicate(p3, Filter.AND_OPERATOR);
        topLvlFilter.breakGroup(Filter.AND_OPERATOR);
        topLvlFilter.addPredicate(p4, Filter.AND_OPERATOR);

        return topLvlFilter;
    }

    
    public static Filter getByDateRangeFilter(DATE_RANGE dateRangeType) {
        Filter topLvlFilter = new Filter();

        ArrayList<String> range = translateDateRanges(dateRangeType);

        ArrayList<String> start = new ArrayList<String>(1);
        start.add(range.get(0));

        ArrayList<String> end = new ArrayList<String>(1);
        end.add(range.get(1));

        Predicate p3 = Predicate.getPredicate("TXDATE", "TXDATE", start,
                "Greater than", Long.class.getName());

        Predicate p4 = Predicate.getPredicate("TXDATE", "TXDATE", end,
                "Lesser than", Long.class.getName());

        topLvlFilter.addPredicate(p3, Filter.AND_OPERATOR);
        topLvlFilter.breakGroup(Filter.AND_OPERATOR);
        topLvlFilter.addPredicate(p4, Filter.AND_OPERATOR);

        return topLvlFilter;
    }

    public static void addDateRangeToFilter(Filter f, DATE_RANGE dateRangeType) {
        ArrayList<String> range = translateDateRanges(dateRangeType);
        addDateRangeToFilter(f, range.get(0), range.get(1));
    }
    
    public static void addDateRangeToFilter(Filter f, String startDate, String endDate) {
        ArrayList<String> start = new ArrayList<String>(1);
        start.add(startDate);

        ArrayList<String> end = new ArrayList<String>(1);
        end.add(endDate);

        Predicate p3 = Predicate.getPredicate("TXDATE", "TXDATE", start,
                "Greater than", Long.class.getName());

        Predicate p4 = Predicate.getPredicate("TXDATE", "TXDATE", end,
                "Lesser than", Long.class.getName());

        f.breakGroup(Filter.AND_OPERATOR);
        f.addPredicate(p3, Filter.AND_OPERATOR);
        f.breakGroup(Filter.AND_OPERATOR);
        f.addPredicate(p4, Filter.AND_OPERATOR);
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
