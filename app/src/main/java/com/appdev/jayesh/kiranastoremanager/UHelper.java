package com.appdev.jayesh.kiranastoremanager;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// File Name: Singleton.java
public class UHelper {

    private static UHelper UHelper = new UHelper();

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private UHelper() {
    }

    /* Static 'instance' method */
    public static UHelper getInstance() {
        return UHelper;
    }

    /* Other methods protected by singleton-ness */
    protected static void demoMethod() {
    }

    public static String dateFormatdmyTOymd(String date) {
        if (date.length() != 0) {
            SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String dateString;
            try {
                dateString = to.format(from.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
                dateString = "";
            }
            return dateString;
        }
        return "";
    }

    public static String dateFormatdmyTOymdhms(String date) {
        if (date.length() != 0) {
            SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd 12:00:00", Locale.ENGLISH);
            String dateString;
            try {
                dateString = to.format(from.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
                dateString = "";
            }
            return dateString;
        }
        return "";
    }

    public static String dateFormatymdhmsTOdmy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String dateFormatymdTOdmy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String dateFormatymdhmsTOddmyyyy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String setPresentDateyyyyMMdd() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddCP() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddhhmmss() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddhhmm() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mma", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateddMMyyyy() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateDDMMYYhhmm() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy-hhmm", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateDDMMYYhhmmss() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy-hhmmss", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String getEmijoByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public static double parseDouble(EditText value) {
        double d = 0;
        String str = value.getText().toString();
        if (str != null) {
            try {
                d = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                d = 0;
            }
        }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static double parseDouble(TextView value) {
        double d = 0;
        String str = value.getText().toString();
        if (str != null) {
            try {
                d = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                d = 0;
            }
        }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static String stringDouble(String v) {

        return String.format(Locale.ENGLISH, "%.2f", parseDouble(v));

    }

    public static double parseDouble(String value) {
        double d;
        if (value == null)
            return 0;
        else
            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                d = 0;
            }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static int parseInt(String text) {
        int i = 0;
        try {
            i = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            i = 0;
            e.printStackTrace();
        }
        return i;
    }

    public static void showAlert(Activity act, String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle("Confirmation");
        alert.setMessage(msg);
        alert.setPositiveButton("OK", listener);
        alert.setNegativeButton("Cancel", listener);
        alert.show();
    }

    //added on 17-06-2018
    public static String getTime(String t) {
        Date dt = new Date();
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        SimpleDateFormat month = new SimpleDateFormat("MMM");
        SimpleDateFormat date = new SimpleDateFormat("dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");

        switch (t) {
            case "d":
                return date.format(dt.getTime());
            case "m":
                return month.format(dt.getTime());
            case "y":
                return year.format(dt.getTime());
            case "dt":
                return datetime.format(dt.getTime());
            case "time":
                return time.format(dt.getTime());
            default:
                datetime.format(dt.getTime());
        }
        return t;
    }

    public static String dateFormatdmyhmaToymdhms(String date) {

        SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyy hh:mm a");
        SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;


    }

    public static long dmyhmsTOmili(String date) {
        //creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    //for use with Emoji 24/06/2018 jayesh
    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    //for adding leading x number of zeros
    public static String intLeadingZero(int numberofZero, int number) {
        String digits = "%0" + numberofZero + "d";
        return String.format(digits, number);
    }

    public static String militoyyyymmdd(Long mili) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mili);
        return formatter.format(calendar.getTime());
    }

    public static String militoyyyymmddhm(Long mili) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mili);


        return formatter.format(calendar.getTime());
    }

    public static String militoddmmyyyyhhmmss(long mili) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mili);
        return formatter.format(calendar.getTime());
    }

    public static String militoddmmyyyyhhmma(long mili) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mili);
        return formatter.format(calendar.getTime());
    }

    public static String militoddmmyyyy(long mili) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mili);
        return formatter.format(calendar.getTime());
    }

    public static long ddmmyyyyHHmmTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static long ddmmyyyyhmaTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static long ddmmyyyyhmsTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static long ddmmyyyyTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static long ymdhmsTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static long ymdhmTomili(String date) {

//creates a formatter that parses the date in the given format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public static int[] convertMillis(long mili) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        int time[] = new int[4];

        long day = 8 * 60 * 60 * 1000;
        long hour = 60 * 60 * 1000;
        long minute = 60 * 1000;
        long second = 1000;

        if (mili > 0) {
            days = mili / day;
            hours = (mili % day) / hour;
            minutes = (mili % hour) / minute;
            seconds = (mili % minute) / second;
        }
        time[0] = (int) (days);
        time[1] = (int) hours;
        time[2] = (int) minutes;
        time[3] = (int) seconds;

        return time;
    }

    public static String timeFormathmsTOhma(String date) {
        SimpleDateFormat from = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat to = new SimpleDateFormat("hh:mm a");
        String timeString;
        try {
            timeString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            timeString = "";
        }
        return timeString;
    }

    public static long getTimeInMili() {

        Date date = new Date();

        return date.getTime();
    }

}
