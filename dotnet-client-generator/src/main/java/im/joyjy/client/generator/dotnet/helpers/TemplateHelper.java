package im.joyjy.client.generator.dotnet.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TemplateHelper {

	public static String readAll(String classPathResource) {
		StringBuilder sb = new StringBuilder(512);
	    try {
	        Reader r = new InputStreamReader(resource(classPathResource), "UTF-8");
	        int c = 0;
	        while ((c = r.read()) != -1) {
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    return sb.toString();
	}
	
	public static BufferedReader open(String classPathResource){
		return new BufferedReader(new InputStreamReader(resource(classPathResource)));
	}
	
	private static InputStream resource(String classPathResource){
		return TemplateHelper.class.getResourceAsStream(classPathResource);
	}

	public static String queryString(Map<String, String> queryParams) {
		if(queryParams.size() == 0){
			return "\"\"";
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> pair: queryParams.entrySet()){
			sb.append("\"&").append(pair.getKey()).append("=\"+").append(pair.getValue()).append("+");
		}
		
		return sb.replace(1, 1, "?").deleteCharAt(sb.length()-1).toString();
	}
}
