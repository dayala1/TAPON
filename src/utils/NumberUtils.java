package utils;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class NumberUtils {
	
	private static Pattern numericPattern = Pattern.compile("([0-9]{1,3}(,[0-9]{1,3})+(\\.[0-9]+)?)|([0-9]*\\.[0-9]+)|([0-9]+)|([0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)");
	
	public static Double getNumericValue(String value){
		assert value != null;
		
		Double result;
		Matcher matcher;
		
		result = null;
		matcher = numericPattern.matcher(value);
		if(matcher.matches()){
			value = value.replace(',', '.');
			try {
				result = Double.parseDouble(value);
			} catch (Exception e) {
				System.out.println(String.format("There was an error while trying to parse the number %s", value));
			}
		}

		return result;
	}
}
