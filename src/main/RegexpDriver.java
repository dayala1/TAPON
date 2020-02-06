package main;

import java.util.ArrayList;
import java.util.List;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class RegexpDriver {
	
	private static Pattern pattern = Pattern.compile("[a-zA-Z0-9]+|\\p{P}+|<[^<>]+>");

	private static List<String> tokenize(String string) {
	    Matcher m = pattern.matcher(string);
	    List<String> matches = new ArrayList<String>();
	    
	    while(m.find()){
	        matches.add(m.group());
	    }

	    return matches;
	  }

	public static void main(String[] args) {
		System.out.println(tokenize("hol5a 6mundo7, esto es una prueba. 53-236-13"));

	}
}
