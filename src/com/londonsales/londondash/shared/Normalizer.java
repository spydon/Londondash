package com.londonsales.londondash.shared;

public class Normalizer {
	
	public static String capitalizeName(String name) {
		String nameList[] = name.split(" ");
		String capitalizedName = "";
		for(int x = 0; x<nameList.length; x++) {
			String tmpName = nameList[x];
			if(!tmpName.equals("")) {
				char letter = Character.toUpperCase(tmpName.charAt(0));
				tmpName = Character.toString(letter).concat(tmpName.substring(1));
				capitalizedName = capitalizedName.concat(tmpName);
				if(!(x==nameList.length-1))
					capitalizedName = capitalizedName.concat(" ");
			}
		}
		return capitalizedName;
	}
	
	public static String normalizeDate(String date) {
		int end = date.indexOf(".");
		if (end != -1)
			return date.substring(0, end);
		else
			return date;
	}
	
	public static Double tryParseDouble(String value) {
		try {
			Double val = null;
			if(value.contains("."))
				val = Double.parseDouble(value);
			return val;
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	public static Integer tryParseInt(String value) {
		try {
			int val = Integer.parseInt(value);
			return val;
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	public static String[] removeRow(String[] array, int row){
	    int length = array.length-1;
	    String[] arrayToReturn = new String[length];
	    int less = 0;
	    for(int i = 0; i < length; i++)
	    	if(i!=row)
	    		arrayToReturn[i-less] = array[i];
	    	else
	    		less = 1;
	    return arrayToReturn;
	}
	
//	public static Date tryParseDate(String value) {
//		try {
//			value = value.substring(0, value.indexOf("."));
//			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			Date val = parser.parse(value);
//			return val;
//		} catch (NumberFormatException nfe) {
//			return null;
//		} catch (ParseException e) {
//			return null;
//		}
//	}
//
//	public static Date parseDate(String value) {
//		value = value.substring(0, value.indexOf("."));
//		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		try {
//			Date val = parser.parse(value);
//			return val;
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
}
