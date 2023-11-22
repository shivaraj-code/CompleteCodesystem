package com.io.codesystem.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateToStringExample {
    public static void main(String[] args) {
        // Create a java.util.Date object
        Date date = new Date();

        // Create a SimpleDateFormat object to define the desired format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        // Convert the Date to a formatted string
        String formattedDate = formatter.format(date);

        System.out.println(formattedDate);
    }
}

