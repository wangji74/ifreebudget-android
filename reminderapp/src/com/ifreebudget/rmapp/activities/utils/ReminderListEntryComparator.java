package com.ifreebudget.rmapp.activities.utils;

import java.util.Comparator;

public class ReminderListEntryComparator implements
        Comparator<ReminderListEntry> {

    @Override
    public int compare(ReminderListEntry object1, ReminderListEntry object2) {
        return object1.getNextTime().compareTo(object2.getNextTime());
    }
}
