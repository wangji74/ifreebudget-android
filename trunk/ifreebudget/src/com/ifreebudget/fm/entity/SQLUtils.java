package com.ifreebudget.fm.entity;

public class SQLUtils {

    public static String buildInList(int[] array) {
        StringBuilder inList = new StringBuilder();
        int len = array.length;
        for (int i = 0; i < len; i++) {
            inList.append(array[i]);
            if (i < len - 1) {
                inList.append(",");
            }
        }
        return inList.toString();
    }
}
