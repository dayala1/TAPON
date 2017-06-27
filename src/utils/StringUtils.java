package utils;

public class StringUtils {
	public static String removeAccents(String string){
		assert string != null;
		
		String result;
		
		result = new String(string);
		
		result = result.replaceAll("[èéêë]","e");
	    result = result.replaceAll("[ûù]","u");
	    result = result.replaceAll("[ïî]","i");
	    result = result.replaceAll("[àâ]","a");
	    result = result.replaceAll("Ô","o");

	    result = result.replaceAll("[ÈÉÊË]","E");
	    result = result.replaceAll("[ÛÙ]","U");
	    result = result.replaceAll("[ÏÎ]","I");
	    result = result.replaceAll("[ÀÂ]","A");
	    result = result.replaceAll("Ô","O");
	    
	    return result;
		
	}
}
