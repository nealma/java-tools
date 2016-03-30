package com.nealma.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by neal.ma on 3/30/16.
 */
public class TimeTools {

    public static String getDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(getDateString(new Date()));
    }
}
