package im.joyjy.client.generator.dotnet.helpers;

import static im.joyjy.client.generator.dotnet.helpers.TemplateHelper.*;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.mule.raml.model.Action;
import org.mule.raml.model.Resource;
import org.mule.raml.model.TypeFieldDefinition;

public class RuleHelper{

	/**
	 * @param line
	 * @param namespace
	 * @param clientName
	 * @param baseUri
	 * @param props
	 * @param inits
	 * @return
	 */
	public static String fillClientTemplate(String line, String namespace, String clientName, String baseUri, String props, String inits) {

		if(line.contains(CLENT_NAME_TOKEN)){
			return StringUtils.replace(line, CLENT_NAME_TOKEN, clientName);
		}else if(line.contains(NAMESPACE_TOKEN)){
			return StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
		}else if(line.contains(BASE_URI_TOKEN)){
			return StringUtils.replace(line, BASE_URI_TOKEN, baseUri);
		}else if(line.contains(PROPERTIES_TOKEN)){
			return StringUtils.replace(line, PROPERTIES_TOKEN, props);
		}else if(line.contains(PROPERTIES_NEW_TOKEN)){
			return StringUtils.replace(line, PROPERTIES_NEW_TOKEN, inits.toString());
		}
		
		return line;
	}
	
	/**
	 * @param line
	 * @param namespace
	 * @param resourceName
	 * @param props
	 * @param inits
	 * @param actions
	 * @return
	 */
	public static String fillResourceTemplate(String line, String namespace, String resourceName, String props, String inits, String actions){

		if(line.contains(RESOURCE_NAME_TOKEN)){
			line = StringUtils.replace(line, RESOURCE_NAME_TOKEN, resourceName);
		}else if(line.contains(NAMESPACE_TOKEN)){
			line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
		}else if(line.contains(ACTIONS_TOKEN)){
			line = StringUtils.replace(line, ACTIONS_TOKEN, actions);
		}else if(line.contains(PROPERTIES_TOKEN)){
			line = StringUtils.replace(line, PROPERTIES_TOKEN, props.toString());
		}else if(line.contains(PROPERTIES_NEW_TOKEN)){
			line = StringUtils.replace(line, PROPERTIES_NEW_TOKEN, inits);
		}
		
		return line;
	}
	
	/**
	 * @param line
	 * @param namespace
	 * @param paramName
	 * @param props
	 * @param path
	 * @param queryString
	 * @return
	 */
	public static String fillParamTemplate(String line, String namespace, String paramName, String props, String path, String queryString){
		
		if(line.contains(PARAM_TOKEN)){
			line = StringUtils.replace(line, PARAM_TOKEN, paramName);
		} else if(line.contains(NAMESPACE_TOKEN)){
			line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
		} else if(line.contains(PROPERTIES_TOKEN)){
			line = StringUtils.replace(line, PROPERTIES_TOKEN, props);
		} else if(line.contains(PATH_TOKEN)){
			line = StringUtils.replace(line, PATH_TOKEN, path);
		} else if(line.contains(QUERYSTRINGS_TOKEN)){
			line = StringUtils.replace(line, QUERYSTRINGS_TOKEN, queryString);
		} else if(line.contains(HEADERS_TOKEN)){
			line = StringUtils.remove(line, HEADERS_TOKEN);
			line = StringUtils.remove(line, TOKEN);
		} else if(line.contains(CONTENT_TOKEN)){
			line = StringUtils.remove(line, CONTENT_TOKEN);
			line = StringUtils.remove(line, TOKEN);
		}
		
		return line;
	}
	
	/**
	 * @param line
	 * @param namespace
	 * @param resultName
	 * @param props
	 * @return
	 */
	public static String fillResultTemplate(String line, String namespace, String resultName, String props){
		if(line.contains(RESULT_TOKEN)){
			line = StringUtils.replace(line, RESULT_TOKEN, resultName);
		} else if(line.contains(NAMESPACE_TOKEN)){
			line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
		} else if(line.contains(PROPERTIES_TOKEN)){
			line = StringUtils.replace(line, PROPERTIES_TOKEN, props);
		}
		
		return line;
	}

	/**
	 * @param action
	 * @param queryParams 
	 * @return
	 */
	public static String fillProperties(Action action, Map<String, String> queryParams) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, TypeFieldDefinition> query: action.getQueryParameters().entrySet()){
			
			sb.append(System.lineSeparator());
			
			String prop = fillProperty(NameHelper.typeName(query.getValue().getType()), query.getKey(), comment(query.getValue()));
			sb.append(prop);
			
			queryParams.put(NameHelper.pascal(query.getKey()), query.getKey());
		}
		return sb.toString();
	}

	/**
	 * @param type
	 * @param comment
	 * @return
	 */
	public static String fillProperty(String type, String name, String comment) {
		String propertyTemplate = read("Property");
		
		String property = StringUtils.replace(propertyTemplate, TYPE_TOKEN, type);
		property = StringUtils.replace(property, NAME_TOKEN, NameHelper.pascal(name));
		return StringUtils.replace(property, COMMENT_TOKEN, comment);
	}

	/**
	 * @param resourceName
	 * @return
	 */
	public static String fillNew(String resourceName) {
		String newTemplate = read("New");
		
		String _new = StringUtils.replace(newTemplate, TYPE_TOKEN, resourceName);
		return StringUtils.replace(_new, NAME_TOKEN, NameHelper.pascal(resourceName));
	}
	
	/**
	 * @param resultType
	 * @param methodName
	 * @param paramType
	 * @param comment
	 * @return
	 */
	public static String fillAction(String resultType, String methodName, String paramType, String comment){
		String actionTemplate = read("Action");
		
		String method = StringUtils.replace(actionTemplate, RESULT_TOKEN, resultType);
		method = StringUtils.replace(method, METHOD_TOKEN, methodName);
		method = StringUtils.replace(method, PARAM_TOKEN, paramType);
		return StringUtils.replace(method, COMMENT_TOKEN, comment);
	}

	public static String fillPath(Action action, Map<String, String> queryParams) {
		String[] sections = action.getResource().getUri().split("/");
		
		StringBuilder sb = new StringBuilder("/");
		
		for(String sec:sections){
			if(sec.startsWith("{")){
				sec = NameHelper.pascal(sec.substring(1, sec.length()-1));
				if(!queryParams.containsKey(sec)){
					throw new RuntimeException("Didn't naming " + sec + " in queryParamters.");
				}else{
					queryParams.remove(sec);
				}
				sec = "\"+" + sec +"+\"";
			}
			if(sb.length() > 1){
				sb.append("/");
			}
			sb.append(sec);
		}
		
		return sb.toString();
	}

	/**
	 * @param queryParams
	 * @return
	 */
	public static String queryString(Map<String, String> queryParams) {
		if(queryParams.size() == 0){
			return "\"\"";
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> pair: queryParams.entrySet()){
			sb.append("\"&").append(pair.getValue()).append("=\"+").append(pair.getKey()).append("+");
		}
		
		return sb.replace(1, 1, "?").deleteCharAt(sb.length()-1).toString();
	}

	/**
	 * @param resource
	 * @return
	 */
	public static String comment(Resource resource) {
		if(resource.getDescription() != null){
			return resource.getDescription();
		}
		
		if(resource.getDisplayName() != null){
			return resource.getDisplayName();
		}

		return "";
	}

	/**
	 * @param action
	 * @return
	 */
	public static String comment(Action action) {
		if(action.getDescription() != null){
			return action.getDescription();
		}

		return "";
	}
	
	/**
	 * @param definition
	 * @return
	 */
	private static String comment(TypeFieldDefinition definition) {
		if(definition.getDescription() != null){
			return definition.getDescription();
		}
		
		if(definition.getName() != null){
			return definition.getName();
		}

		return "";
	}
}
