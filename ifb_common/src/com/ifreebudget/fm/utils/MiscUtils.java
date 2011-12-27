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
package com.ifreebudget.fm.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import android.util.Log;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class MiscUtils {
    public static boolean isDateValid(String date) {
        ArrayList<String> split = splitString(date, "-");
        if (split == null || split.size() != 3) {
            return false;
        }
        try {
            if (split.get(0).length() != 4 || split.get(1).length() != 2
                    || split.get(2).length() != 2) {
                return false;
            }

            GregorianCalendar gc = new GregorianCalendar();
            Calendar now = Calendar.getInstance();
            int yr = Integer.parseInt(split.get(0));
            int mo = Integer.parseInt(split.get(1));
            int dt = Integer.parseInt(split.get(2));

            if (yr >= now.get(Calendar.YEAR)) {
                return false;
            }
            if (mo < 1 || mo > 12) {
                return false;
            }
            if (dt < 1 || dt > 31) {
                return false;
            }
            gc.set(Calendar.MONTH, (mo - 1));
            if (dt > gc.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                return false;
            }
            return true;
        }
        catch (NullPointerException e) {
            return false;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns an arraylist of strings splitting the input string using a
     * delimiter
     * 
     * @param String
     *            to split String delimiter
     * @return ArrayList containing strings split by delimiter
     */
    public static final ArrayList<String> splitString(String s, String delim) {
        ArrayList<String> list = new ArrayList<String>();
        if (s.length() == 0 || delim.length() == 0) {
            list.add(s);
            return list;
        }

        int pos;
        int curr = 0;

        while (true) {
            pos = s.indexOf(delim, curr);
            if (pos < 0) {
                if (curr > 0) {
                    String temp = s.substring(curr, s.length()).trim();
                    if (temp.length() > 0)
                        list.add(temp);
                }
                break;
            }
            String temp = s.substring(curr, pos).trim();
            if (!temp.equals(delim) && temp.length() > 0) {
                list.add(temp);
            }
            curr = pos + delim.length();
        }
        if (list.size() == 0)
            list.add(s);
        return list;
    }

    public static String wrapText(String s, int len) {
        StringTokenizer st = new StringTokenizer(s, " ");
        ArrayList<StringBuffer> rlist = new ArrayList<StringBuffer>();
        StringBuffer ret = new StringBuffer();
        while (st.hasMoreTokens()) {
            StringBuffer tok = new StringBuffer(st.nextToken());
            tok.append(" ");

            if (rlist.size() == 0) {
                rlist.add(tok);
                continue;
            }
            StringBuffer lastLine = rlist.get(rlist.size() - 1);
            if (tok.length() + lastLine.length() > len) {
                rlist.add(tok);
            }
            else {
                rlist.get(rlist.size() - 1).append(tok);
            }
        }
        for (int i = 0; i < rlist.size(); i++) {
            ret.append(rlist.get(i));
            ret.append('\n');
        }
        return ret.toString();
    }

    public static String wrapWords(String s) {
        if (s == null)
            return "";

        StringBuffer ret = new StringBuffer();
        StringBuffer line = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            line.append(s.charAt(i));
            if (line.length() >= 75) {
                ret.append(line);
                ret.append("<BR>");
                line.delete(0, line.length());
            }
        }

        return ret.toString();
    }

    private static int checkLength(StringBuffer s) {
        int pos = s.lastIndexOf("\n");
        if (pos >= 0) {
            return (s.length() - pos);
        }
        return -1;
    }

    /**
     * @param s
     * @return
     */
    public static String encodeHtmlChars(String s) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                ret.append("&lt;");
                break;
            case '>':
                ret.append("&gt;");
                break;
            case '&':
                ret.append("&amp;");
                break;
            default:
                ret.append(c);
                break;
            }
        }
        return ret.toString();
    }

    /**
     * 
     * @param s
     * @return
     */
    public static String decodeHtmlChars(String s) {
        s = s.replaceAll("&nbsp;", " ");
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        s = s.replaceAll("&amp;", "&");
        return s;
    }

    public static String getHtmlFormattedText(String s) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case ' ':
                ret.append("&nbsp;");
                break;
            case '\n':
                ret.append("<BR>");
                break;
            case '\t':
                ret.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                break;
            default:
                ret.append(c);
                break;
            }
        }
        return ret.toString();
    }

    public static String trimChars(String s, char c) {
        if (s == null || s.length() == 0)
            return s;
        int spos = 0;
        while (true) {
            if (s.charAt(spos) == c)
                spos++;
            else
                break;
            if (spos >= s.length() - 1)
                break;
        }

        int epos = s.length() - 1;
        while (true) {
            if (s.charAt(epos) == c)
                epos--;
            else
                break;
            if (epos <= 0)
                break;
        }
        if (spos <= epos && (epos < s.length())) {
            return (s.substring(spos, epos + 1));
        }
        if (spos + 1 + epos == s.length())
            return "";
        return s;
    }

    public static String stackTrace2String(Throwable t) {
        StringWriter sw = new StringWriter();
        try {
            t.printStackTrace(new PrintWriter(sw, true));
            String stacktrace = sw.toString();

            return stacktrace;
        }
        finally {
            try {
                if (sw != null)
                    sw.close();
            }
            catch (Exception e) {
                Log.e("MiscUtils", e.getMessage());
            }
        }
    }

    public static double getPercentMatch(String s1, String s2) {
        double percent = 0;
        if (s1.equals(s2)) {
            percent = 100;
        }
        else {
            ArrayList<String> splitS1 = splitString(s1, " ");
            ArrayList<String> splitS2 = splitString(s2, " ");

            int sz1 = splitS1.size();
            int sz2 = splitS2.size();

            int curr = 0;
            for (int i = 0; i < sz1; i++) {
                if (i < sz2) {
                    if (splitS1.get(i).equalsIgnoreCase(splitS2.get(i))) {
                        curr += 1;
                    }
                }
                else {
                    break;
                }
            }

            if (curr == sz2) {
                percent = 100;
            }
            percent = (100 * curr) / sz2;
        }

        return percent;
    }

    public static String getExtension(File file) {
        String fname = file.getAbsolutePath();
        int pos = fname.lastIndexOf('.');
        if (pos >= 0 && pos + 1 < fname.length()) {
            String ext = fname.substring(pos + 1);
            return ext;
        }
        return null;
    }

//    public static void main(String args[]) {
//        String x = "\"+0.86 - +0.16%\"";
//        System.out.println(trimChars(x, '"'));
//    }

    public static boolean isPrintableChar(char ch) {
        if (Character.isLetterOrDigit(ch) || (ch >= 32 && ch <= 126)) {
            return true;
        }
        return false;
    }

    public static boolean isValidString(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isValidString(String s, int len) {
        if (isValidString(s)) {
            if (s.length() == len) {
                return true;
            }
        }
        return false;
    }
}
