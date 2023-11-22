package com.io.codesystem.utils;

public class StringReplacementExample {
    public static void main(String[] args) {
        String inputString = "code-maintenance-source-files/inprocess/icd/20230831/icd102023.zip";
        String datePattern = "(\\d{8})";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(datePattern);
        java.util.regex.Matcher matcher = pattern.matcher(inputString);

        if (matcher.find()) {
            String date = matcher.group(1);
            String updatedString = inputString.replaceFirst("/inprocess/icd/" + date + "/", "/upload/icd/");
            System.out.println(updatedString);
        } else {
            System.out.println("Date not found in the input string.");
        }
    }
}

