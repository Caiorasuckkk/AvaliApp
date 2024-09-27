package com.example.avaliapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateValidator {

    public static String getIso8601Date() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }
}
