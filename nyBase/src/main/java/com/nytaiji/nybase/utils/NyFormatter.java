package com.nytaiji.nybase.utils;

import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NyFormatter {
    private static final String DATE_FORMAT = "E, LLLL d, yyyy";
    private static final String SHORT_DATE_FORMAT = "LLL d, yyyy";

    public static CharSequence formatDate() {
        return formatDate(System.currentTimeMillis());
    }

    public static CharSequence formatDate(long millis) {
        return DateFormat.format(DATE_FORMAT, millis);
    }

    public static CharSequence formatShortDate(long millis) {
        return DateFormat.format(SHORT_DATE_FORMAT, millis);
    }

    public static CharSequence formatLongDate(long millis) {
        //	long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH");
        Date resultdate = new Date(millis);
        return sdf.format(resultdate);
    }

    public static CharSequence nyFormat(long millis) {
        //	long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date resultdate = new Date(millis);
        return sdf.format(resultdate);
    }

    public static long getDateLong(String dateS) {
        // String dateS = eDate.getText().toString().trim();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH);

        Date date = null;
        try {
            date = sdf.parse(dateS);
        } catch (ParseException e) {
            sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
            try {
                date = sdf.parse(dateS);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        long dateTimeMills = 0L;
        if (date != null) dateTimeMills = date.getTime();
        Log.e("MixedContainer", "mDate " + dateTimeMills);
        return dateTimeMills;
    }

    public static String getDateString(long dateTimeMills) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH);
        return formatter.format(dateTimeMills);
    }

    public static String dateRemoveBar(String date) {
        // 2022-6-12
        String year=date.substring(0, date.indexOf("-"));
        String month=date.substring(date.indexOf("-")+1, date.lastIndexOf("-"));
        String day=date.substring(date.lastIndexOf("-")+1);
        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        Log.e("nyFormatter--------", "date=" + date + "  dateRemoveBar= " + year + month + day);
        return year + month + day;
    }

    public static boolean isValidNyDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyymmddd",Locale.ENGLISH);
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
