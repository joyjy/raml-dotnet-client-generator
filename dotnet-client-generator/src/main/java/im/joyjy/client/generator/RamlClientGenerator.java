package im.joyjy.client.generator;

import im.joyjy.client.generator.dotnet.DotNetGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.mule.raml.ApiModelLoader;
import org.mule.raml.model.ApiModel;

public abstract class RamlClientGenerator {

	protected URL raml;
	protected File output;
	protected String namespace;

	public static RamlClientGenerator dotnet() {
		return new DotNetGenerator();
	}

	public RamlClientGenerator from(URL raml) {
		this.raml = raml;
		return this;
	}

	public RamlClientGenerator generateTo(File output){
		this.output = output;
		namespace = this.generate();
		return this;
	}
	
	protected ApiModel getApiModel() throws IOException{
		try (InputStreamReader inputStreamReader = new InputStreamReader(raml.openStream(), "UTF-8")) {
            return ApiModelLoader.build(inputStreamReader, raml.toExternalForm());
        }
	}

	protected abstract String generate();

	public String getPackage() {
		return namespace;
	}
}
