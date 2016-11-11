package im.joyjy.client.generator.dotnet.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

public class TemplateHelper {
	
	// common
	public static final String TOKEN = "}}";
	static final String NAMESPACE_TOKEN = "{{namespace}}";
	static final String COMMENT_TOKEN = "{{comment}}";
	// properties
	static final String PROPERTIES_TOKEN = "{{properties}}";
	static final String PROPERTIES_NEW_TOKEN = "{{new properties}}";
	static final String TYPE_TOKEN = "{{type}}";
	static final String NAME_TOKEN = "{{name}}";
	// client
	static final String CLENT_NAME_TOKEN = "{{clientName}}";
	static final String BASE_URI_TOKEN = "{{baseUri}}";
	// resource
	static final String RESOURCE_NAME_TOKEN = "{{resourceName}}";
	static final String ACTIONS_TOKEN = "{{actions}}";
	static final String METHOD_TOKEN = "{{method}}";
	// param
	static final String PARAM_TOKEN = "{{paramName}}";
	static final String PATH_TOKEN = "{{path}}";
	static final String QUERYSTRINGS_TOKEN = "{{queryStrings}}";
	static final String HEADERS_TOKEN = "{{headers#";
	static final String CONTENT_TOKEN = "{{content#";
	// result
	static final String RESULT_TOKEN = "{{resultName}}";
	
	private static final String CLASSPATH = "/templates/dotnet/{}Template.cs";

	/**
	 * 获取
	 * @param template
	 * @return
	 */
	public static String read(String template) {
		StringBuilder sb = new StringBuilder(512);
	    try {
	        Reader r = new InputStreamReader(resource(template), "UTF-8");
	        int c = 0;
	        while ((c = r.read()) != -1) {
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    return sb.toString();
	}
	
	/**
	 * @param template
	 * @return
	 */
	public static BufferedReader open(String classPathResource){
		return new BufferedReader(new InputStreamReader(resource(classPathResource)));
	}
	
	/**
	 * 
	 * @param template
	 * @return
	 */
	private static InputStream resource(String template){
		return TemplateHelper.class.getResourceAsStream(StringUtils.replace(CLASSPATH, "{}", template));
	}
}
