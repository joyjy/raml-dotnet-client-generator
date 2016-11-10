package im.joyjy.client.generator.dotnet.helpers;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.mule.raml.model.ActionType;

import com.fasterxml.jackson.databind.JsonNode;

public class NameHelper {

	/**
	 * 根据域名生成命名空间和类名
	 * @param domain
	 * @return
	 */
	public static String[] clientName(String domain) {
		String[] secs = domain.split("\\.");
		StringBuilder namespace = new StringBuilder();
		StringBuilder className = new StringBuilder();
		
		StringBuilder reserve = new StringBuilder();
		int end = secs.length-1;
		for(int i=end; i >= 0; i--){
			if(i != end){
				namespace.append(".").append(pascal(secs[i]));
				className.append(pascal(secs[i]));
			}
			reserve.append(".").append(secs[i]);
		}
		return new String[] {
				namespace.deleteCharAt(0).toString(),
				className.append("Client").toString(),
				reserve.deleteCharAt(0).toString()};
	}

	/**
	 * 根据文件和类名生成 .cs 文件名
	 * @param folder
	 * @param name
	 * @return
	 */
	public static  String csFilename(File folder, String name) {
		return folder.getAbsolutePath()+File.separator+name+ ".cs";
	}
	
	/**
	 * 根据 path 生成资源类名
	 * @param path
	 * @return
	 */
	public static String resourceName(String path) {
		return pascal(path.replaceFirst("/", ""));
	}

	/**
	 * 根据 HTTP Method 生成方法名
	 * @param httpMethod
	 * @return
	 */
	public static String methodName(ActionType httpMethod) {
		return pascal(StringUtils.lowerCase(httpMethod.name()));
	}

	/**
	 * 将 RAML 数据类型转换为
	 * @param type
	 * @return
	 */
	public static String typeName(String type) {
		switch(type){
		case "integer": return "int";
		case "string" : 
		case "boolean": return type;
		default: return pascal(type);
		}
	}

	public static String typeName(JsonNode value) {
		switch(value.getNodeType()){
		case NUMBER: return "int";
		case STRING: return "string";
		default: return pascal(StringUtils.lowerCase(value.getNodeType().name()));
		}
	}

	public static String pascal(String name) {
		return StringUtils.capitalize(name);
	}

	public static String list(String propType) {
		return "List<" + propType +">";
	}
}
