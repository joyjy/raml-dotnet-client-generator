package im.joyjy.client.generator.dotnet;

import im.joyjy.client.generator.RamlClientGenerator;
import im.joyjy.client.generator.dotnet.helpers.FileHelper;
import im.joyjy.client.generator.dotnet.helpers.NameHelper;
import im.joyjy.client.generator.dotnet.helpers.TemplateHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.mule.raml.model.Action;
import org.mule.raml.model.ActionType;
import org.mule.raml.model.ApiModel;
import org.mule.raml.model.MimeType;
import org.mule.raml.model.Resource;
import org.mule.raml.model.Response;
import org.mule.raml.model.TypeFieldDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DotNetGenerator extends RamlClientGenerator {

	// common
	private static final String TOKEN = "}}";
	private static final String NAMESPACE_TOKEN = "{{namespace}}";
	private static final String COMMENT_TOKEN = "{{comment}}";
	// properties
	private static final String PROPERTIES_TOKEN = "{{properties}}";
	private static final String PROPERTIES_NEW_TOKEN = "{{new properties}}";
	private static final String TYPE_TOKEN = "{{type}}";
	private static final String NAME_TOKEN = "{{name}}";
	// client
	private static final String CLENT_NAME_TOKEN = "{{clientName}}";
	private static final String BASE_URI_TOKEN = "{{baseUri}}";
	// resource
	private static final String RESOURCE_NAME_TOKEN = "{{resourceName}}";
	private static final String ACTIONS_TOKEN = "{{actions}}";
	private static final String METHOD_TOKEN = "{{method}}";
	// param
	private static final String PARAM_TOKEN = "{{paramName}}";
	private static final String PATH_TOKEN = "{{path}}";
	private static final String QUERYSTRINGS_TOKEN = "{{queryStrings}}";
	private static final String HEADERS_TOKEN = "{{headers#";
	private static final String CONTENT_TOKEN = "{{content#";
	// result
	private static final String RESULT_TOKEN = "{{resultName}}";
	
	private String namespace;

	@Override
	public String generate() {
		ApiModel model;
		try {
			model = getApiModel();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		String[] clientName = NameHelper.clientName(model.getTitle());
		namespace = clientName[0];
		String baseUri = StringUtils.replace(model.getBaseUri(), "{version}", model.getVersion());

		if (!output.exists()) {
			output.mkdirs();
        }
		
		try {
			writeClientClass(output, clientName[1], baseUri, model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return clientName[2];
	}
	
	/**
	 * 写入 Client
	 * @param output
	 * @param namespace
	 * @param clientName
	 * @param baseUri
	 * @param model
	 * @throws IOException
	 */
	private void writeClientClass(File output, String clientName, String baseUri, ApiModel model) throws IOException {
		
		String file = NameHelper.csFilename(output, clientName);
		
		StringBuilder props = new StringBuilder();
		StringBuilder inits = new StringBuilder();
		for(Map.Entry<String, Resource> entry: model.getResources().entrySet()){
			props.append(System.lineSeparator());
			inits.append(System.lineSeparator());
			String[] resource = getAndWriteResourceClass(output, entry, model);
			props.append(resource[0]);
			inits.append(resource[1]);
		}
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = TemplateHelper.open("/templates/dotnet/ClientTemplate.cs")){
				String line;
				while((line = reader.readLine()) != null){
					
					if(line.contains(CLENT_NAME_TOKEN)){
						line = StringUtils.replace(line, CLENT_NAME_TOKEN, clientName);
					}else if(line.contains(NAMESPACE_TOKEN)){
						line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
					}else if(line.contains(BASE_URI_TOKEN)){
						line = StringUtils.replace(line, BASE_URI_TOKEN, baseUri);
					}else if(line.contains(PROPERTIES_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_TOKEN, props.toString());
					}else if(line.contains(PROPERTIES_NEW_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_NEW_TOKEN, inits.toString());
					}
					
					if(!line.contains(TOKEN)){
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}
	}

	/**
	 * 写入资源
	 * @param output
	 * @param namespace
	 * @param resource
	 * @param model
	 * @return 
	 * @throws IOException
	 */
	private String[] getAndWriteResourceClass(File folder, Entry<String, Resource> resource, ApiModel model) throws IOException {
			
		String resourceName = NameHelper.resourceName(resource.getKey());

		folder = FileHelper.cd(folder, resourceName);
		
		StringBuilder props = new StringBuilder();
		StringBuilder inits = new StringBuilder();
		for(Map.Entry<String, Resource> entry: resource.getValue().getResources().entrySet()){
			props.append(System.lineSeparator());
			inits.append(System.lineSeparator());
			String[] r = getAndWriteResourceClass(folder, entry, model);
			props.append(r[0]);
			inits.append(r[1]);
		}
		
		String file = NameHelper.csFilename(folder, resourceName);
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = TemplateHelper.open("/templates/dotnet/ResourceTemplate.cs")){
				String line;
				while((line = reader.readLine()) != null){
					
					if(line.contains(RESOURCE_NAME_TOKEN)){
						line = StringUtils.replace(line, RESOURCE_NAME_TOKEN, resourceName);
					}else if(line.contains(NAMESPACE_TOKEN)){
						line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
					}else if(line.contains(ACTIONS_TOKEN)){
						line = StringUtils.replace(line, ACTIONS_TOKEN, actions(folder, resourceName, resource.getValue().getActions()));
					}else if(line.contains(PROPERTIES_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_TOKEN, props.toString());
					}else if(line.contains(PROPERTIES_NEW_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_NEW_TOKEN, inits.toString());
					}
					
					if(!line.contains(TOKEN)){
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}
		
		String propertyTemplate = TemplateHelper.readAll("/templates/dotnet/PropertyTemplate.cs");
		String property = StringUtils.replace(propertyTemplate, TYPE_TOKEN, resourceName);
		property = StringUtils.replace(property, NAME_TOKEN, NameHelper.pascal(resourceName));
		String comment = resource.getValue().getDescription() == null? resource.getValue().getDisplayName() + "":resource.getValue().getDescription();
		property = StringUtils.replace(property, COMMENT_TOKEN, comment);
		
		String newTemplate = TemplateHelper.readAll("/templates/dotnet/NewTemplate.cs");
		String _new = StringUtils.replace(newTemplate, TYPE_TOKEN, resourceName);
		_new = StringUtils.replace(_new, NAME_TOKEN, NameHelper.pascal(resourceName));
		return new String[]{property, _new};
	}

	/**
	 * 生成所有方法
	 * @param actions
	 * @return
	 * @throws IOException 
	 */
	private String actions(File folder, String resourceName, Map<ActionType, Action> actions) throws IOException {
		String actionTemplate = TemplateHelper.readAll("/templates/dotnet/ActionTemplate.cs");
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<ActionType, Action> entry: actions.entrySet()){
			
			String methodName = NameHelper.methodName(entry.getKey());
			
			sb.append(System.lineSeparator());
			String action = StringUtils.replace(actionTemplate, RESULT_TOKEN, getAndWriteResultType(folder, resourceName+methodName, entry.getValue().getResponses()));
			action = StringUtils.replace(action, METHOD_TOKEN, methodName);
			action = StringUtils.replace(action, PARAM_TOKEN, getAndWriteParamType(folder, resourceName+methodName, entry.getValue()));
			String comment = entry.getValue().getDescription()  + "";
			action = StringUtils.replace(action, COMMENT_TOKEN, comment);
			
			sb.append(action);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	/**
	 * 生成参数类型
	 * @param folder
	 * @param name
	 * @param action
	 * @return
	 * @throws IOException
	 */
	private String getAndWriteParamType(File folder, String name, Action action) throws IOException {
		
		String paramName = name + "Param";
		String file = NameHelper.csFilename(FileHelper.cd(folder, "models"), paramName);
		
		Map<String, String> queryParams = new LinkedHashMap<>();
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = TemplateHelper.open("/templates/dotnet/ParamTemplate.cs")){
				String line;
				while((line = reader.readLine()) != null){
					
					if(line.contains(PARAM_TOKEN)){
						line = StringUtils.replace(line, PARAM_TOKEN, paramName);
					} else if(line.contains(NAMESPACE_TOKEN)){
						line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
					} else if(line.contains(PROPERTIES_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_TOKEN, getProperties(action, queryParams));
					} else if(line.contains(PATH_TOKEN)){
						line = StringUtils.replace(line, PATH_TOKEN, action.getResource().getUri());
					} else if(line.contains(QUERYSTRINGS_TOKEN)){
						line = StringUtils.replace(line, QUERYSTRINGS_TOKEN, TemplateHelper.queryString(queryParams));
					} else if(line.contains(HEADERS_TOKEN)){
						line = StringUtils.remove(line, HEADERS_TOKEN);
						line = StringUtils.remove(line, TOKEN);
					} else if(line.contains(CONTENT_TOKEN)){
						line = StringUtils.remove(line, CONTENT_TOKEN);
						line = StringUtils.remove(line, TOKEN);
					}
					
					if(!line.contains(TOKEN)){
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}
		
		return paramName;
	}

	/**
	 * 为 Param 生成属性
	 * @param action
	 * @param queryParams 
	 * @return
	 */
	private String getProperties(Action action, Map<String, String> queryParams) {
		String propertyTemplate = TemplateHelper.readAll("/templates/dotnet/PropertyTemplate.cs");
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, TypeFieldDefinition> query: action.getQueryParameters().entrySet()){
			
			sb.append(System.lineSeparator());
			
			String prop = StringUtils.replace(propertyTemplate, TYPE_TOKEN, NameHelper.typeName(query.getValue().getType()));
			prop = StringUtils.replace(prop, NAME_TOKEN, NameHelper.pascal(query.getKey()));
			String comment = query.getValue().getDescription() == null ? query.getValue().getName() == null ? "": query.getValue().getName():query.getValue().getDescription();
			prop = StringUtils.replace(prop, COMMENT_TOKEN, comment);
			sb.append(prop);
			
			queryParams.put(query.getKey(), NameHelper.pascal(query.getKey()));
		}
		return sb.toString();
	}

	/**
	 * 选取第一个 2xx 状态码生成结果类型
	 * @param folder
	 * @param name
	 * @param responses
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private String getAndWriteResultType(File folder, String name, Map<String, Response> responses) throws JsonProcessingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();
		
		for(Entry<String, Response> entry: responses.entrySet()){
			if(entry.getKey().charAt(0) == '2'){
				for(Entry<String, MimeType> body: entry.getValue().getBody().entrySet()){
					return getAndWriteResultType(folder, name, mapper.readTree(body.getValue().getExample()));
				}
			}
		}
		
		//throw new RuntimeException("必须有返回值为 2xx 的 JSON Sample");
		return "ResponseBase";
	}

	/**
	 * 生成结果类型
	 * @param folder
	 * @param name
	 * @param json
	 * @return
	 * @throws IOException
	 */
	private String getAndWriteResultType(File folder, String name, JsonNode json) throws IOException {
		String resultName = name + "Result";
		String file = NameHelper.csFilename(FileHelper.cd(folder, "models"), resultName);
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = TemplateHelper.open("/templates/dotnet/ResultTemplate.cs")){
				String line;
				while((line = reader.readLine()) != null){
					
					if(line.contains(RESULT_TOKEN)){
						line = StringUtils.replace(line, RESULT_TOKEN, resultName);
					} else if(line.contains(NAMESPACE_TOKEN)){
						line = StringUtils.replace(line, NAMESPACE_TOKEN, namespace);
					} else if(line.contains(PROPERTIES_TOKEN)){
						line = StringUtils.replace(line, PROPERTIES_TOKEN, getAndWriteProperties(folder, name, json));
					}
					
					if(!line.contains(TOKEN)){
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}
		
		return resultName;
	}

	private String getAndWriteProperties(File folder, String name, JsonNode json) throws IOException {
		String propertyTemplate = TemplateHelper.readAll("/templates/dotnet/PropertyTemplate.cs");
		StringBuilder sb = new StringBuilder();
		
		Iterator<Entry<String, JsonNode>> itr = json.fields();
		while(itr.hasNext()){
			Entry<String, JsonNode> node = itr.next();
			String propType = NameHelper.typeName(node.getValue());
			String propName = NameHelper.pascal(node.getKey());
			
			if(node.getValue().isNull()){
				throw new RuntimeException(node.getValue() + "is null, can't known type.");
			}
			
			if(node.getValue().isObject()){
				propType = name + propName + "Result";
				getAndWriteResultType(folder, name + propName, node.getValue());
			}else if(node.getValue().isArray()){
				JsonNode first = node.getValue().elements().next();
				if(first == null){
					throw new RuntimeException(node.getValue() + "is empty, can't known type.");
				}
				propType = NameHelper.typeName(first);
				if(first.isObject()){
					propType = name + propName + "Result";
					getAndWriteResultType(folder, name + propName, first);
				}
				propType = NameHelper.list(propType);
			}
			
			if(node.getKey().equalsIgnoreCase("status") || node.getKey().equalsIgnoreCase("message")){
				continue;
			}
			
			sb.append(System.lineSeparator());
			
			String prop = StringUtils.replace(propertyTemplate, TYPE_TOKEN, propType);
			prop = StringUtils.replace(prop, NAME_TOKEN, propName);
			sb.append(prop);
		}
		
		return sb.toString();
	}
}
