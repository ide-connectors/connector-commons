package com.atlassian.connector.commons.jira;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * User: kalamon
 * Date: May 12, 2009
 * Time: 1:26:22 PM
 */
public class JiraTImeFormatter {


    public static String formatShortTimeFromJiraTimeString(String dateString) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
        DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            try {
                t = ds.format(df.parse(dateString));
            } catch (ParseException e2) {
                t = "Invalid";
            }
        }

        return t;
    }

    public static String formatDateTimeFromJiraTimeString(String dateString) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
        DateFormat ds = new SimpleDateFormat("dd/MMM/yy HH:mm");
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            try {
                t = ds.format(df.parse(dateString));
            } catch (ParseException e2) {
                t = "Invalid";
            }
        }

        return t;
    }

    public static String formatDateFromJiraTimeString(String dateString) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
        DateFormat ds = new SimpleDateFormat("dd/MMM/yy");
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            try {
                t = ds.format(df.parse(dateString));
            } catch (ParseException e2) {
                t = "Invalid";
            }
        }

            return t;
        }

    }