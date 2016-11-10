package im.joyjy.client.generator.dotnet.helpers;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class FileHelper {

	public static File cd(File folder, String name) {
		File subFolder = new File(folder.getAbsolutePath()+File.separator+StringUtils.lowerCase(name));
		if (!subFolder.exists()) {
			subFolder.mkdirs();
        }
		return subFolder;
	}

}
