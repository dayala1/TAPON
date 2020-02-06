package utils;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class NumberUtils {

	public static Double getNumericValue(String value){
		assert value != null;
		
		Double result;
		Matcher matcher;
		
		result = null;
		value = value.replace(',', '.');
		try {
			result = Double.parseDouble(value);
		} catch (Exception e) {
			//System.out.println(String.format("There was an error while trying to parse the number %s", value));
		}

		return result;
	}
}
