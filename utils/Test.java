package com.io.codesystem.utils;

public class Test {
	public static void main(String[] args) {

		/*
		 * String regex = "\\d{4}[\\dA-Za-z]\\s.*"; String inputString =
		 * "9904T Teststring"; // Check if the inputString matches the regular
		 * expression using // String.matches() if (inputString.matches(regex)) {
		 * System.out.println("The inputString matches the pattern: " + inputString); }
		 * else { System.out.println("The inputString does not match the pattern."); }
		 */
		String regex = "\\d{4}[\\dA-Za-z]\\s.*";

		
		 String line = "0350T RADIOSTEREOMETRIC ANALYSIS LOWER EXTREMITY EXAM";
		
		//String line = "9920T	Office or other outpatient visit for the evaluation and management of a new patient, which requires a medically appropriate history and/or examination and straightforward medical decision making. When using time for code selection, 15-29 minutes of total time is spent on the date of the encounter.";
		System.out.println(line);
		if (line.matches(regex)) {
			// if (line.matches("^\\d.*")) { 
			//"\\d[4][\\dA-Za-z] \\s" 
			//String[] parts =line.split("\\s+", 2);
			System.out.println("character");
			String[] splitLine = line.trim().split("\\s+", 2);
			System.out.println(splitLine);
			String code = splitLine[0];
			String codeDesc = splitLine[1];
			System.out.println(code);
			System.out.println(codeDesc);
		}
	}

}
