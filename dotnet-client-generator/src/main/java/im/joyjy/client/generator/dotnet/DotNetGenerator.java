package im.joyjy.client.generator.dotnet;

import static im.joyjy.client.generator.dotnet.helpers.RuleHelper.*;
import static im.joyjy.client.generator.dotnet.helpers.TemplateHelper.*;
import im.joyjy.client.generator.RamlClientGenerator;
import im.joyjy.client.generator.dotnet.helpers.FileHelper;
import im.joyjy.client.generator.dotnet.helpers.NameHelper;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DotNetGenerator extends RamlClientGenerator {
	
	private String namespace;
	private String baseUri;
	private String clientName;

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
		this.clientName = clientName[1];
		baseUri = StringUtils.replace(model.getBaseUri(), "{version}", model.getVersion());

		if (!output.exists()) {
			output.mkdirs();
        }
		
		try {
			writeClientClass(output, model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return clientName[2]; // reservedDomain
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
	private void writeClientClass(File output, ApiModel model) throws IOException {
		
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
			try(BufferedReader reader = open("Client")){
				String line;
				while((line = reader.readLine()) != null){
					
					line = fillClientTemplate(line, namespace, clientName, baseUri, props.toString(), inits.toString());
					
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
	 * @param resourceEntry
	 * @param model
	 * @return 
	 * @throws IOException
	 */
	private String[] getAndWriteResourceClass(File folder, Entry<String, Resource> resourceEntry, ApiModel model) throws IOException {
			
		String resourceName = NameHelper.resourceName(resourceEntry.getKey());
		Resource resource = resourceEntry.getValue();
		
		if(resourceName.startsWith("{")){
			resourceName = "By" + NameHelper.pascal(resourceName.substring(1, resourceName.length()-1));
		}

		folder = FileHelper.cd(folder, resourceName);
		
		StringBuilder props = new StringBuilder();
		StringBuilder inits = new StringBuilder();
		for(Map.Entry<String, Resource> entry: resource.getResources().entrySet()){
			props.append(System.lineSeparator());
			inits.append(System.lineSeparator());
			String[] r = getAndWriteResourceClass(folder, entry, model);
			props.append(r[0]);
			inits.append(r[1]);
		}
		
		String file = NameHelper.csFilename(folder, resourceName);
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = open("Resource")){
				String line;
				while((line = reader.readLine()) != null){
					
					line = fillResourceTemplate(line, 
												namespace, resourceName, props.toString(), inits.toString(),
												actions(folder, resourceName, resource.getActions()));
					
					if(!line.contains(TOKEN)){
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}

		return new String[]{ fillProperty(resourceName, resourceName, comment(resource)), fillNew(resourceName)};
	}

	/**
	 * 生成所有方法
	 * @param actions
	 * @return
	 * @throws IOException 
	 */
	private String actions(File folder, String resourceName, Map<ActionType, Action> actions) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<ActionType, Action> entry: actions.entrySet()){
			
			String methodName = NameHelper.methodName(entry.getKey());
			String entityNamePrefix = resourceName+methodName;
			
			sb.append(System.lineSeparator());

			String paramType = getAndWriteParamType(folder, entityNamePrefix, entry.getValue());
			String resultType = getAndWriteResultType(folder, entityNamePrefix, entry.getValue().getResponses());
			
			sb.append(fillAction(resultType, methodName, paramType, comment(entry.getValue())));
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
		String props = fillProperties(action, queryParams);
		String path = fillPath(action, queryParams);
		
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			try(BufferedReader reader = open("Param")){
				String line;
				while((line = reader.readLine()) != null){
					
					line = fillParamTemplate(line, namespace, paramName, props, path, queryString(queryParams));
					
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
			try(BufferedReader reader = open("Result")){
				String line;
				while((line = reader.readLine()) != null){
					
					fillResultTemplate(line,namespace, resultName, getAndWriteProperties(folder, name, json));
					
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
			sb.append(fillProperty(propType, propName, ""));
		}
		
		return sb.toString();
	}
}
